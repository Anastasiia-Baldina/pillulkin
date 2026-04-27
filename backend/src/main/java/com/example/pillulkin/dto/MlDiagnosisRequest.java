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
public class MlDiagnosisRequest {
    private List<String> symptoms;
    private Integer top_k;
    private Integer max_questions;
}
