package me.vinceh121.quickytdlp;

import java.nio.file.Path;

public class Config {
	private String listenAddress, ytDlpPath, downloadBasePath;
	private int port, workerInstances, downloadTTL;
	private Path downloadFolder = Path.of("/tmp/quick-yt-dlp");

	public String getListenAddress() {
		return this.listenAddress;
	}

	public void setListenAddress(String listenAddress) {
		this.listenAddress = listenAddress;
	}

	public String getYtDlpPath() {
		return this.ytDlpPath;
	}

	public void setYtDlpPath(String ytDlpPath) {
		this.ytDlpPath = ytDlpPath;
	}

	public String getDownloadBasePath() {
		return downloadBasePath;
	}

	public void setDownloadBasePath(String downloadBasePath) {
		this.downloadBasePath = downloadBasePath;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getWorkerInstances() {
		return this.workerInstances;
	}

	public void setWorkerInstances(int workerInstances) {
		this.workerInstances = workerInstances;
	}

	/**
	 * @return time until a download gets deleted, in seconds
	 */
	public int getDownloadTTL() {
		return this.downloadTTL;
	}

	/**
	 * @param downloadTTL time until a download gets deleted, in seconds
	 */
	public void setDownloadTTL(int downloadTTL) {
		this.downloadTTL = downloadTTL;
	}

	public Path getDownloadFolder() {
		return downloadFolder;
	}

	public void setDownloadFolder(Path downloadFolder) {
		this.downloadFolder = downloadFolder;
	}
}
