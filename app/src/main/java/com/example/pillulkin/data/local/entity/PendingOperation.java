package com.example.pillulkin.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pending_operation")
public class PendingOperation {

    public static final String TYPE_PROFILE_UPDATE = "PROFILE_UPDATE";
    public static final String TYPE_SYMPTOM_ADD = "SYMPTOM_ADD";
    public static final String TYPE_SYMPTOM_DELETE = "SYMPTOM_DELETE";
    public static final String TYPE_MEDICINE_ADD = "MEDICINE_ADD";
    public static final String TYPE_MEDICINE_DELETE = "MEDICINE_DELETE";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SYNCING = "SYNCING";
    public static final String STATUS_FAILED = "FAILED";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "payload")
    private String payload;

    @ColumnInfo(name = "entity_id")
    private Long entityId;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "retry_count")
    private int retryCount;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
