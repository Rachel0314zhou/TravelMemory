package com.example.travelmemory.model;

public class RegretItem {
    private int id;
    private int tripId;
    private int prePlanId;
    private String title;
    private String type;
    private String reason;
    private String createdAt;

    public RegretItem() {}

    public RegretItem(int id, int tripId, int prePlanId, String title, String type, String reason, String createdAt) {
        this.id = id;
        this.tripId = tripId;
        this.prePlanId = prePlanId;
        this.title = title;
        this.type = type;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }
    public int getPrePlanId() { return prePlanId; }
    public void setPrePlanId(int prePlanId) { this.prePlanId = prePlanId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
