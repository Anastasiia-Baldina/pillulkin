package com.example.pillulkin.data.remote.model;

import java.util.List;

public class DiagnosisRequest {
    private List<String> symptoms;
    private String step;
    private List<String> answers;

    public DiagnosisRequest(List<String> symptoms, String step, List<String> answers) {
        this.symptoms = symptoms;
        this.step = step;
        this.answers = answers;
    }

    public List<String> getSymptoms() { return symptoms; }
    public String getStep() { return step; }
    public List<String> getAnswers() { return answers; }
}
