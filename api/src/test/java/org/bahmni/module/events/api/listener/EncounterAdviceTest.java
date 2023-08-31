package org.bahmni.module.events.api.listener;

import org.bahmni.module.events.api.model.BahmniEventType;
import org.bahmni.module.events.api.model.Event;
import org.bahmni.module.events.api.publisher.BahmniEventPublisher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@PowerMockIgnore("javax.management.*")
@PrepareForTest({ ConversionUtil.class })
@RunWith(PowerMockRunner.class)
public class EncounterAdviceTest {

	private EncounterAdvice encounterAdvice;
    private BahmniEventPublisher bahmniEventPublisher;

    private final String ENCOUNTER_SAVE_METHOD_NAME = "saveEncounter";

	@Before
	public void setUp() {
        bahmniEventPublisher = mock(BahmniEventPublisher.class);
        encounterAdvice = new EncounterAdvice(bahmniEventPublisher);
	}

	@Test
	public void shouldVerifyBahmniEventPublishIsCalledGivenEncounterIsCreated() throws NoSuchMethodException {
        Method saveEncounterMethod = EncounterService.class.getMethod(ENCOUNTER_SAVE_METHOD_NAME, Encounter.class);
		Encounter newEncounter = getEncounter();
		PowerMockito.mockStatic(ConversionUtil.class);
        Object[] args = {newEncounter};
        newEncounter.setId(null);
        encounterAdvice.before(saveEncounterMethod, args, null);
		PowerMockito.when(ConversionUtil.convertToRepresentation(getEncounter(), Representation.FULL)).thenReturn(newEncounter);

		encounterAdvice.afterReturning(getEncounter(), saveEncounterMethod, null, null);

		verify(bahmniEventPublisher, times(1)).publishEvent(any(Event.class));
	}

    @Test
    public void shouldPublishCreateEventGivenEncounterIsCreated() throws NoSuchMethodException {
        Method saveEncounterMethod = EncounterService.class.getMethod(ENCOUNTER_SAVE_METHOD_NAME, Encounter.class);
        Encounter newEncounter = getEncounter();

        PowerMockito.mockStatic(ConversionUtil.class);
        PowerMockito.when(ConversionUtil.convertToRepresentation(getEncounter(), Representation.FULL)).thenReturn(newEncounter);

        Object[] args = {newEncounter};
        newEncounter.setId(null);
        encounterAdvice.before(saveEncounterMethod, args, null);
        encounterAdvice.afterReturning(newEncounter, saveEncounterMethod, null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(bahmniEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

        Event event = eventArgumentCaptor.getValue();
        assertEquals(BahmniEventType.BAHMNI_ENCOUNTER_CREATED, event.eventType);
    }

    @Test
    public void shouldPublishUpdateEventGivenEncounterIsUpdated() throws NoSuchMethodException {
        Method saveEncounterMethod = EncounterService.class.getMethod(ENCOUNTER_SAVE_METHOD_NAME, Encounter.class);
        Encounter newEncounter = getEncounter();

        PowerMockito.mockStatic(ConversionUtil.class);
        PowerMockito.when(ConversionUtil.convertToRepresentation(getEncounter(), Representation.FULL)).thenReturn(newEncounter);

        Object[] args = {newEncounter};
        encounterAdvice.before(saveEncounterMethod, args, null);
        encounterAdvice.afterReturning(newEncounter, saveEncounterMethod, null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(bahmniEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

        Event event = eventArgumentCaptor.getValue();
        assertEquals(BahmniEventType.BAHMNI_ENCOUNTER_UPDATED, event.eventType);
    }

	@Test
	public void shouldVerifyPublishedContentForAEncounter() throws NoSuchMethodException {
        Method saveEncounterMethod = EncounterService.class.getMethod(ENCOUNTER_SAVE_METHOD_NAME, Encounter.class);
		Encounter newEncounter = getEncounter();

		PowerMockito.mockStatic(ConversionUtil.class);
		PowerMockito.when(ConversionUtil.convertToRepresentation(getEncounter(), Representation.FULL)).thenReturn(newEncounter);

        Object[] args = {newEncounter};
        encounterAdvice.before(saveEncounterMethod, args, null);
        encounterAdvice.afterReturning(newEncounter, saveEncounterMethod, null, null);

		ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
		verify(bahmniEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

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
