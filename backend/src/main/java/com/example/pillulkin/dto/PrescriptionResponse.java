package com.example.pillulkin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponse {

    private Long id;
    private Long patientId;
    private Long medicineId;
    private String medicineName;
    private String dosage;
    private String form;
    private String activeSubstance;
    private String dosageInstructions;
    private Integer frequency;
    private String mealTiming;
    private String timeOffset;
    private String customInstructions;
    private LocalDateTime prescribedAt;
    private String status;
}
