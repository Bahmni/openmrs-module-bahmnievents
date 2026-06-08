/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.bahmni.module.events.api.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.events.api.model.Event;
import org.springframework.context.event.EventListener;
import org.springframework.jms.core.JmsTemplate;

public class JMSEventPublisher {

	private static final Logger log = LogManager.getLogger(JMSEventPublisher.class);
	
	private final JmsTemplate jmsTemplate;
	
	private final ObjectMapper objectMapper;
	
	public JMSEventPublisher(JmsTemplate jmsTemplate, ObjectMapper objectMapper) {
		this.jmsTemplate = jmsTemplate;
		this.objectMapper = objectMapper;
	}

	@EventListener
	public void onApplicationEvent(Event event) {
		jmsTemplate.send(event.eventType.topic(), new JMSMessageCreator(objectMapper, event));
		log.info("Published Message with id : " + event.payloadId);
	}
}
