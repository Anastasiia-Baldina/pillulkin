package com.example.pillulkin.domain.usecase;

import com.example.pillulkin.data.repository.RecommendationRepository;
import com.example.pillulkin.domain.model.RecommendationResult;

import java.util.Collections;

public class GetMedicineRecommendationsUseCase {
    private final RecommendationRepository repository;

    public GetMedicineRecommendationsUseCase(RecommendationRepository repository) {
        this.repository = repository;
    }

    public RecommendationResult execute(String medicineName) {
        if (medicineName == null || medicineName.trim().isEmpty()) {
            return new RecommendationResult("", RecommendationResult.TYPE_MEDICINE, Collections.emptyList());
        }
        return repository.findByMedicineName(medicineName.trim());
    }
}
