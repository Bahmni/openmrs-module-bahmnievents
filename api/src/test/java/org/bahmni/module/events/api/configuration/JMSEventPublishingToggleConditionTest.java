/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

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