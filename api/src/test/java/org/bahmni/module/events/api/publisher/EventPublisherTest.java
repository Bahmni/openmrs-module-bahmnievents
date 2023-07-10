package org.bahmni.module.events.api.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.module.events.api.model.Event;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Person;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.springframework.jms.core.JmsTemplate;

import java.util.Date;

import static org.bahmni.module.events.api.model.BahmniEventType.BAHMNI_PATIENT_CREATED;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EventPublisherTest {
	
	@Test
	public void shouldPublishEventOnTopicNameAssociateWithEventType() {
		EventPublisher eventPublisher;
		JmsTemplate jmsTemplate = mock(JmsTemplate.class);
		ObjectMapper objectMapper = new ObjectMapper();
		eventPublisher = new EventPublisher(jmsTemplate, objectMapper);

		Person person = getPerson();
		Event event = new Event(BAHMNI_PATIENT_CREATED, person, person.getUuid());

		eventPublisher.onApplicationEvent(event);

		verify(jmsTemplate, times(1)).send(anyString(), any(JMSMessageCreator.class));
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
