package com.example.pillulkin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientMedicineRequest {

    @NotNull(message = "Medicine ID is required")
    private Long medicineId;

    private LocalDate expirationDate;

    private String quantity;
}