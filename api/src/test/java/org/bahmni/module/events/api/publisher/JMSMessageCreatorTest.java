package org.bahmni.module.events.api.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.module.events.api.model.Event;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Person;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Date;

import static org.bahmni.module.events.api.model.BahmniEventType.BAHMNI_PATIENT_CREATED;
import static org.bahmni.module.events.api.publisher.JMSMessageCreator.EventHeaderKey.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JMSMessageCreatorTest {

    @Test
    public void shouldVerifyUUIDAndIdOfPatientInJsonPayloadGivenEventData() throws JMSException {
        ObjectMapper objectMapper = new ObjectMapper();
        Person person = getPerson();
        Event event = new Event(BAHMNI_PATIENT_CREATED, person, person.getUuid());

        Session session = mock(Session.class);
        TextMessage message = mock(TextMessage.class);

        JMSMessageCreator jmsMessageCreator = new JMSMessageCreator(objectMapper, event);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> propertyKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> propertyValueCaptor = ArgumentCaptor.forClass(String.class);

        when(session.createTextMessage()).thenReturn(message);
        doNothing().when(message).setText(textCaptor.capture());
        doNothing().when(message).setStringProperty(propertyKeyCaptor.capture(), propertyValueCaptor.capture());

        jmsMessageCreator.createMessage(session);

        Assertions.assertTrue(textCaptor.getValue().contains("\"uuid\":\"bce786c0-aa57-480d-be6a-23692590086b\""));
        Assertions.assertTrue(textCaptor.getValue().contains("\"id\":123"));
    }

    @Test
    public void shouldVerifyHeadersInPatientEventGivenEventData() throws JMSException {
        ObjectMapper objectMapper = new ObjectMapper();
        Person person = getPerson();
        Event event = new Event(BAHMNI_PATIENT_CREATED, person, person.getUuid());

        Session session = mock(Session.class);
        TextMessage message = mock(TextMessage.class);

        JMSMessageCreator jmsMessageCreator = new JMSMessageCreator(objectMapper, event);

        ArgumentCaptor<String> propertyKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> propertyValueCaptor = ArgumentCaptor.forClass(String.class);

        when(session.createTextMessage()).thenReturn(message);
        doNothing().when(message).setText(anyString());
        doNothing().when(message).setStringProperty(propertyKeyCaptor.capture(), propertyValueCaptor.capture());

        jmsMessageCreator.createMessage(session);

        Assertions.assertEquals(EVENT_TYPE.key(), propertyKeyCaptor.getAllValues().get(0));
        Assertions.assertEquals("BAHMNI_PATIENT_CREATED", propertyValueCaptor.getAllValues().get(0));
        Assertions.assertEquals(PAYLOAD_ID.key(), propertyKeyCaptor.getAllValues().get(1));
        Assertions.assertEquals("bce786c0-aa57-480d-be6a-23692590086b", propertyValueCaptor.getAllValues().get(1));
        Assertions.assertEquals(EVENT_ID.key(), propertyKeyCaptor.getAllValues().get(2));
        Assertions.assertNotNull(propertyValueCaptor.getAllValues().get(2));
        Assertions.assertEquals(PUBLISHED_DATE_TIME.key(), propertyKeyCaptor.getAllValues().get(3));
        Assertions.assertNotNull(propertyValueCaptor.getAllValues().get(3));
    }

    @Test
    public void shouldVerifyEventPayloadToBeSentInJsonFormatGivenEventData() throws JMSException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Person person = getPerson();
        Event event = new Event(BAHMNI_PATIENT_CREATED, person, person.getUuid());

        Session session = mock(Session.class);
        TextMessage message = mock(TextMessage.class);

        JMSMessageCreator jmsMessageCreator = new JMSMessageCreator(objectMapper, event);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);

        when(session.createTextMessage()).thenReturn(message);
        doNothing().when(message).setText(textCaptor.capture());
        doNothing().when(message).setStringProperty(anyString(), anyString());

        jmsMessageCreator.createMessage(session);

        String newOutput =  textCaptor.getValue();

        objectMapper.readTree(newOutput);
    }



    private Person getPerson() {
        Person person = new Person();
        person.setId(123);
        person.setGender("M");
        person.setBirthdate(new Date(694224000000L));
        person.setUuid("bce786c0-aa57-480d-be6a-23692590086b");

        return person;
    }
}