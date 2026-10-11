package com.ruralhealth.resident.model;

import java.time.LocalDate;

// One row of the "visits" table
public class Visit {

    private Integer id;
    private Integer residentId;
    private Integer healthCentreId;
    private LocalDate visitDate;
    private String reason;
    private String diagnosis;
    private String treatment;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getResidentId() { return residentId; }
    public void setResidentId(Integer residentId) { this.residentId = residentId; }

    public Integer getHealthCentreId() { return healthCentreId; }
    public void setHealthCentreId(Integer healthCentreId) { this.healthCentreId = healthCentreId; }

    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }
}
