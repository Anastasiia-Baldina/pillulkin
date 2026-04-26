package com.example.pillulkin.data.remote.model;

public class PrescriptionRequest {
    private Long medicineId;
    private String dosageInstructions;
    private Integer frequency;
    private String mealTiming;
    private String timeOffset;
    private String customInstructions;

    public PrescriptionRequest(Long medicineId, String dosageInstructions, Integer frequency,
                               String mealTiming, String timeOffset, String customInstructions) {
        this.medicineId = medicineId;
        this.dosageInstructions = dosageInstructions;
        this.frequency = frequency;
        this.mealTiming = mealTiming;
        this.timeOffset = timeOffset;
        this.customInstructions = customInstructions;
    }

    public Long getMedicineId() { return medicineId; }
    public String getDosageInstructions() { return dosageInstructions; }
    public Integer getFrequency() { return frequency; }
    public String getMealTiming() { return mealTiming; }
    public String getTimeOffset() { return timeOffset; }
    public String getCustomInstructions() { return customInstructions; }
}
