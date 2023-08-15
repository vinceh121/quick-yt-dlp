package me.vinceh121.quickytdlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.FormattedMessage;

import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageProducer;
import me.vinceh121.quickytdlp.event.FinishedEvent;
import me.vinceh121.quickytdlp.event.IEvent;
import me.vinceh121.quickytdlp.event.ProgressEvent;

public class DownloadWorker implements Handler<Promise<Void>> {
	private static final Logger LOG = LogManager.getLogger(DownloadWorker.class);
	public static final String PROGRESS_MAGIC = "PROG:", PROGRESS_SEPARATOR = "----quick-yt-dlp-split",
			EVENT_BUS_PREFIX = "me.vinceh121.quickytdlp.";
	private final QuickYtDlp main;
	private final UUID id = UUID.randomUUID();
	private final MessageProducer<IEvent> eventPublisher;
	private final Map<String, ProgressEvent> state = new HashMap<>();
	private String downloadPath;
	private URL url;
	private boolean audioOnly = false;

	public DownloadWorker(QuickYtDlp main) {
		this.main = main;

		this.eventPublisher = this.main.getVertx().eventBus().publisher(EVENT_BUS_PREFIX + id);
	}

	@Override
	public void handle(final Promise<Void> p) {
		final String[] cmd = Arrays.stream(new String[] {
				this.main.getConfig().getYtDlpPath(),
				this.audioOnly ? "-x" : null,
				"--restrict-filenames",
				"--embed-metadata",
				"--newline",
				"--progress-template",
				PROGRESS_MAGIC + String.join(PROGRESS_SEPARATOR, "%(info.id)s", "%(info.title)s", "%(info.thumbnail)s",
						"%(progress.downloaded_bytes)s", "%(progress.total_bytes)s", "%(progress.eta)s",
						"%(progress.speed)s", "%(info.playlist_index)s", "%(info.playlist_count)s",
						"%(progress.status)s", "%(info.filename)s"),
				this.url.toString() }).filter(s -> s != null).toArray(l -> new String[l]);

		Path folderPath = this.main.getConfig().getDownloadFolder().resolve(this.id.toString());
		File folder = folderPath.toFile();
		folder.mkdir();

		final Process proc;
		try {
			proc = Runtime.getRuntime().exec(cmd, null, folder);
		} catch (IOException e) {
			p.fail(e);
			return;
		}

		final BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		br.lines().forEach(this::handleProgress);

		final int exitValue;
		try {
			exitValue = proc.waitFor();
		} catch (InterruptedException e) {
			p.fail(e);
			return;
		}

		if (exitValue != 0) {
			p.fail("yt-dlp exited with " + exitValue);
			try {
				LOG.error("yt-dlp exited with code {} for job {}. Stderr: {}", exitValue, this.id,
						new String(proc.getErrorStream().readAllBytes()));
			} catch (IOException e) {
				LOG.error(new FormattedMessage("yt-dlp exited with code {} for job {}. Couldn't read stderr.", this.id,
						exitValue), e);
			}
			return;
		}

		final File[] list = folder.listFiles();

		final String downloadPath;

		final Path zipPath;

		if (list.length == 1) {
			zipPath = null;
			downloadPath = this.main.getConfig().getDownloadBasePath() + "/" + this.id + "/" + list[0].getName();
		} else if (list.length > 1) {
			zipPath = this.main.getConfig().getDownloadFolder().resolve(this.id + ".zip");

			try (final ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipPath))) {
				zipOut.setLevel(9);

				for (File f : list) {
					ZipEntry entry = new ZipEntry(f.getName());
					zipOut.putNextEntry(entry);
					try (FileInputStream in = new FileInputStream(f)) {
						in.transferTo(zipOut);
					}
				}
			} catch (IOException e) {
				LOG.error(new FormattedMessage("Failed to create ZIP for job {}", this.id), e);
				p.fail(e);
				return;
			}

			downloadPath = this.main.getConfig().getDownloadBasePath() + "/" + this.id + ".zip";
		} else {
			throw new IllegalStateException();
		}

		this.downloadPath = downloadPath;
		this.eventPublisher.write(new FinishedEvent(this.id, downloadPath));

		this.main.getVertx().setTimer(this.main.getConfig().getDownloadTTL() * 1000, t -> {
			try {
				recurseDelete(folderPath);
				if (zipPath != null) {
					Files.deleteIfExists(zipPath);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		p.complete();
	}

	private void handleProgress(final String line) {
		if (!line.startsWith(PROGRESS_MAGIC)) {
			return;
		}

		final String[] parts
				= line.substring(PROGRESS_MAGIC.length(), line.length()).split(Pattern.quote(PROGRESS_SEPARATOR));

		final String videoId = parts[0];
		final String videoTitle = parts[1];
		final String videoThumbnail = "NA".equals(parts[2]) ? null : parts[2];
		final long downloadedBytes = Long.parseLong(parts[3]);
		final long totalBytes = Long.parseLong(parts[4]);
		final float eta = "NA".equals(parts[5]) ? Float.NaN : Float.parseFloat(parts[5]);
		final float speed = "NA".equals(parts[6]) ? Float.NaN : Float.parseFloat(parts[6]);
		final int playlistIndex = "NA".equals(parts[7]) ? -1 : Integer.parseInt(parts[7]);
		final int playlistCount = "NA".equals(parts[8]) ? -1 : Integer.parseInt(parts[8]);
		final String status = parts[9];
		final String filename = parts[10];

		final String downloadPath = this.main.getConfig().getDownloadBasePath() + "/" + this.id + "/" + filename;

		final ProgressEvent event = new ProgressEvent(this.id, videoId, videoTitle, videoThumbnail, downloadedBytes,
				totalBytes, eta, speed, playlistIndex, playlistCount, status, downloadPath);

		this.state.put(videoId, event);
		this.eventPublisher.write(event);
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public boolean isAudioOnly() {
		return audioOnly;
	}

	public void setAudioOnly(boolean audioOnly) {
		this.audioOnly = audioOnly;
	}

	public String getDownloadPath() {
		return this.downloadPath;
	}

	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}

	public UUID getId() {
		return id;
	}

	public static void recurseDelete(Path file) throws IOException {
		Files.walkFileTree(file, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return super.visitFile(file, attrs);
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return super.postVisitDirectory(dir, exc);
			}
		});
	}
}
