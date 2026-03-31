package com.example.pillulkin.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "analogue_dictionary")
public class AnalogueDictionaryEntryEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String sourceName;
    private String analogueName;
    private String activeSubstance;

    public AnalogueDictionaryEntryEntity(String sourceName, String analogueName, String activeSubstance) {
        this.sourceName = sourceName;
        this.analogueName = analogueName;
        this.activeSubstance = activeSubstance;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getAnalogueName() { return analogueName; }
    public void setAnalogueName(String analogueName) { this.analogueName = analogueName; }
    public String getActiveSubstance() { return activeSubstance; }
    public void setActiveSubstance(String activeSubstance) { this.activeSubstance = activeSubstance; }
}
