package com.example.pillulkin.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientMedicineRequest {

    private Long medicineId;

    private LocalDate expirationDate;

    private String quantity;
}