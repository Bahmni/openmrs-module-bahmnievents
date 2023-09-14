package org.bahmni.module.events.api.model;

public enum BahmniEventType {
    BAHMNI_PATIENT_CREATED("bahmni-patient"),
    BAHMNI_PATIENT_UPDATED("bahmni-patient"),
    BAHMNI_APPOINTMENT_CREATED("bahmni-appointment"),
    BAHMNI_APPOINTMENT_UPDATED("bahmni-appointment"),
    BAHMNI_RECURRING_APPOINTMENT_CREATED("bahmni-recurring-appointment"),
    BAHMNI_RECURRING_APPOINTMENT_UPDATED("bahmni-recurring-appointment"),
    BAHMNI_ENCOUNTER_CREATED("bahmni-encounter"),
    BAHMNI_ENCOUNTER_UPDATED("bahmni-encounter");

    private final String topic;
    BahmniEventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return topic;
    }
}