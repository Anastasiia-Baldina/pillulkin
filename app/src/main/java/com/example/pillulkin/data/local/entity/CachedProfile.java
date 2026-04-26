package com.example.pillulkin.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cached_profile")
public class CachedProfile {

    @PrimaryKey
    @ColumnInfo(name = "patient_id")
    private long patientId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "age")
    private Integer age;

    @ColumnInfo(name = "allergies")
    private String allergies;

    @ColumnInfo(name = "contraindications")
    private String contraindications;

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "cached_at")
    private long cachedAt;

    public long getPatientId() { return patientId; }
    public void setPatientId(long patientId) { this.patientId = patientId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    public String getContraindications() { return contraindications; }
    public void setContraindications(String contraindications) { this.contraindications = contraindications; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public long getCachedAt() { return cachedAt; }
    public void setCachedAt(long cachedAt) { this.cachedAt = cachedAt; }
}
