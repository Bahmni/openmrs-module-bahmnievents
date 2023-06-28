package org.bahmni.module.events.publisher;

import org.bahmni.module.events.model.Event;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Person;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventPublisherTest {
	
	@Test
	public void shouldPublishEventOnTopicNameAsEventType() {
		EventPublisher eventPublisher;
		JmsTemplate jmsTemplate = mock(JmsTemplate.class);
		ObjectMapper objectMapper = new ObjectMapper();
		eventPublisher = new EventPublisher(jmsTemplate, objectMapper);

		Person person = getPerson();
		String eventType = "bahmni-patient";
		Event event = new Event(eventType, person, "bce786c0-aa57-480d-be6a-23692590086b");
		eventPublisher.onApplicationEvent(event);

		verify(jmsTemplate, times(1)).send(eq(eventType), any(MessageCreator.class));
	}

	@Test
	public void shouldThrowExceptionGivenExceptionOccurredWhileConvertingThePayloadToJsonBeforePublishing() {
		EventPublisher eventPublisher;
		JmsTemplate jmsTemplate = mock(JmsTemplate.class);
		ObjectMapper objectMapper = mock(ObjectMapper.class);
		eventPublisher = new EventPublisher(jmsTemplate, objectMapper);

		Person person = getPerson();
		String eventType = "bahmni-patient";
		Event event = new Event(eventType, person, "bce786c0-aa57-480d-be6a-23692590086b");

		String errorMessage = "Error in converting to json";
		try {
			when(objectMapper.writeValueAsString(any())).thenThrow(new IOException(errorMessage));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Exception exception = assertThrows(RuntimeException.class, () -> eventPublisher.onApplicationEvent(event));

		assertEquals(errorMessage, exception.getCause().getMessage());
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
