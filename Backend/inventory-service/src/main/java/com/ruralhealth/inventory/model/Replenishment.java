package com.ruralhealth.inventory.model;

import java.time.LocalDate;

// One row of the "replenishment_requests" table: a request to refill stock.
// medicineName is filled in when reading (SQL JOIN) and ignored when saving.
public record Replenishment(
        Integer id,
        Integer medicineId,
        String medicineName,
        Integer centreId,
        Integer quantityNeeded,
        String requestedBy,
        LocalDate requestedOn,
        String status) {
}
