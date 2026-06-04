package org.bahmni.module.events.api.listener;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.events.api.model.BahmniEventType;
import org.bahmni.module.events.api.model.Event;
import org.bahmni.module.events.api.publisher.BahmniEventPublisher;
import org.hibernate.SessionFactory;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.bahmni.module.events.api.model.BahmniEventType.*;

public class EncounterAdvice implements AfterReturningAdvice, MethodBeforeAdvice {

	private final Logger log = LogManager.getLogger(EncounterAdvice.class);

	private final BahmniEventPublisher eventPublisher;
	private final SessionFactory sessionFactory;
	private final ThreadLocal<Map<String,Integer>> threadLocal = new ThreadLocal<>();
	private final String ENCOUNTER_ID_KEY = "encounterId";
	private final Set<String> adviceMethodNames = Sets.newHashSet("saveEncounter");

	public EncounterAdvice() {
		this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
		this.sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
	}

	public EncounterAdvice(BahmniEventPublisher bahmniEventPublisher,SessionFactory sessionFactory) {
		this.eventPublisher = bahmniEventPublisher;
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
		try {
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
		catch(Exception exception){
			log.error("Error in Bahmni events EncounterAdvice while sending Encounter event : ", exception);
			// ToDo : Remove below info logs once no session issue is completely resolved.
			Encounter encounter = (Encounter)returnValue;
 			log.info("DEBUG : Encounter with no session issue " +encounter.toString());
			log.info("DEBUG : Is Encounter Entity available in session ???? " +this.sessionFactory.getCurrentSession().contains(encounter));
			log.info("DEBUG : Statics hibernate ***** "+ this.sessionFactory.getStatistics());
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
