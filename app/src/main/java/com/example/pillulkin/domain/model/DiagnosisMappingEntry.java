package com.example.pillulkin.domain.model;

public class DiagnosisMappingEntry {
    private long id;
    private String diagnosisName;
    private String medicineName;
    private String activeSubstance;

    public DiagnosisMappingEntry(long id, String diagnosisName, String medicineName, String activeSubstance) {
        this.id = id;
        this.diagnosisName = diagnosisName;
        this.medicineName = medicineName;
        this.activeSubstance = activeSubstance;
    }

    public long getId() { return id; }
    public String getDiagnosisName() { return diagnosisName; }
    public String getMedicineName() { return medicineName; }
    public String getActiveSubstance() { return activeSubstance; }
}
