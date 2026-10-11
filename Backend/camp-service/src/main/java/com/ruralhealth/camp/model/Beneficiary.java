package com.ruralhealth.camp.model;

import java.time.LocalDate;

// One row of the "camp_beneficiaries" table: a person screened at a camp.
// campName is filled in when reading (SQL JOIN) and ignored when saving.
public record Beneficiary(
        Integer id,
        Integer campId,
        String campName,
        Integer residentId,
        String residentName,
        String screeningType,
        String result,
        Boolean followUpNeeded,
        LocalDate followUpDate,
        String followUpStatus) {
}
