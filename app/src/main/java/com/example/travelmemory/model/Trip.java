package com.example.travelmemory.model;

import com.example.travelmemory.utils.TravelConstants;

public class Trip {
    private int id;
    private int userId;
    private String name;
    private String destination;
    private String startDate;
    private String endDate;
    private String coverPath;
    private String mode;
    private String status;
    private String createdAt;

    public Trip() {}

    public Trip(int id, String name, String destination, String startDate, String endDate, String coverPath, String createdAt) {
        this.id = id;
        this.userId = 0;
        this.name = name;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.coverPath = coverPath;
        this.mode = TravelConstants.TripMode.SIMPLE;
        this.status = TravelConstants.TripState.ACTIVE;
        this.createdAt = createdAt;
    }

    public Trip(int id, int userId, String name, String destination, String startDate, String endDate, String coverPath, String createdAt) {
        this(id, userId, name, destination, startDate, endDate, coverPath, TravelConstants.TripMode.SIMPLE, TravelConstants.TripState.ACTIVE, createdAt);
    }

    public Trip(int id, int userId, String name, String destination, String startDate, String endDate, String coverPath, String mode, String status, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.coverPath = coverPath;
        this.mode = mode;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
