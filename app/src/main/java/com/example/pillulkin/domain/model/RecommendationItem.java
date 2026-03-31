package com.example.pillulkin.domain.model;

public class RecommendationItem {
    private long medicineId;
    private String medicineName;
    private String dosage;
    private String expirationDate;
    private boolean isExpired;
    private String recommendationReason;
    private String warning;

    public RecommendationItem(long medicineId, String medicineName, String dosage, String expirationDate,
                             boolean isExpired, String recommendationReason, String warning) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.expirationDate = expirationDate;
        this.isExpired = isExpired;
        this.recommendationReason = recommendationReason;
        this.warning = warning;
    }

    public long getMedicineId() { return medicineId; }
    public String getMedicineName() { return medicineName; }
    public String getDosage() { return dosage; }
    public String getExpirationDate() { return expirationDate; }
    public boolean isExpired() { return isExpired; }
    public String getRecommendationReason() { return recommendationReason; }
    public String getWarning() { return warning; }
}
