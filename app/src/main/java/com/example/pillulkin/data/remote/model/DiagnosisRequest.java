package com.example.pillulkin.data.remote.model;

import java.util.List;

public class DiagnosisRequest {
    private List<Integer> symptoms;

    public DiagnosisRequest(List<Integer> symptoms) {
        this.symptoms = symptoms;
    }

    public List<Integer> getSymptoms() {
        return symptoms;
    }
}
