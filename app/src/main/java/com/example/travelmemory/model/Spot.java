package com.example.travelmemory.model;

public class Spot {
    private int id;
    private int tripId;
    private String name;
    private String type;
    private String note;
    private int isFavorite; // 1 for true, 0 for false
    private int sourcePlanId;
    private String createdAt;

    public Spot() {}

    public Spot(int id, int tripId, String name, String type, String note, int isFavorite, String createdAt) {
        this(id, tripId, name, type, note, isFavorite, 0, createdAt);
    }

    public Spot(int id, int tripId, String name, String type, String note, int isFavorite, int sourcePlanId, String createdAt) {
        this.id = id;
        this.tripId = tripId;
        this.name = name;
        this.type = type;
        this.note = note;
        this.isFavorite = isFavorite;
        this.sourcePlanId = sourcePlanId;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getIsFavorite() { return isFavorite; }
    public void setIsFavorite(int isFavorite) { this.isFavorite = isFavorite; }

    public int getSourcePlanId() { return sourcePlanId; }
    public void setSourcePlanId(int sourcePlanId) { this.sourcePlanId = sourcePlanId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
