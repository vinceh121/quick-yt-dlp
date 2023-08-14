package me.vinceh121.quickytdlp.event;

import java.util.UUID;

public class FinishedEvent implements IEvent {
	private static final long serialVersionUID = -4317651162316876673L;
	private final UUID downloadId;

	public FinishedEvent(UUID downloadId) {
		this.downloadId = downloadId;
	}

	@Override
	public EventType getEventType() {
		return EventType.FINISHED;
	}

	@Override
	public UUID getDownloadId() {
		return this.downloadId;
	}
}
