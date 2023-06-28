package org.bahmni.module.events.configuration;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

@Component
public class EventPublishingToggleCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        String event_publisher_toggle = System.getenv("PUBLISH_PATIENT_EVENTS_TO_JMS");
        return  event_publisher_toggle != null && event_publisher_toggle.equalsIgnoreCase("true");
    }
}