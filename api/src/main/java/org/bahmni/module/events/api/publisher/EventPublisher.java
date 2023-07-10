package org.bahmni.module.events.api.publisher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.events.api.model.Event;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.event.EventListener;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.IOException;

public class EventPublisher {
	
	private static final Logger log = LogManager.getLogger(EventPublisher.class);
	
	private final JmsTemplate jmsTemplate;
	
	private final ObjectMapper objectMapper;
	
	public EventPublisher(JmsTemplate jmsTemplate, ObjectMapper objectMapper) {
		this.jmsTemplate = jmsTemplate;
		this.objectMapper = objectMapper;
	}
	
	@EventListener
	public void onApplicationEvent(Event event) {
		String jsonPayload = toJsonPayload(event.payload);
		jmsTemplate.send(event.eventType.topic(), session -> {
			TextMessage message = session.createTextMessage(jsonPayload);
			addMetaInfoInHeaders(message, event);
			return message;
		});
		log.info("Published Message with id : " + event.payloadId);
	}

	private void addMetaInfoInHeaders(TextMessage message, Event event) {
		try {
			message.setStringProperty("eventType", event.eventType.name());
			message.setStringProperty("payloadId", event.payloadId);
			message.setStringProperty("eventId", event.eventId);
			message.setStringProperty("publishedDateTime", event.publishedDateTime.toString());

			log.info("Added meta info for patient in headers : " + event.payloadId);
		}
		catch (JMSException exception) {
			log.error("Error while adding meta info to message : ", exception);
			throw new RuntimeException(exception);
		}
	}

	private String toJsonPayload(Object event) {
		String payload;
		try {
			payload = objectMapper.writeValueAsString(event);
		}
		catch (IOException exception) {
			log.error("Error while creating event payload : ", exception);
			throw new RuntimeException(exception);
		}
		return payload;
	}
}
