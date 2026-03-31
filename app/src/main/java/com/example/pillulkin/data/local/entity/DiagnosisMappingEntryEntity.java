package com.example.pillulkin.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "diagnosis_mapping")
public class DiagnosisMappingEntryEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String diagnosisName;
    private String medicineName;
    private String activeSubstance;

    public DiagnosisMappingEntryEntity(String diagnosisName, String medicineName, String activeSubstance) {
        this.diagnosisName = diagnosisName;
        this.medicineName = medicineName;
        this.activeSubstance = activeSubstance;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getDiagnosisName() { return diagnosisName; }
    public void setDiagnosisName(String diagnosisName) { this.diagnosisName = diagnosisName; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getActiveSubstance() { return activeSubstance; }
    public void setActiveSubstance(String activeSubstance) { this.activeSubstance = activeSubstance; }
}
