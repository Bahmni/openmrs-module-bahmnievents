package org.bahmni.module.events.api.listener;

import org.bahmni.module.events.api.model.BahmniEventType;
import org.bahmni.module.events.api.model.Event;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.bahmni.module.events.api.model.BahmniEventType.*;

public class AppointmentAdvice implements AfterReturningAdvice, ApplicationEventPublisherAware, MethodBeforeAdvice {

	private ApplicationEventPublisher eventPublisher;

	private final ThreadLocal<Map<String,Integer>> threadLocal = new ThreadLocal<>();
	private final String APPOINTMENT_ID_KEY = "appointmentId";

	private final ApplicationContext applicationContext;

	public AppointmentAdvice(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
		Map<String, Integer> appointmentInfo = threadLocal.get();
		if (appointmentInfo != null) {
			BahmniEventType eventType = appointmentInfo.get(APPOINTMENT_ID_KEY) == null ? BAHMNI_APPOINTMENT_CREATED : BAHMNI_APPOINTMENT_UPDATED;
			threadLocal.remove();
			Appointment appointment = (Appointment) returnValue;
			AppointmentMapper appointmentMapper = applicationContext.getBean(AppointmentMapper.class);
			Object representation = appointmentMapper.constructResponse(appointment);
			Event event = new Event(eventType, representation, appointment.getUuid());
			eventPublisher.publishEvent(event);
			System.out.println("Successfully published event with uuid : " + appointment.getUuid());
		}
	}

	@Override
	public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {
		this.eventPublisher = applicationEventPublisher;
	}

	@Override
	public void before(Method method, Object[] objects, Object o) {
		Appointment appointment = ((Supplier<Appointment>) objects[0]).get();
		Map<String, Integer> appointmentInfo = new HashMap<>(1);
		appointmentInfo.put(APPOINTMENT_ID_KEY, appointment.getId());
		threadLocal.set(appointmentInfo);
	}
}