package com.example.pillulkin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DoctorGenerateCodeRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private Integer expiresInMinutes = 60;
}