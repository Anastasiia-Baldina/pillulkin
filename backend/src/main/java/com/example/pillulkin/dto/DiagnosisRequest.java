package com.example.pillulkin.dto;

import jakarta.validation.constraints.NotEmpty;
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
    private List<String> symptoms;

    private String step;

    private List<String> answers;
}
