package com.ruralhealth.healthcentre.model;

import java.time.LocalDate;
import java.time.LocalTime;

// One row of the "appointments" table.
// centreName and workerName are filled in when we read data (SQL JOIN); they are ignored when saving.
public record Appointment(
        Integer id,
        Integer residentId,
        Integer centreId,
        Integer workerId,
        LocalDate appointmentDate,
        LocalTime appointmentTime,
        String reason,
        String status,
        String centreName,
        String workerName) {
}
