package com.ruralhealth.camp.model;

// One row of the "camp_staff" table (medical staff taking part in a camp)
public record CampStaff(
        Integer id,
        Integer campId,
        String staffName,
        String role) {
}
