package com.ruralhealth.inventory.model;

import java.time.LocalDate;

// One row of the "issue_records" table: medicine given to a resident.
// medicineName is filled in when reading (SQL JOIN) and ignored when saving.
public record IssueRecord(
        Integer id,
        Integer medicineId,
        String medicineName,
        Integer residentId,
        Integer quantity,
        LocalDate issueDate,
        String issuedBy) {
}
