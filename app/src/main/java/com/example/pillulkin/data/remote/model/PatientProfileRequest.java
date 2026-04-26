package com.example.pillulkin.data.remote.model;

public class PatientProfileRequest {
    private String name;
    private Integer age;
    private String allergies;
    private String contraindications;
    private String notes;

    public PatientProfileRequest() {}

    public PatientProfileRequest(String name, Integer age, String allergies, String contraindications, String notes) {
        this.name = name;
        this.age = age;
        this.allergies = allergies;
        this.contraindications = contraindications;
        this.notes = notes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getContraindications() {
        return contraindications;
    }

    public void setContraindications(String contraindications) {
        this.contraindications = contraindications;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
