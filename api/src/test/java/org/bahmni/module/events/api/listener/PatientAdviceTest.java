package org.bahmni.module.events.api.listener;

import org.bahmni.module.events.api.model.BahmniEventType;
import org.bahmni.module.events.api.model.Event;
import org.bahmni.module.events.api.publisher.BahmniEventPublisher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Method;
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
	
	private PatientAdvice patientAdvice;
	
	private BahmniEventPublisher bahmniEventPublisher;
	
	@Before
	public void setUp() {
        bahmniEventPublisher = mock(BahmniEventPublisher.class);
        patientAdvice = new PatientAdvice(bahmniEventPublisher);
	}

    @Test
    public void shouldVerifyTheEventPublishedIsNotGettingTriggeredGivenPatientNeitherCreatedNorUpdated() throws NoSuchMethodException {
        Method savePatientMethod = PatientService.class.getMethod("purgePatient", Patient.class);
        Patient newPatient = getPatient();
        PowerMockito.mockStatic(ConversionUtil.class);
        Object[] args = {newPatient};
        newPatient.setId(null);
        patientAdvice.before(savePatientMethod, args, null);
        PowerMockito.when(ConversionUtil.convertToRepresentation(getPatient(), Representation.FULL)).thenReturn(newPatient);

        patientAdvice.afterReturning(getPatient(), savePatientMethod, null, null);

        verify(bahmniEventPublisher, times(0)).publishEvent(any(Event.class));
    }
	
	@Test
	public void shouldVerifyTheEventPublishedIsGettingTriggeredGivenPatientIsCreated() throws NoSuchMethodException {
        Method savePatientMethod = PatientService.class.getMethod("savePatient", Patient.class);
        Patient newPatient = getPatient();
		PowerMockito.mockStatic(ConversionUtil.class);
        Object[] args = {newPatient};
        newPatient.setId(null);
        patientAdvice.before(savePatientMethod, args, null);
		PowerMockito.when(ConversionUtil.convertToRepresentation(getPatient(), Representation.FULL)).thenReturn(newPatient);
		
		patientAdvice.afterReturning(getPatient(), savePatientMethod, null, null);
		
		verify(bahmniEventPublisher, times(1)).publishEvent(any(Event.class));
	}

    @Test
    public void shouldPublishCreateEventGivenPatientIsCreated() throws NoSuchMethodException {
        Patient newPatient = getPatient();
        Method savePatientMethod = PatientService.class.getMethod("savePatient", Patient.class);

        PowerMockito.mockStatic(ConversionUtil.class);
        PowerMockito.when(ConversionUtil.convertToRepresentation(getPatient(), Representation.FULL)).thenReturn(newPatient);

        Object[] args = {newPatient};
        newPatient.setId(null);
        patientAdvice.before(savePatientMethod, args, null);
        patientAdvice.afterReturning(newPatient, savePatientMethod, null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(bahmniEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

        Event event = eventArgumentCaptor.getValue();
        assertEquals(BahmniEventType.BAHMNI_PATIENT_CREATED, event.eventType);
    }

    @Test
    public void shouldPublishUpdateEventGivenPatientIsUpdated() throws NoSuchMethodException {
        Patient newPatient = getPatient();
        Method savePatientMethod = PatientService.class.getMethod("savePatient", Patient.class);

        PowerMockito.mockStatic(ConversionUtil.class);
        PowerMockito.when(ConversionUtil.convertToRepresentation(getPatient(), Representation.FULL)).thenReturn(newPatient);

        Object[] args = {newPatient};
        patientAdvice.before(savePatientMethod, args, null);
        patientAdvice.afterReturning(newPatient, savePatientMethod, null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(bahmniEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

        Event event = eventArgumentCaptor.getValue();
        assertEquals(BahmniEventType.BAHMNI_PATIENT_UPDATED, event.eventType);
    }
	
	@Test
	public void shouldVerifyEventPublishedContentGivenPatientIsCreated() throws NoSuchMethodException {
		Patient newPatient = getPatient();
        Method savePatientMethod = PatientService.class.getMethod("savePatient", Patient.class);
		
		PowerMockito.mockStatic(ConversionUtil.class);
		PowerMockito.when(ConversionUtil.convertToRepresentation(getPatient(), Representation.FULL)).thenReturn(newPatient);

        Object[] args = {newPatient};
        patientAdvice.before(savePatientMethod, args, null);
		patientAdvice.afterReturning(newPatient, savePatientMethod, null, null);
		
		ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
		verify(bahmniEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());
		
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
