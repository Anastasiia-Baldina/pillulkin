package com.example.pillulkin.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "symptoms")
public class SymptomEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String description;
    private long timestamp;

    public SymptomEntity(String description, long timestamp) {
        this.description = description;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
