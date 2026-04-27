package com.example.pillulkin.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminder")
public class Reminder {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "hour")
    private int hour;

    @ColumnInfo(name = "minute")
    private int minute;

    @ColumnInfo(name = "medicine_name")
    private String medicineName;

    @ColumnInfo(name = "custom_text")
    private String customText;

    @ColumnInfo(name = "is_enabled")
    private boolean enabled;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }
    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getCustomText() { return customText; }
    public void setCustomText(String customText) { this.customText = customText; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @NonNull
    public String getDisplayText() {
        if (customText != null && !customText.isEmpty()) return customText;
        if (medicineName != null && !medicineName.isEmpty())
            return "Не забудьте принять " + medicineName;
        return "Не забудьте принять лекарство";
    }

    @NonNull
    public String getTimeFormatted() {
        return String.format("%02d:%02d", hour, minute);
    }
}
