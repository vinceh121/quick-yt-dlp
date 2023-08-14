package me.vinceh121.quickytdlp.event;

import java.util.UUID;

public class ProgressEvent implements IEvent {
	private static final long serialVersionUID = -330167806283720593L;
	private final UUID downloadId;
	private final String videoId, videoTitle, videoThumbnail, status;
	private final long downloadedBytes, totalBytes;
	private final float progress, speed;
	private final int playlistIndex, playlistCount;

	public ProgressEvent(UUID downloadId, String videoId, String videoTitle, String videoThumbnail,
			long downloadedBytes, long totalBytes, float progress, float speed, int playlistIndex, int playlistCount,
			String status) {
		this.downloadId = downloadId;
		this.videoId = videoId;
		this.videoTitle = videoTitle;
		this.videoThumbnail = videoThumbnail;
		this.downloadedBytes = downloadedBytes;
		this.totalBytes = totalBytes;
		this.progress = progress;
		this.speed = speed;
		this.playlistIndex = playlistIndex;
		this.playlistCount = playlistCount;
		this.status = status;
	}

	@Override
	public EventType getEventType() {
		return EventType.PROGRESS;
	}

	@Override
	public UUID getDownloadId() {
		return downloadId;
	}

	public String getVideoId() {
		return videoId;
	}

	public String getVideoTitle() {
		return videoTitle;
	}

	public String getVideoThumbnail() {
		return videoThumbnail;
	}

	public long getDownloadedBytes() {
		return downloadedBytes;
	}

	public long getTotalBytes() {
		return totalBytes;
	}

	public float getProgress() {
		return progress;
	}

	public float getSpeed() {
		return speed;
	}

	public int getPlaylistIndex() {
		return playlistIndex;
	}

	public int getPlaylistCount() {
		return playlistCount;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return "ProgressEvent [downloadId="
				+ downloadId
				+ ", videoId="
				+ videoId
				+ ", videoTitle="
				+ videoTitle
				+ ", videoThumbnail="
				+ videoThumbnail
				+ ", status="
				+ status
				+ ", downloadedBytes="
				+ downloadedBytes
				+ ", totalBytes="
				+ totalBytes
				+ ", progress="
				+ progress
				+ ", speed="
				+ speed
				+ ", playlistIndex="
				+ playlistIndex
				+ ", playlistCount="
				+ playlistCount
				+ "]";
	}
}
