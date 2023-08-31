package org.bahmni.module.events.api.publisher;

import org.bahmni.module.events.api.listener.PatientAdvice;
import org.bahmni.module.events.api.model.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Person;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Date;

import static org.bahmni.module.events.api.model.BahmniEventType.BAHMNI_PATIENT_CREATED;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BahmniEventPublisherTest {

    private final BahmniEventPublisher bahmniEventPublisher = new BahmniEventPublisher();

    private ApplicationEventPublisher applicationEventPublisher;

    @Before
    public void setUp() {
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        bahmniEventPublisher.setApplicationEventPublisher(applicationEventPublisher);
    }

    @Test
    public void shouldVerifyEventGetsPublished() {
        Person person = getPerson();
        Event event = new Event(BAHMNI_PATIENT_CREATED, person, person.getUuid());

        bahmniEventPublisher.publishEvent(event);

        verify(applicationEventPublisher, times(1)).publishEvent(event);
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