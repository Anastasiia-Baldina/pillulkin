package com.example.pillulkin.domain.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class RecommendationResultTest {
    @Test
    public void isEmpty_withEmptyList_returnsTrue() {
        RecommendationResult result = new RecommendationResult("query", "MEDICINE", java.util.Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    public void isEmpty_withNullList_returnsTrue() {
        RecommendationResult result = new RecommendationResult("query", "MEDICINE", null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void isEmpty_withNonEmptyList_returnsFalse() {
        RecommendationItem item = new RecommendationItem(1, "Парацетамол", "500 мг", "12.12.2025", false, "Причина", null);
        java.util.List<RecommendationItem> items = java.util.Collections.singletonList(item);
        RecommendationResult result = new RecommendationResult("query", "MEDICINE", items);
        assertFalse(result.isEmpty());
    }

    @Test
    public void getQuery_returnsCorrectValue() {
        RecommendationResult result = new RecommendationResult("test query", "DIAGNOSIS", java.util.Collections.emptyList());
        assertEquals("test query", result.getQuery());
    }

    @Test
    public void getQueryType_returnsCorrectValue() {
        RecommendationResult result = new RecommendationResult("query", "DIAGNOSIS", java.util.Collections.emptyList());
        assertEquals("DIAGNOSIS", result.getQueryType());
    }
}
