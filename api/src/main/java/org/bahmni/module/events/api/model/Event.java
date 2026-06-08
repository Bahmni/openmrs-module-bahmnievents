/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.bahmni.module.events.api.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Event {

	private static final long version = 1L;
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
