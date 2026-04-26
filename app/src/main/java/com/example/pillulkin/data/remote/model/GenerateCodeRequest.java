package com.example.pillulkin.data.remote.model;

public class GenerateCodeRequest {
    private Long patientId;
    private Integer expiresInMinutes;

    public GenerateCodeRequest(Long patientId, Integer expiresInMinutes) {
        this.patientId = patientId;
        this.expiresInMinutes = expiresInMinutes;
    }
}
