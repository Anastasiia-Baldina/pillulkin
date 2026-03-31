package com.example.pillulkin.domain.model;

public class Medicine {
    private long id;
    private String name;
    private String dosage;
    private String expirationDate;
    private int quantity;
    private String form;
    private String comment;

    public Medicine(long id, String name, String dosage, String expirationDate, int quantity, String form, String comment) {
        this.id = id;
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
