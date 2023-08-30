package org.bahmni.module.events.api.configuration;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class JMSEventPublishingToggleConditionTest {

    @Test
    public void shouldReturnFalseGivenEventPublishingTogglePropertyNotFound() {
        JMSEventPublishingToggleCondition JMSEventPublishingToggleCondition = new JMSEventPublishingToggleCondition();
        boolean matches = JMSEventPublishingToggleCondition.matches(null, null);
        Assertions.assertFalse(matches);
    }
}