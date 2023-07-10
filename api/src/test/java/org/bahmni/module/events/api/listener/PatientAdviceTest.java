package org.bahmni.module.events.api.listener;

import org.bahmni.module.events.api.model.BahmniEventType;
import org.bahmni.module.events.api.model.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@PowerMockIgnore("javax.management.*")
@PrepareForTest({ ConversionUtil.class })
@RunWith(PowerMockRunner.class)
public class PatientAdviceTest {
	
	private final PatientAdvice patientAdvice = new PatientAdvice();
	
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Before
	public void setUp() {
		applicationEventPublisher = mock(ApplicationEventPublisher.class);
		patientAdvice.setApplicationEventPublisher(applicationEventPublisher);
	}
	
	@Test
	public void shouldVerifyBahmniEventPublishIsCalledGivenPatientIsCreated() {
		Patient newPatient = getPatient();
		PowerMockito.mockStatic(ConversionUtil.class);
		PowerMockito.when(ConversionUtil.convertToRepresentation(getPatient(), Representation.FULL)).thenReturn(newPatient);
		
		patientAdvice.afterReturning(getPatient(), null, null, null);
		
		verify(applicationEventPublisher, times(1)).publishEvent(any(Event.class));
	}

    @Test
    public void shouldPublishCreateEventGivenPatientIsCreated() {
        Patient newPatient = getPatient();

        PowerMockito.mockStatic(ConversionUtil.class);
        PowerMockito.when(ConversionUtil.convertToRepresentation(getPatient(), Representation.FULL)).thenReturn(newPatient);

        Object[] args = {newPatient};
        newPatient.setId(null);
        patientAdvice.before(null, args, null);
        patientAdvice.afterReturning(newPatient, null, null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(applicationEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

        Event event = eventArgumentCaptor.getValue();
        assertEquals(BahmniEventType.BAHMNI_PATIENT_CREATED, event.eventType);
    }

    @Test
    public void shouldPublishUpdateEventGivenPatientIsUpdated() {
        Patient newPatient = getPatient();

        PowerMockito.mockStatic(ConversionUtil.class);
        PowerMockito.when(ConversionUtil.convertToRepresentation(getPatient(), Representation.FULL)).thenReturn(newPatient);

        Object[] args = {newPatient};
        patientAdvice.before(null, args, null);
        patientAdvice.afterReturning(newPatient, null, null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(applicationEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

        Event event = eventArgumentCaptor.getValue();
        assertEquals(BahmniEventType.BAHMNI_PATIENT_UPDATED, event.eventType);
    }
	
	@Test
	public void shouldVerifyPublishedContentForAPatient() {
		Patient newPatient = getPatient();
		
		PowerMockito.mockStatic(ConversionUtil.class);
		PowerMockito.when(ConversionUtil.convertToRepresentation(getPatient(), Representation.FULL)).thenReturn(newPatient);

        Object[] args = {newPatient};
        patientAdvice.before(null, args, null);
		patientAdvice.afterReturning(newPatient, null, null, null);
		
		ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
		verify(applicationEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());
		
		Event event = eventArgumentCaptor.getValue();
		assertEquals(BahmniEventType.BAHMNI_PATIENT_UPDATED, event.eventType);
		assertEquals(newPatient.getUuid(), event.payloadId);
	}
	
	private Patient getPatient() {
        PersonName name = new PersonName();
        name.setGivenName("firstname");
        name.setFamilyName("lastname");
        name.setUuid(UUID.randomUUID().toString());

        Set<PersonName> names = new HashSet<>();
        names.add(name);

        Person person = new Person();
        person.setId(123);
        person.setGender("M");
        person.setBirthdate(new Date(694224000000L));
        person.setUuid(UUID.randomUUID().toString());
        person.setNames(names);

        return new Patient(person);
    }
}
