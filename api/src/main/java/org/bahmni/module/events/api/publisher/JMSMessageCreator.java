package org.bahmni.module.events.api.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.events.api.model.Event;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

public class JMSMessageCreator implements MessageCreator {

    private static final Logger log = LogManager.getLogger(JMSMessageCreator.class);
    private final ObjectMapper objectMapper;
    private final Event event;

    public JMSMessageCreator(ObjectMapper objectMapper, Event event) {
        this.objectMapper = objectMapper;
        this.event = event;
    }

    private Message addMetaInfoInHeaders(TextMessage message) throws JMSException {

        message.setStringProperty("eventType", event.eventType.name());
        message.setStringProperty("payloadId", event.payloadId);
        message.setStringProperty("eventId", event.eventId);
        message.setStringProperty("publishedDateTime", event.publishedDateTime.toString());

        log.info("Added meta info for patient in headers : " + event.payloadId);

        return message;
    }

    private String eventPayloadAsJson() {
        try {
            return objectMapper.writeValueAsString(event.payload);
        }
        catch (JsonProcessingException exception) {
            log.error("Error while converting payload to json : ", exception);
            throw new RuntimeException(exception);
        }
    }

    @Override
    public Message createMessage(Session session) throws JMSException {
        TextMessage message = session.createTextMessage();
        message.setText(eventPayloadAsJson());
        return addMetaInfoInHeaders(message);
    }
}
