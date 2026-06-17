package com.example.travelmemory.model;

public class PrePlan {
    private int id;
    private int tripId;
    private String type;
    private String title;
    private String location;
    private String targetDate;
    private String note;
    private int isCompleted;
    private String createdAt;

    public PrePlan() {}

    public PrePlan(int id, int tripId, String title, String type, String note, String targetDate, int isCompleted, String createdAt) {
        this(id, tripId, type, title, "", targetDate, note, isCompleted, createdAt);
    }

    public PrePlan(int id, int tripId, String type, String title, String location, String targetDate, String note, int isCompleted, String createdAt) {
        this.id = id;
        this.tripId = tripId;
        this.type = type;
        this.title = title;
        this.location = location;
        this.targetDate = targetDate;
        this.note = note;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getTargetDate() { return targetDate; }
    public void setTargetDate(String targetDate) { this.targetDate = targetDate; }
    public int getIsCompleted() { return isCompleted; }
    public void setIsCompleted(int isCompleted) { this.isCompleted = isCompleted; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
