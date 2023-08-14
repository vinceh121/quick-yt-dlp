package me.vinceh121.quickytdlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageProducer;
import me.vinceh121.quickytdlp.event.FinishedEvent;
import me.vinceh121.quickytdlp.event.IEvent;
import me.vinceh121.quickytdlp.event.ProgressEvent;

public class DownloadWorker implements Handler<Promise<Void>> {
	public static final String PROGRESS_MAGIC = "PROG:", PROGRESS_SEPARATOR = "----quick-yt-dlp-split",
			EVENT_BUS_PREFIX = "me.vinceh121.quickytdlp.";
	private final QuickYtDlp main;
	private final UUID id = UUID.randomUUID();
	private final MessageProducer<IEvent> eventPublisher;
	private URL url;
	private boolean audioOnly = false;

	public DownloadWorker(QuickYtDlp main) {
		this.main = main;

		this.eventPublisher = this.main.getVertx().eventBus().publisher(EVENT_BUS_PREFIX + id);
	}

	@Override
	public void handle(final Promise<Void> p) {
		final String[] cmd = {
				this.main.getConfig().getYtDlpPath(),
				this.audioOnly ? "-x" : "",
				"--restrict-filenames",
				"--embed-metadata",
				"--newline",
				"--progress-template",
				PROGRESS_MAGIC + String.join(PROGRESS_SEPARATOR, "%(info.id)s", "%(info.title)s", "%(info.thumbnail)s",
						"%(progress.downloaded_bytes)s", "%(progress.total_bytes)s", "%(progress.eta)s",
						"%(progress.speed)s", "%(info.playlist_index)s", "%(info.playlist_count)s",
						"%(progress.status)s"),
				this.url.toString() };

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
				System.err.println(new String(proc.getErrorStream().readAllBytes()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		this.eventPublisher.write(new FinishedEvent(this.id));

		p.complete();
	}

	private void handleProgress(final String line) {
		System.out.println(line);
		if (!line.startsWith(PROGRESS_MAGIC)) {
			return;
		}

		final String[] parts
				= line.substring(PROGRESS_MAGIC.length(), line.length()).split(Pattern.quote(PROGRESS_SEPARATOR));

		final String videoId = parts[0];
		final String videoTitle = parts[1];
		final String videoThumbnail = parts[2];
		final long downloadedBytes = Long.parseLong(parts[3]);
		final long totalBytes = Long.parseLong(parts[4]);
		final float progress = "NA".equals(parts[5]) ? Float.NaN : Float.parseFloat(parts[5]);
		final float speed = "NA".equals(parts[6]) ? Float.NaN : Float.parseFloat(parts[6]);
		final int playlistIndex = "NA".equals(parts[7]) ? -1 : Integer.parseInt(parts[7]);
		final int playlistCount = "NA".equals(parts[8]) ? -1 : Integer.parseInt(parts[8]);
		final String status = parts[9];

		final ProgressEvent event = new ProgressEvent(this.id, videoId, videoTitle, videoThumbnail, downloadedBytes,
				totalBytes, progress, speed, playlistIndex, playlistCount, status);

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

	public UUID getId() {
		return id;
	}
}