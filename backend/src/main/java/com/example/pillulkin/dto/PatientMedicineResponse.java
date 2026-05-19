package com.example.pillulkin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientMedicineResponse {

    private Long id;
    private Long patientId;
    private Long medicineId;
    private String medicineName;
    private String dosage;
    private String form;
    private String activeSubstance;
    private LocalDateTime addedAt;
    private LocalDate expirationDate;
    private String quantity;
}