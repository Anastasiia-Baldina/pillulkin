package com.example.pillulkin.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cached_medicine")
public class CachedMedicine {

    @PrimaryKey
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "patient_id")
    private long patientId;

    @ColumnInfo(name = "medicine_id")
    private Long medicineId;

    @ColumnInfo(name = "medicine_name")
    private String medicineName;

    @ColumnInfo(name = "dosage")
    private String dosage;

    @ColumnInfo(name = "form")
    private String form;

    @ColumnInfo(name = "added_at")
    private String addedAt;

    @ColumnInfo(name = "expiration_date")
    private String expirationDate;

    @ColumnInfo(name = "quantity")
    private String quantity;

    @ColumnInfo(name = "active_substance")
    private String activeSubstance;

    @ColumnInfo(name = "cached_at")
    private long cachedAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getPatientId() { return patientId; }
    public void setPatientId(long patientId) { this.patientId = patientId; }
    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getForm() { return form; }
    public void setForm(String form) { this.form = form; }
    public String getAddedAt() { return addedAt; }
    public void setAddedAt(String addedAt) { this.addedAt = addedAt; }
    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public String getActiveSubstance() { return activeSubstance; }
    public void setActiveSubstance(String activeSubstance) { this.activeSubstance = activeSubstance; }
    public long getCachedAt() { return cachedAt; }
    public void setCachedAt(long cachedAt) { this.cachedAt = cachedAt; }
}
