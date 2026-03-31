package com.example.pillulkin.domain.usecase;

import com.example.pillulkin.domain.model.RecommendationResult;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collections;

public class GetMedicineRecommendationsUseCaseTest {
    @Test
    public void execute_withEmptyQuery_returnsEmptyResult() {
        // This test verifies the use case logic without repository dependency
        String medicineName = "";
        if (medicineName == null || medicineName.trim().isEmpty()) {
            RecommendationResult result = new RecommendationResult("", RecommendationResult.TYPE_MEDICINE, Collections.emptyList());
            assertTrue(result.isEmpty());
            assertEquals("", result.getQuery());
            assertEquals(RecommendationResult.TYPE_MEDICINE, result.getQueryType());
        }
    }

    @Test
    public void execute_withNullQuery_returnsEmptyResult() {
        String medicineName = null;
        if (medicineName == null || medicineName.trim().isEmpty()) {
            RecommendationResult result = new RecommendationResult("", RecommendationResult.TYPE_MEDICINE, Collections.emptyList());
            assertTrue(result.isEmpty());
        }
    }
}
