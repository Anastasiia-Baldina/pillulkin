package com.example.pillulkin.domain.model;

public class AnalogueDictionaryEntry {
    private long id;
    private String sourceName;
    private String analogueName;
    private String activeSubstance;

    public AnalogueDictionaryEntry(long id, String sourceName, String analogueName, String activeSubstance) {
        this.id = id;
        this.sourceName = sourceName;
        this.analogueName = analogueName;
        this.activeSubstance = activeSubstance;
    }

    public long getId() { return id; }
    public String getSourceName() { return sourceName; }
    public String getAnalogueName() { return analogueName; }
    public String getActiveSubstance() { return activeSubstance; }
}
