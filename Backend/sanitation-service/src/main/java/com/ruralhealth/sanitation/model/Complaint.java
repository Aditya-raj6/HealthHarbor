package com.ruralhealth.sanitation.model;

import java.time.LocalDate;

// One row of the "complaints" table.
// facilityName and village are filled in when reading (SQL JOIN) and ignored when saving.
public record Complaint(
        Integer id,
        Integer facilityId,
        String facilityName,
        String village,
        String description,
        String reportedBy,
        LocalDate reportedOn,
        String priority,
        String status) {
}
