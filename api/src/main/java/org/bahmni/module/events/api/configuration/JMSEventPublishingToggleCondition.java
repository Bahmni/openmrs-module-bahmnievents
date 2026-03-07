/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.bahmni.module.events.api.configuration;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

@Component
public class JMSEventPublishingToggleCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        String event_publisher_toggle = System.getenv("PUBLISH_PATIENT_EVENTS_TO_JMS");
        return  event_publisher_toggle != null && event_publisher_toggle.equalsIgnoreCase("true");
    }
}