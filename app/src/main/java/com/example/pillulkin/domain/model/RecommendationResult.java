package com.example.pillulkin.domain.model;

import java.util.List;

public class RecommendationResult {
    private String query;
    private String queryType;
    private List<RecommendationItem> items;

    public static final String TYPE_MEDICINE = "MEDICINE";
    public static final String TYPE_DIAGNOSIS = "DIAGNOSIS";

    public RecommendationResult(String query, String queryType, List<RecommendationItem> items) {
        this.query = query;
        this.queryType = queryType;
        this.items = items;
    }

    public String getQuery() { return query; }
    public String getQueryType() { return queryType; }
    public List<RecommendationItem> getItems() { return items; }
    public boolean isEmpty() { return items == null || items.isEmpty(); }
}
