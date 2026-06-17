package com.example.travelmemory.model;

public class SearchResult {
    private int tripId;
    private String title;
    private String subtitle;
    private String sourceType;
    private String dateText;

    public SearchResult(int tripId, String title, String subtitle, String sourceType, String dateText) {
        this.tripId = tripId;
        this.title = title;
        this.subtitle = subtitle;
        this.sourceType = sourceType;
        this.dateText = dateText;
    }

    public int getTripId() { return tripId; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getSourceType() { return sourceType; }
    public String getDateText() { return dateText; }
}
