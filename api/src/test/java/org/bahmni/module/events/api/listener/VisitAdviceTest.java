package org.bahmni.module.events.api.listener;

import org.bahmni.module.events.api.model.BahmniEventType;
import org.bahmni.module.events.api.model.Event;
import org.bahmni.module.events.api.publisher.BahmniEventPublisher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
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
public class VisitAdviceTest {
    private VisitAdvice visitAdvice;
    private BahmniEventPublisher bahmniEventPublisher;

    private final String VISIT_SAVE_METHOD_NAME = "saveVisit";

    @Before
    public void setUp() {
        bahmniEventPublisher = mock(BahmniEventPublisher.class);
        visitAdvice = new VisitAdvice(bahmniEventPublisher);
    }

    @Test
    public void shouldVerifyBahmniEventPublishIsCalledGivenVisitIsCreated() throws NoSuchMethodException {
        Method saveVisitMethod = VisitService.class.getMethod(VISIT_SAVE_METHOD_NAME, Visit.class);
        Visit newVisit = getVisit();
        PowerMockito.mockStatic(ConversionUtil.class);
        Object[] args = {newVisit};
        newVisit.setId(null);
        visitAdvice.before(saveVisitMethod, args, null);
        PowerMockito.when(ConversionUtil.convertToRepresentation(getVisit(), Representation.FULL)).thenReturn(newVisit);

        visitAdvice.afterReturning(getVisit(), saveVisitMethod, null, null);

        verify(bahmniEventPublisher, times(1)).publishEvent(any(Event.class));
    }

    @Test
    public void shouldPublishCreateEventGivenVisitIsCreated() throws NoSuchMethodException {
        Method saveVisitMethod = VisitService.class.getMethod(VISIT_SAVE_METHOD_NAME, Visit.class);
        Visit newVisit = getVisit();

        PowerMockito.mockStatic(ConversionUtil.class);
        PowerMockito.when(ConversionUtil.convertToRepresentation(getVisit(), Representation.FULL)).thenReturn(newVisit);

        Object[] args = {newVisit};
        newVisit.setId(null);
        visitAdvice.before(saveVisitMethod, args, null);
        visitAdvice.afterReturning(newVisit, saveVisitMethod, null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(bahmniEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

        Event event = eventArgumentCaptor.getValue();
        assertEquals(BahmniEventType.BAHMNI_VISIT_CREATED, event.eventType);
    }

    @Test
    public void shouldPublishUpdateEventGivenVisitIsUpdated() throws NoSuchMethodException {
        Method saveVisitMethod = VisitService.class.getMethod(VISIT_SAVE_METHOD_NAME, Visit.class);
        Visit newVisit = getVisit();

        PowerMockito.mockStatic(ConversionUtil.class);

        PowerMockito.when(ConversionUtil.convertToRepresentation(getVisit(), Representation.FULL)).thenReturn(newVisit);

        Object[] args = {newVisit};
        visitAdvice.before(saveVisitMethod, args, null);
        visitAdvice.afterReturning(newVisit, saveVisitMethod, null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(bahmniEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

        Event event = eventArgumentCaptor.getValue();
        assertEquals(BahmniEventType.BAHMNI_VISIT_UPDATED, event.eventType);
    }

    @Test
    public void shouldVerifyPublishedContentForAVisit() throws NoSuchMethodException {
        Method saveVisitMethod = VisitService.class.getMethod(VISIT_SAVE_METHOD_NAME, Visit.class);
        Visit newVisit = getVisit();

        PowerMockito.mockStatic(ConversionUtil.class);
        PowerMockito.when(ConversionUtil.convertToRepresentation(getVisit(), Representation.FULL)).thenReturn(newVisit);

        Object[] args = {newVisit};
        visitAdvice.before(saveVisitMethod, args, null);
        visitAdvice.afterReturning(newVisit, saveVisitMethod, null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(bahmniEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

        Event event = eventArgumentCaptor.getValue();
        assertEquals(BahmniEventType.BAHMNI_VISIT_UPDATED, event.eventType);
        assertEquals(newVisit.getUuid(), event.payloadId);
    }

    private Visit getVisit() {
        Visit visit = new Visit();
        visit.setUuid(UUID.randomUUID().toString());
        visit.setId(123);
        return visit;
    }
}
