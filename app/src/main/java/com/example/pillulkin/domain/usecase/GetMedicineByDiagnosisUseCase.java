package com.example.pillulkin.domain.usecase;

import com.example.pillulkin.data.repository.RecommendationRepository;
import com.example.pillulkin.domain.model.RecommendationResult;

import java.util.Collections;

public class GetMedicineByDiagnosisUseCase {
    private final RecommendationRepository repository;

    public GetMedicineByDiagnosisUseCase(RecommendationRepository repository) {
        this.repository = repository;
    }

    public RecommendationResult execute(String diagnosis) {
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            return new RecommendationResult("", RecommendationResult.TYPE_DIAGNOSIS, Collections.emptyList());
        }
        return repository.findByDiagnosis(diagnosis.trim());
    }
}
