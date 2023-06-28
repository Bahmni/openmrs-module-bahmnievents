package org.bahmni.module.events.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Event {
	
	public final String eventId;
	
	public final String eventType;
	
	public final String payloadId;
	
	public final Object payload;
	
	public final LocalDateTime publishedDateTime;
	
	public Event(String eventType, Object payload, String payloadId) {
		this.eventType = eventType;
		this.payload = payload;
		this.eventId = UUID.randomUUID().toString();
		this.payloadId = payloadId;
		this.publishedDateTime = LocalDateTime.now();
	}
	
}
