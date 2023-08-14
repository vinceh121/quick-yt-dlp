package me.vinceh121.quickytdlp.event;

import java.io.Serializable;
import java.util.UUID;

public interface IEvent extends Serializable {
	EventType getEventType();

	UUID getDownloadId();

	public enum EventType {
		PROGRESS, FINISHED;
	}
}
