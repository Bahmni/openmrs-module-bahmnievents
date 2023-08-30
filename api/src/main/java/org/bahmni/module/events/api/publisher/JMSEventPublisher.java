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
