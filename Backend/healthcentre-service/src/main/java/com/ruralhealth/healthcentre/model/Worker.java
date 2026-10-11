package com.ruralhealth.healthcentre.model;

// One row of the "workers" table (doctor, nurse, ANM, ASHA ...)
public record Worker(
        Integer id,
        Integer centreId,
        String name,
        String role,
        String phone) {
}
