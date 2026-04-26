package com.example.pillulkin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRequest {

    private Long medicineId;
    private String dosageInstructions;
    private Integer frequency;
    private String mealTiming;
    private String timeOffset;
    private String customInstructions;
}
