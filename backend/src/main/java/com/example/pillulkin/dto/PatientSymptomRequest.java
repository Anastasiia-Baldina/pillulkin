package com.example.pillulkin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PatientSymptomRequest {

    @NotBlank(message = "Symptom is required")
    private String symptom;
}