package org.bahmni.module.events.api.listener;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.events.api.model.BahmniEventType;
import org.bahmni.module.events.api.model.Event;
import org.bahmni.module.events.api.publisher.BahmniEventPublisher;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
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
import java.util.Set;

import static org.bahmni.module.events.api.model.BahmniEventType.*;

public class EncounterAdvice implements AfterReturningAdvice, MethodBeforeAdvice {

	private final Logger log = LogManager.getLogger(EncounterAdvice.class);

	private final BahmniEventPublisher eventPublisher;
	private final ThreadLocal<Map<String,Integer>> threadLocal = new ThreadLocal<>();
	private final String ENCOUNTER_ID_KEY = "encounterId";
	private final Set<String> adviceMethodNames = Sets.newHashSet("saveEncounter");

	public EncounterAdvice() {
		this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
	}

	public EncounterAdvice(BahmniEventPublisher bahmniEventPublisher) {
		this.eventPublisher = bahmniEventPublisher;
	}

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
		if (adviceMethodNames.contains(method.getName())) {
			Map<String, Integer> encounterInfo = threadLocal.get();
			// TODO: This is a workaround to avoid publishing duplicate events because currently the event is getting called twice. Need to find out the reason and resolve it.
			if (encounterInfo != null) {
				BahmniEventType eventType = encounterInfo.get(ENCOUNTER_ID_KEY) == null ? BAHMNI_ENCOUNTER_CREATED : BAHMNI_ENCOUNTER_UPDATED;
				threadLocal.remove();
				Encounter encounter = (Encounter) returnValue;

				Object representation = ConversionUtil.convertToRepresentation(encounter, Representation.FULL);
				Event event = new Event(eventType, representation, encounter.getUuid());
				eventPublisher.publishEvent(event);

				System.out.println("Successfully published event with uuid : " + encounter.getUuid());
			}
		}
	}

	@Override
	public void before(Method method, Object[] objects, Object o) {
		if (adviceMethodNames.contains(method.getName())) {
			Encounter encounter = (Encounter) objects[0];
			Map<String, Integer> encounterInfo = new HashMap<>(1);
			encounterInfo.put(ENCOUNTER_ID_KEY, encounter.getId());
			threadLocal.set(encounterInfo);
		}
	}
}
