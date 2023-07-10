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