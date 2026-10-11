package com.ruralhealth.inventory.model;

import java.time.LocalDate;

// One row of the "medicines" table (medicines and medical supplies)
public record Medicine(
        Integer id,
        Integer centreId,
        String name,
        String category,
        Integer quantity,
        Integer minLevel,
        String unit,
        LocalDate expiryDate) {
}
