package com.example.travelmemory.model;

public class Photo {
    private int id;
    private int tripId;
    private int dayRecordId;
    private String imagePath;
    private String createdAt;

    public Photo() {}

    public Photo(int id, int tripId, int dayRecordId, String imagePath, String createdAt) {
        this.id = id;
        this.tripId = tripId;
        this.dayRecordId = dayRecordId;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public int getDayRecordId() { return dayRecordId; }
    public void setDayRecordId(int dayRecordId) { this.dayRecordId = dayRecordId; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
