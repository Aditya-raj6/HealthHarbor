package com.ruralhealth.sanitation.model;

import java.time.LocalDate;

// One row of the "sanitation_facilities" table (public toilet, handpump, drain ...)
public record Facility(
        Integer id,
        String name,
        String type,
        String village,
        String cleanlinessStatus,
        LocalDate lastMaintenance) {
}
