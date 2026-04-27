package com.example.pillulkin.data.remote.model;

import java.util.List;

public class DiagnosisResponse {
    private String diagnosis;
    private double confidence;
    private List<String> suggestedQuestions;
    private boolean isFinal;

    public String getDiagnosis() { return diagnosis; }
    public double getConfidence() { return confidence; }
    public List<String> getSuggestedQuestions() { return suggestedQuestions; }
    public boolean isFinal() { return isFinal; }
}
