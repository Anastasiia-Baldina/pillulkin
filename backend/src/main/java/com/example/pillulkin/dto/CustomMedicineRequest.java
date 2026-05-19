package com.example.pillulkin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomMedicineRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String dosage;
    private String form;
    private String activeSubstance;
    private String indications;
    private String contraindications;
    private LocalDate expirationDate;
    private String quantity;
}