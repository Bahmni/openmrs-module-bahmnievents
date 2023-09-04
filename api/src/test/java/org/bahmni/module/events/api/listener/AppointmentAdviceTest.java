package org.bahmni.module.events.api.listener;

import org.bahmni.module.events.api.model.BahmniEventType;
import org.bahmni.module.events.api.model.Event;
import org.bahmni.module.events.api.publisher.BahmniEventPublisher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Method;
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
    private BahmniEventPublisher bahmniEventPublisher;
    private AppointmentMapper appointmentMapper;

    private final String SAVE_APPOINTMENT_METHOD_NAME = "validateAndSave";

	@Before
	public void setUp() {
        appointmentMapper = mock(AppointmentMapper.class);
        bahmniEventPublisher = mock(BahmniEventPublisher.class);
        appointmentAdviceAdvice = new AppointmentAdvice(bahmniEventPublisher, appointmentMapper);
    }
	
	@Test
	public void shouldVerifyBahmniEventPublishIsCalledGivenAppointmentIsCreated() throws NoSuchMethodException {
        Method saveAppointmentMethod = AppointmentsService.class.getMethod(SAVE_APPOINTMENT_METHOD_NAME, Appointment.class);
        Supplier<Appointment> appointmentSupplier = getAppointmentSupplier();
        Object[] args = {appointmentSupplier};
        Appointment newAppointment = appointmentSupplier.get();
        newAppointment.setId(null);

        appointmentAdviceAdvice.before(saveAppointmentMethod, args, null);

		AppointmentDefaultResponse defaultResponse = new AppointmentDefaultResponse();
        defaultResponse.setUuid(newAppointment.getUuid());
        when(appointmentMapper.constructResponse(newAppointment)).thenReturn(defaultResponse);

        appointmentAdviceAdvice.afterReturning(appointmentSupplier.get(), saveAppointmentMethod, null, null);
		
		verify(bahmniEventPublisher, times(1)).publishEvent(any(Event.class));
	}

    @Test
    public void shouldPublishCreateEventGivenAppointmentIsCreated() throws NoSuchMethodException {
        Method saveAppointmentMethod = AppointmentsService.class.getMethod(SAVE_APPOINTMENT_METHOD_NAME, Appointment.class);
        Supplier<Appointment> appointmentSupplier = getAppointmentSupplier();
        Object[] args = {appointmentSupplier};
        Appointment newAppointment = appointmentSupplier.get();
        newAppointment.setId(null);

        appointmentAdviceAdvice.before(saveAppointmentMethod, args, null);

        AppointmentDefaultResponse defaultResponse = new AppointmentDefaultResponse();
        defaultResponse.setUuid(newAppointment.getUuid());
        when(appointmentMapper.constructResponse(newAppointment)).thenReturn(defaultResponse);

        appointmentAdviceAdvice.afterReturning(appointmentSupplier.get(), saveAppointmentMethod, null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(bahmniEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

        Event event = eventArgumentCaptor.getValue();
        assertEquals(BahmniEventType.BAHMNI_APPOINTMENT_CREATED, event.eventType);
    }

    @Test
    public void shouldPublishUpdateEventGivenAppointmentIsUpdated() throws NoSuchMethodException {
        Method saveAppointmentMethod = AppointmentsService.class.getMethod(SAVE_APPOINTMENT_METHOD_NAME, Appointment.class);
        Supplier<Appointment> appointmentSupplier = getAppointmentSupplier();
        Object[] args = {appointmentSupplier};
        Appointment appointment = appointmentSupplier.get();

        appointmentAdviceAdvice.before(saveAppointmentMethod, args, null);

        AppointmentDefaultResponse defaultResponse = new AppointmentDefaultResponse();
        defaultResponse.setUuid(appointment.getUuid());
        when(appointmentMapper.constructResponse(appointment)).thenReturn(defaultResponse);

        appointmentAdviceAdvice.afterReturning(appointmentSupplier.get(), saveAppointmentMethod, null, null);

        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(bahmniEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

        Event event = eventArgumentCaptor.getValue();
        assertEquals(BahmniEventType.BAHMNI_APPOINTMENT_UPDATED, event.eventType);
    }

	@Test
	public void shouldVerifyPublishedContentForAppointment() throws NoSuchMethodException {
        Method saveAppointmentMethod = AppointmentsService.class.getMethod(SAVE_APPOINTMENT_METHOD_NAME, Appointment.class);
        Supplier<Appointment> appointmentSupplier = getAppointmentSupplier();
        Object[] args = {appointmentSupplier};
        Appointment appointment = appointmentSupplier.get();

        appointmentAdviceAdvice.before(saveAppointmentMethod, args, null);

        AppointmentDefaultResponse defaultResponse = new AppointmentDefaultResponse();
        defaultResponse.setUuid(appointment.getUuid());
        when(appointmentMapper.constructResponse(appointment)).thenReturn(defaultResponse);

        appointmentAdviceAdvice.afterReturning(appointmentSupplier.get(), saveAppointmentMethod, null, null);

		ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
		verify(bahmniEventPublisher, times(1)).publishEvent(eventArgumentCaptor.capture());

		Event event = eventArgumentCaptor.getValue();
		assertEquals(BahmniEventType.BAHMNI_APPOINTMENT_UPDATED, event.eventType);
		assertEquals(appointment.getUuid(), event.payloadId);
	}
	
	private Supplier<Appointment> getAppointmentSupplier() {
        Appointment appointment = new Appointment();
        appointment.setId(123);
        appointment.setUuid(UUID.randomUUID().toString());

        return () -> appointment;
    }
}
