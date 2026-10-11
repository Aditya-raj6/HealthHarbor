package com.ruralhealth.sanitation.model;

import java.time.LocalDate;

// One row of the "maintenance_records" table (repair / cleaning history)
public record MaintenanceRecord(
        Integer id,
        Integer facilityId,
        String activity,
        LocalDate doneOn,
        String doneBy) {
}
