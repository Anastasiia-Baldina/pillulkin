package com.example.pillulkin.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisRequest {

    @NotEmpty(message = "Symptoms list must not be empty")
    @Size(min = 8, max = 8, message = "Exactly 8 symptom values required")
    private List<Integer> symptoms;
}
