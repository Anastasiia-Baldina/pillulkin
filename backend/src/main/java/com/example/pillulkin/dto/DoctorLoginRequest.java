package com.example.pillulkin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DoctorLoginRequest {

    @NotBlank(message = "Code is required")
    private String code;
}