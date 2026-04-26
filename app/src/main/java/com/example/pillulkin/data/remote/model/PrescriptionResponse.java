package com.example.pillulkin.data.remote.model;

public class PrescriptionResponse {
    private Long id;
    private Long patientId;
    private Long medicineId;
    private String medicineName;
    private String dosage;
    private String form;
    private String activeSubstance;
    private String dosageInstructions;
    private Integer frequency;
    private String mealTiming;
    private String timeOffset;
    private String customInstructions;
    private String prescribedAt;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientId() { return patientId; }
    public Long getMedicineId() { return medicineId; }
    public String getMedicineName() { return medicineName; }
    public String getDosage() { return dosage; }
    public String getForm() { return form; }
    public String getActiveSubstance() { return activeSubstance; }
    public String getDosageInstructions() { return dosageInstructions; }
    public Integer getFrequency() { return frequency; }
    public String getMealTiming() { return mealTiming; }
    public String getTimeOffset() { return timeOffset; }
    public String getCustomInstructions() { return customInstructions; }
    public String getPrescribedAt() { return prescribedAt; }
    public String getStatus() { return status; }
}
