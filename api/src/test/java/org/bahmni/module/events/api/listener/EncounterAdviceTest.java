package org.bahmni.module.events.api.listener;

import org.bahmni.module.events.api.model.BahmniEventType;
import org.bahmni.module.events.api.model.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openmrs.Encounter;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@PowerMockIgnore("javax.management.*")
@PrepareForTest({ ConversionUtil.class })
@RunWith(PowerMockRunner.class)
public class EncounterAdviceTest {

	private final EncounterAdvice encounterAdvice = new EncounterAdvice();

	private ApplicationEventPublisher applicationEventPublisher;

	@Before
	public void setUp() {
		applicationEventPublisher = mock(ApplicationEventPublisher.class);
        encounterAdvice.setApplicationEventPublisher(applicationEventPublisher);
	}

	@Test
	public void shouldVerifyBahmniEventPublishIsCalledGivenEncounterIsCreated() {
		Encounter newEncounter = getEncounter();
		PowerMockito.mockStatic(ConversionUtil.class);
        Object[] args = {newEncounter};
        newEncounter.setId(null);
        encounterAdvice.before(null, args, null);
		PowerMockito.when(ConversionUtil.convertToRepresentation(getEncounter(), Representation.FULL)).thenReturn(newEncounter);

		encounterAdvice.afterReturning(getEncounter(), null, null, null);

		verify(applicationEventPublisher, times(1)).publishEvent(any(Event.class));
	}

    @Test
    public void shouldPublishCreateEventGivenEncounterIsCreated() {
        Encounter newEncounter = getEncounter();

        PowerMockito.mockStatic(ConversionUtil.class);
        PowerMockito.when(ConversionUtil.convertToRepresentation(getEncounter(), Representation.FULL)).thenReturn(newEncounter);

        Object[] args = {newEncounter};
        newEncounter.setId(null);
        encounterAdvice.before(null, args, null);
        encounterAdvice.afterReturning(newEncounter, null, null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(applicationEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

        Event event = eventArgumentCaptor.getValue();
        assertEquals(BahmniEventType.BAHMNI_ENCOUNTER_CREATED, event.eventType);
    }

    @Test
    public void shouldPublishUpdateEventGivenEncounterIsUpdated() {
        Encounter newEncounter = getEncounter();

        PowerMockito.mockStatic(ConversionUtil.class);
        PowerMockito.when(ConversionUtil.convertToRepresentation(getEncounter(), Representation.FULL)).thenReturn(newEncounter);

        Object[] args = {newEncounter};
        encounterAdvice.before(null, args, null);
        encounterAdvice.afterReturning(newEncounter, null, null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(applicationEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

        Event event = eventArgumentCaptor.getValue();
        assertEquals(BahmniEventType.BAHMNI_ENCOUNTER_UPDATED, event.eventType);
    }

	@Test
	public void shouldVerifyPublishedContentForAEncounter() {
		Encounter newEncounter = getEncounter();

		PowerMockito.mockStatic(ConversionUtil.class);
		PowerMockito.when(ConversionUtil.convertToRepresentation(getEncounter(), Representation.FULL)).thenReturn(newEncounter);

        Object[] args = {newEncounter};
        encounterAdvice.before(null, args, null);
        encounterAdvice.afterReturning(newEncounter, null, null, null);

		ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
		verify(applicationEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

		Event event = eventArgumentCaptor.getValue();
		assertEquals(BahmniEventType.BAHMNI_ENCOUNTER_UPDATED, event.eventType);
		assertEquals(newEncounter.getUuid(), event.payloadId);
	}

	private Encounter getEncounter() {
        Encounter encounter = new Encounter();
        encounter.setUuid(UUID.randomUUID().toString());
        encounter.setId(123);
        return encounter;
    }
}
