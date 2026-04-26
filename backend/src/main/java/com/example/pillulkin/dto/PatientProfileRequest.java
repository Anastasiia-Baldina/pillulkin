package com.example.pillulkin.dto;

import lombok.Data;

@Data
public class PatientProfileRequest {

    private String name;
    private Integer age;
    private String allergies;
    private String contraindications;
    private String notes;
}