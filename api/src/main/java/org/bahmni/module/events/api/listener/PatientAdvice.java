package org.bahmni.module.events.api.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.events.api.model.BahmniEventType;
import org.bahmni.module.events.api.model.Event;
import org.bahmni.module.events.api.publisher.BahmniEventPublisher;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.bahmni.module.events.api.model.BahmniEventType.BAHMNI_PATIENT_CREATED;
import static org.bahmni.module.events.api.model.BahmniEventType.BAHMNI_PATIENT_UPDATED;

public class PatientAdvice implements AfterReturningAdvice, MethodBeforeAdvice {
	
	private final Logger log = LogManager.getLogger(PatientAdvice.class);
	private final BahmniEventPublisher eventPublisher;
	private final ThreadLocal<Map<String,Integer>> threadLocal = new ThreadLocal<>();
	private final String PATIENT_ID_KEY = "patientId";

	public PatientAdvice() {
		this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
	}

	public PatientAdvice(BahmniEventPublisher bahmniEventPublisher) {
		this.eventPublisher = bahmniEventPublisher;
	}

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
		if (method.getName().equals("savePatient")) {
			Map<String, Integer> patientInfo = threadLocal.get();
			BahmniEventType eventType = patientInfo != null && patientInfo.get(PATIENT_ID_KEY) == null ? BAHMNI_PATIENT_CREATED : BAHMNI_PATIENT_UPDATED;
			threadLocal.remove();

			Patient patient = (Patient) returnValue;

			Object representation = ConversionUtil.convertToRepresentation(patient, Representation.FULL);
			Event event = new Event(eventType, representation, patient.getUuid());
			eventPublisher.publishEvent(event);

			log.info("Successfully published event with uuid : " + patient.getUuid());
		}
	}

	@Override
	public void before(Method method, Object[] objects, Object o) {
		if (method.getName().equals("savePatient")) {
			Patient patient = (Patient) objects[0];

			Map<String, Integer> patientInfo = new HashMap<>(1);
			patientInfo.put(PATIENT_ID_KEY, patient.getId());
			threadLocal.set(patientInfo);
		}
	}
}
