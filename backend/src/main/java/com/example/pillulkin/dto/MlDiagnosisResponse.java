package com.example.pillulkin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MlDiagnosisResponse {

    private List<Prediction> predictions;
    private List<String> unknown_symptoms;
    private List<String> suggested_questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Prediction {
        private String disease;
        private double probability;
    }
}
