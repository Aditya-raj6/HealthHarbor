package com.ruralhealth.healthcentre.model;

import java.time.LocalTime;

// A "record" is a short way to write a class that only holds data.
// This one matches one row of the "health_centres" table.
public record HealthCentre(
        Integer id,
        String name,
        String village,
        String facilities,
        LocalTime openTime,
        LocalTime closeTime,
        String workingDays) {
}
