package com.ruralhealth.healthcentre.model;

import java.time.LocalDate;

// One row of the "consultations" table (what happened during an appointment)
public record Consultation(
        Integer id,
        Integer appointmentId,
        String symptoms,
        String diagnosis,
        String treatment,
        LocalDate consultDate) {
}
