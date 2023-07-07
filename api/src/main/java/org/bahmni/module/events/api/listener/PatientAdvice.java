package org.bahmni.module.events.api.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.events.api.model.BahmniEventType;
import org.bahmni.module.events.api.model.Event;
import org.openmrs.Patient;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;

import static org.bahmni.module.events.api.model.BahmniEventType.PATIENT_CREATED_UPDATED;

public class PatientAdvice implements AfterReturningAdvice, ApplicationEventPublisherAware {
	
	private final Logger log = LogManager.getLogger(PatientAdvice.class);

	private ApplicationEventPublisher eventPublisher;

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
		
		Patient patient = (Patient) returnValue;
		
		Object representation = ConversionUtil.convertToRepresentation(patient, Representation.FULL);
		Event event = new Event(PATIENT_CREATED_UPDATED, representation, patient.getUuid());
		eventPublisher.publishEvent(event);
		
		log.info("Successfully published event with uuid : " + patient.getUuid());
	}
	
	@Override
	public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {
		this.eventPublisher = applicationEventPublisher;
	}
}
