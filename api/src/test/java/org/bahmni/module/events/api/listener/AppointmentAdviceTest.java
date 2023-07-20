package org.bahmni.module.events.api.listener;

import org.bahmni.module.events.api.model.BahmniEventType;
import org.bahmni.module.events.api.model.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore("javax.management.*")
@PrepareForTest({ ConversionUtil.class })
@RunWith(PowerMockRunner.class)
public class AppointmentAdviceTest {

	private AppointmentAdvice appointmentAdviceAdvice;
	
	private ApplicationEventPublisher applicationEventPublisher;

    private ApplicationContext applicationContext;
    private AppointmentMapper appointmentMapper;
	
	@Before
	public void setUp() {
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        applicationContext = mock(ApplicationContext.class);
        appointmentAdviceAdvice = new AppointmentAdvice(applicationContext);
        appointmentAdviceAdvice.setApplicationEventPublisher(applicationEventPublisher);
        appointmentMapper = mock(AppointmentMapper.class);
	}
	
	@Test
	public void shouldVerifyBahmniEventPublishIsCalledGivenAppointmentIsCreated() {
        Supplier<Appointment> appointmentSupplier = getAppointmentSupplier();
        Object[] args = {appointmentSupplier};
        Appointment newAppointment = appointmentSupplier.get();
        newAppointment.setId(null);

        appointmentAdviceAdvice.before(null, args, null);

		AppointmentDefaultResponse defaultResponse = new AppointmentDefaultResponse();
        defaultResponse.setUuid(newAppointment.getUuid());
        when(applicationContext.getBean(AppointmentMapper.class)).thenReturn(appointmentMapper);
        when(appointmentMapper.constructResponse(newAppointment)).thenReturn(defaultResponse);

        appointmentAdviceAdvice.afterReturning(appointmentSupplier.get(), null, null, null);
		
		verify(applicationEventPublisher, times(1)).publishEvent(any(Event.class));
	}

    @Test
    public void shouldPublishCreateEventGivenAppointmentIsCreated() {
        Supplier<Appointment> appointmentSupplier = getAppointmentSupplier();
        Object[] args = {appointmentSupplier};
        Appointment newAppointment = appointmentSupplier.get();
        newAppointment.setId(null);

        appointmentAdviceAdvice.before(null, args, null);

        AppointmentDefaultResponse defaultResponse = new AppointmentDefaultResponse();
        defaultResponse.setUuid(newAppointment.getUuid());
        when(applicationContext.getBean(AppointmentMapper.class)).thenReturn(appointmentMapper);
        when(appointmentMapper.constructResponse(newAppointment)).thenReturn(defaultResponse);

        appointmentAdviceAdvice.afterReturning(appointmentSupplier.get(), null, null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(applicationEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

        Event event = eventArgumentCaptor.getValue();
        assertEquals(BahmniEventType.BAHMNI_APPOINTMENT_CREATED, event.eventType);
    }

    @Test
    public void shouldPublishUpdateEventGivenAppointmentIsUpdated() {
        Supplier<Appointment> appointmentSupplier = getAppointmentSupplier();
        Object[] args = {appointmentSupplier};
        Appointment appointment = appointmentSupplier.get();

        appointmentAdviceAdvice.before(null, args, null);

        AppointmentDefaultResponse defaultResponse = new AppointmentDefaultResponse();
        defaultResponse.setUuid(appointment.getUuid());
        when(applicationContext.getBean(AppointmentMapper.class)).thenReturn(appointmentMapper);
        when(appointmentMapper.constructResponse(appointment)).thenReturn(defaultResponse);

        appointmentAdviceAdvice.afterReturning(appointmentSupplier.get(), null, null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(applicationEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

        Event event = eventArgumentCaptor.getValue();
        assertEquals(BahmniEventType.BAHMNI_APPOINTMENT_UPDATED, event.eventType);
    }

	@Test
	public void shouldVerifyPublishedContentForAppointment() {
        Supplier<Appointment> appointmentSupplier = getAppointmentSupplier();
        Object[] args = {appointmentSupplier};
        Appointment appointment = appointmentSupplier.get();

        appointmentAdviceAdvice.before(null, args, null);

        AppointmentDefaultResponse defaultResponse = new AppointmentDefaultResponse();
        defaultResponse.setUuid(appointment.getUuid());
        when(applicationContext.getBean(AppointmentMapper.class)).thenReturn(appointmentMapper);
        when(appointmentMapper.constructResponse(appointment)).thenReturn(defaultResponse);

        appointmentAdviceAdvice.afterReturning(appointmentSupplier.get(), null, null, null);

		ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
		verify(applicationEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

		Event event = eventArgumentCaptor.getValue();
		assertEquals(BahmniEventType.BAHMNI_APPOINTMENT_UPDATED, event.eventType);
		assertEquals(appointment.getUuid(), event.payloadId);
	}
	
	private Supplier<Appointment> getAppointmentSupplier() {
        Appointment appointment = new Appointment();
        appointment.setId(123);
        appointment.setUuid(UUID.randomUUID().toString());

        Supplier<Appointment> appointmentSupplier = () -> appointment;
        return appointmentSupplier;
    }
}
