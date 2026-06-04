package org.bahmni.module.events.api.listener;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.events.api.model.BahmniEventType;
import org.bahmni.module.events.api.model.Event;
import org.bahmni.module.events.api.publisher.BahmniEventPublisher;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.bahmni.module.events.api.model.BahmniEventType.BAHMNI_VISIT_CREATED;
import static org.bahmni.module.events.api.model.BahmniEventType.BAHMNI_VISIT_UPDATED;


public class VisitAdvice implements AfterReturningAdvice, MethodBeforeAdvice {
    private final Logger log = LogManager.getLogger(EncounterAdvice.class);

    private final BahmniEventPublisher eventPublisher;
    private final ThreadLocal<Map<String,Integer>> threadLocal = new ThreadLocal<>();
    private final String VISIT_ID_KEY = "visitId";
    private final Set<String> adviceMethodNames = Sets.newHashSet("saveVisit");

    public VisitAdvice() {
        this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
    }
    public VisitAdvice(BahmniEventPublisher bahmniEventPublisher) {
        this.eventPublisher = bahmniEventPublisher;
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
        if (adviceMethodNames.contains(method.getName())) {
            Map<String, Integer> visitInfo = threadLocal.get();
            // TODO: This is a workaround to avoid publishing duplicate events because currently the event is getting called twice. Need to find out the reason and resolve it.
            if (visitInfo != null) {
                BahmniEventType eventType = visitInfo.get(VISIT_ID_KEY) == null ? BAHMNI_VISIT_CREATED : BAHMNI_VISIT_UPDATED;
                threadLocal.remove();
                Visit visit = (Visit) returnValue;

                Object representation = ConversionUtil.convertToRepresentation(visit, Representation.FULL);
                Event event = new Event(eventType, representation, visit.getUuid());
                eventPublisher.publishEvent(event);

                System.out.println("Successfully published event with uuid : " + visit.getUuid());
            }
        }
    }

    @Override
    public void before(Method method, Object[] objects, Object o) {
        if (adviceMethodNames.contains(method.getName())) {
            Visit visit = (Visit) objects[0];
            Map<String, Integer> visitInfo = new HashMap<>(1);
            visitInfo.put(VISIT_ID_KEY, visit.getId());
            threadLocal.set(visitInfo);
        }
    }
}
