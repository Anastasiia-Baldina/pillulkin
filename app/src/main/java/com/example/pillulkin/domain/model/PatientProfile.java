package com.example.pillulkin.domain.model;

public class PatientProfile {
    private long id;
    private String name;
    private int age;
    private String allergies;
    private String contraindications;
    private String notes;

    public PatientProfile(long id, String name, int age, String allergies, String contraindications, String notes) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.allergies = allergies;
        this.contraindications = contraindications;
        this.notes = notes;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    public String getContraindications() { return contraindications; }
    public void setContraindications(String contraindications) { this.contraindications = contraindications; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
