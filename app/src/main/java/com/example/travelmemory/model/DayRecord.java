package com.example.travelmemory.model;

public class DayRecord {
    private int id;
    private int tripId;
    private String date;
    private String note;
    private String mood;
    private String createdAt;

    public DayRecord() {}

    public DayRecord(int id, int tripId, String date, String note, String mood, String createdAt) {
        this.id = id;
        this.tripId = tripId;
        this.date = date;
        this.note = note;
        this.mood = mood;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
