package com.example.pillulkin.data.remote.model;

public class PatientMedicineResponse {
    private Long id;
    private Long patientId;
    private Long medicineId;
    private String medicineName;
    private String dosage;
    private String form;
    private String activeSubstance;
    private String addedAt;
    private String expirationDate;
    private String quantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getForm() { return form; }
    public void setForm(String form) { this.form = form; }

    public String getActiveSubstance() { return activeSubstance; }
    public void setActiveSubstance(String activeSubstance) { this.activeSubstance = activeSubstance; }

    public String getAddedAt() { return addedAt; }
    public void setAddedAt(String addedAt) { this.addedAt = addedAt; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
}
