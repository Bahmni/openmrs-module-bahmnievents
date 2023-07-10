package org.bahmni.module.events.api.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.events.api.model.BahmniEventType;
import org.bahmni.module.events.api.model.Event;
import org.openmrs.Patient;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.bahmni.module.events.api.model.BahmniEventType.BAHMNI_PATIENT_CREATED;

public class PatientAdvice implements AfterReturningAdvice, ApplicationEventPublisherAware, MethodBeforeAdvice {
	
	private final Logger log = LogManager.getLogger(PatientAdvice.class);

	private ApplicationEventPublisher eventPublisher;

	private final ThreadLocal<Map<String,Integer>> threadLocal = new ThreadLocal<>();

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {

		Map<String, Integer> patientInfo = threadLocal.get();
		BahmniEventType eventType = BahmniEventType.BAHMNI_PATIENT_UPDATED;
		if(patientInfo == null || patientInfo.isEmpty() || patientInfo.get("patientId") == null) {
			eventType = BAHMNI_PATIENT_CREATED;
		}
		threadLocal.remove();

		Patient patient = (Patient) returnValue;
		
		Object representation = ConversionUtil.convertToRepresentation(patient, Representation.FULL);
		Event event = new Event(eventType, representation, patient.getUuid());
		eventPublisher.publishEvent(event);
		
		log.info("Successfully published event with uuid : " + patient.getUuid());
	}
	
	@Override
	public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {
		this.eventPublisher = applicationEventPublisher;
	}

	@Override
	public void before(Method method, Object[] objects, Object o) {
		Patient patient = (Patient) objects[0];

		Map<String, Integer> patientInfo = new HashMap<>(1);
		patientInfo.put("patientId", patient.getId());
		threadLocal.set(patientInfo);
	}
}
