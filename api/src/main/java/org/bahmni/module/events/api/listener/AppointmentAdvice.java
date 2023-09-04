package org.bahmni.module.events.api.listener;

import com.google.common.collect.Sets;
import org.bahmni.module.events.api.model.BahmniEventType;
import org.bahmni.module.events.api.model.Event;
import org.bahmni.module.events.api.publisher.BahmniEventPublisher;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static org.bahmni.module.events.api.model.BahmniEventType.BAHMNI_APPOINTMENT_CREATED;
import static org.bahmni.module.events.api.model.BahmniEventType.BAHMNI_APPOINTMENT_UPDATED;

public class AppointmentAdvice implements AfterReturningAdvice, MethodBeforeAdvice {

	private final BahmniEventPublisher eventPublisher;
	private final ThreadLocal<Map<String,Integer>> threadLocal = new ThreadLocal<>();
	private final String APPOINTMENT_ID_KEY = "appointmentId";
	private final AppointmentMapper appointmentMapper;
	private final Set<String> adviceMethodNames = Sets.newHashSet("validateAndSave");

	public AppointmentAdvice() {
		this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
		this.appointmentMapper = Context.getRegisteredComponent("appointmentMapper", AppointmentMapper.class);
	}

	public AppointmentAdvice(BahmniEventPublisher bahmniEventPublisher, AppointmentMapper appointmentMapper) {
		this.eventPublisher = bahmniEventPublisher;
		this.appointmentMapper = appointmentMapper;
	}

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
		if (adviceMethodNames.contains(method.getName())) {
			Map<String, Integer> appointmentInfo = threadLocal.get();
			// TODO: This is a workaround to avoid publishing duplicate events because currently the event is getting called twice. Need to find out the reason and resolve it.
			if (appointmentInfo != null) {
				BahmniEventType eventType = appointmentInfo.get(APPOINTMENT_ID_KEY) == null ? BAHMNI_APPOINTMENT_CREATED : BAHMNI_APPOINTMENT_UPDATED;
				threadLocal.remove();
				Appointment appointment = (Appointment) returnValue;
				Object representation = appointmentMapper.constructResponse(appointment);
				Event event = new Event(eventType, representation, appointment.getUuid());
				eventPublisher.publishEvent(event);
				System.out.println("Successfully published event with uuid : " + appointment.getUuid());
			}
		}
	}

	@Override
	public void before(Method method, Object[] objects, Object o) {
		if (adviceMethodNames.contains(method.getName())) {
			Appointment appointment = ((Supplier<Appointment>) objects[0]).get();
			Map<String, Integer> appointmentInfo = new HashMap<>(1);
			appointmentInfo.put(APPOINTMENT_ID_KEY, appointment.getId());
			threadLocal.set(appointmentInfo);
		}
	}
}