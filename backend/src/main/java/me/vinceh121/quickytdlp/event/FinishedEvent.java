package me.vinceh121.quickytdlp.event;

import java.util.UUID;

public class FinishedEvent implements IEvent {
	private static final long serialVersionUID = -4317651162316876673L;
	private final UUID downloadId;
	private final String downloadPath;

	public FinishedEvent(UUID downloadId, String downloadPath) {
		this.downloadId = downloadId;
		this.downloadPath = downloadPath;
	}

	@Override
	public EventType getEventType() {
		return EventType.FINISHED;
	}

	@Override
	public UUID getDownloadId() {
		return this.downloadId;
	}
	
	public String getDownloadPath() {
		return this.downloadPath;
	}
}
