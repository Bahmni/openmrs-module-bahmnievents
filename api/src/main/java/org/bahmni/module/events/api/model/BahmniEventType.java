/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

package org.bahmni.module.events.api.model;

public enum BahmniEventType {
    BAHMNI_PATIENT_CREATED("bahmni-patient"),
    BAHMNI_PATIENT_UPDATED("bahmni-patient");
    private final String topic;
    BahmniEventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return topic;
    }
}