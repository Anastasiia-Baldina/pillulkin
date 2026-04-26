package com.example.pillulkin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceMedicineResponse {

    private Long id;
    private String name;
    private String dosage;
    private String form;
    private String activeSubstance;
    private String indications;
    private String contraindications;
    private String category;
}