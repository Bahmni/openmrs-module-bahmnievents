package org.bahmni.module.events.configuration;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class EventPublishingToggleConditionTest {

    @Test
    public void shouldReturnFalseGivenEventPublishingTogglePropertyNotFound() {
        EventPublishingToggleCondition eventPublishingToggleCondition = new EventPublishingToggleCondition();
        boolean matches = eventPublishingToggleCondition.matches(null, null);
        Assertions.assertFalse(matches);
    }
}