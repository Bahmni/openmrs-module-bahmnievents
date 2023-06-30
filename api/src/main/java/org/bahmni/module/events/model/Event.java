package org.bahmni.module.events.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Event {
	public final String eventId;
	public final BahmniEventType eventType;
	public final String payloadId;
	public final Object payload;
	public final LocalDateTime publishedDateTime;
	
	public Event(BahmniEventType eventType, Object payload, String payloadId) {
		this.eventType = eventType;
		this.payload = payload;
		this.eventId = UUID.randomUUID().toString();
		this.payloadId = payloadId;
		this.publishedDateTime = LocalDateTime.now();
	}
}
