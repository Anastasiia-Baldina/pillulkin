package com.example.pillulkin.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "medicines")
public class MedicineEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private String dosage;
    private String expirationDate;
    private int quantity;
    private String form;
    private String comment;

    public MedicineEntity(String name, String dosage, String expirationDate, int quantity, String form, String comment) {
        this.name = name;
        this.dosage = dosage;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
        this.form = form;
        this.comment = comment;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getForm() { return form; }
    public void setForm(String form) { this.form = form; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
