package com.example.pillulkin.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "doctor_access_codes")
public class DoctorAccessCodeEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String code;
    private long createdAt;
    private long expiresAt;
    private String status;

    public DoctorAccessCodeEntity(String code, long createdAt, long expiresAt, String status) {
        this.code = code;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = status;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
