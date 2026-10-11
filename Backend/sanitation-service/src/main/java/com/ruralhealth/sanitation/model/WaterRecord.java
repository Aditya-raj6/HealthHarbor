package com.ruralhealth.sanitation.model;

import java.time.LocalDate;

// One row of the "water_records" table (water supply / quality observation)
public record WaterRecord(
        Integer id,
        String village,
        String source,
        String qualityStatus,
        Double phValue,
        String observation,
        String issue,
        String correctiveAction,
        String actionStatus,
        LocalDate recordedOn) {
}
