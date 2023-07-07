package org.bahmni.module.events.api.model;

public enum BahmniEventType {
    PATIENT_CREATED_UPDATED("bahmni-patient");
    private final String topic;
    BahmniEventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return topic;
    }
}