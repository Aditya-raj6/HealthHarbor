package com.ruralhealth.camp.model;

import java.time.LocalDate;

// One row of the "camps" table (a health camp)
public record Camp(
        Integer id,
        String name,
        String village,
        LocalDate campDate,
        Integer centreId,
        String description,
        String status) {
}
