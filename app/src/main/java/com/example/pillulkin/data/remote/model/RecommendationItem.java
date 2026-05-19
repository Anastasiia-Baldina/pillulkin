package com.example.pillulkin.data.remote.model;

public class RecommendationItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_MEDICINE = 1;
    public static final int TYPE_ADD_CUSTOM = 2;

    private final int type;
    private String headerText;
    private ReferenceMedicineResponse medicine;
    private boolean fromCabinet;

    private RecommendationItem(int type) {
        this.type = type;
    }

    public static RecommendationItem header(String text) {
        RecommendationItem item = new RecommendationItem(TYPE_HEADER);
        item.headerText = text;
        return item;
    }

    public static RecommendationItem medicine(ReferenceMedicineResponse med, boolean fromCabinet) {
        RecommendationItem item = new RecommendationItem(TYPE_MEDICINE);
        item.medicine = med;
        item.fromCabinet = fromCabinet;
        return item;
    }

    public static RecommendationItem customButton() {
        return new RecommendationItem(TYPE_ADD_CUSTOM);
    }

    public int getType() { return type; }
    public String getHeaderText() { return headerText; }
    public ReferenceMedicineResponse getMedicine() { return medicine; }
    public boolean isFromCabinet() { return fromCabinet; }
}
