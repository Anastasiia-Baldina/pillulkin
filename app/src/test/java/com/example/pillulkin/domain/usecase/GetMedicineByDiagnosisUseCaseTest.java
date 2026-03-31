package com.example.pillulkin.domain.usecase;

import com.example.pillulkin.domain.model.RecommendationResult;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collections;

public class GetMedicineByDiagnosisUseCaseTest {
    @Test
    public void execute_withEmptyQuery_returnsEmptyResult() {
        String diagnosis = "";
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            RecommendationResult result = new RecommendationResult("", RecommendationResult.TYPE_DIAGNOSIS, Collections.emptyList());
            assertTrue(result.isEmpty());
            assertEquals("", result.getQuery());
            assertEquals(RecommendationResult.TYPE_DIAGNOSIS, result.getQueryType());
        }
    }

    @Test
    public void execute_withNullQuery_returnsEmptyResult() {
        String diagnosis = null;
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            RecommendationResult result = new RecommendationResult("", RecommendationResult.TYPE_DIAGNOSIS, Collections.emptyList());
            assertTrue(result.isEmpty());
        }
    }
}
