package com.example.travelmemory.model;

public class PrePlan {
    public static final String STATUS_NORMAL = "normal";
    public static final String STATUS_PENDING = "pending";
    public static final String TIME_SECTION_MORNING = "上午";
    public static final String TIME_SECTION_AFTERNOON = "下午";
    public static final String TIME_SECTION_EVENING = "晚上";
    public static final String TIME_SECTION_PENDING = "待定";

    private int id;
    private int tripId;
    private int dayIndex;
    private String type;
    private String title;
    private String location;
    private String targetDate;
    private String timeText;
    private String timeSection;
    private String note;
    private int isCompleted;
    private String status;
    private String createdAt;

    public PrePlan() {}

    public PrePlan(int id, int tripId, String title, String type, String note, String targetDate, int isCompleted, String createdAt) {
        this(id, tripId, type, title, "", targetDate, "", note, isCompleted, createdAt);
    }

    public PrePlan(int id, int tripId, String type, String title, String location, String targetDate, String note, int isCompleted, String createdAt) {
        this(id, tripId, type, title, location, targetDate, "", note, isCompleted, STATUS_NORMAL, createdAt);
    }

    public PrePlan(int id, int tripId, String type, String title, String location, String targetDate, String timeText, String note, int isCompleted, String createdAt) {
        this(id, tripId, type, title, location, targetDate, timeText, note, isCompleted, STATUS_NORMAL, createdAt);
    }

    public PrePlan(int id, int tripId, String type, String title, String location, String targetDate, String timeText, String note, int isCompleted, String status, String createdAt) {
        this(id, tripId, 0, type, title, location, targetDate, timeText, TIME_SECTION_PENDING, note, isCompleted, status, createdAt);
    }

    public PrePlan(int id, int tripId, int dayIndex, String type, String title, String location, String targetDate, String timeText, String timeSection, String note, int isCompleted, String status, String createdAt) {
        this.id = id;
        this.tripId = tripId;
        this.dayIndex = Math.max(0, dayIndex);
        this.type = type;
        this.title = title;
        this.location = location;
        this.targetDate = targetDate;
        this.timeText = timeText;
        this.timeSection = normalizeTimeSection(timeSection);
        this.note = note;
        this.isCompleted = isCompleted;
        this.status = normalizeStatus(status);
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }
    public int getDayIndex() { return dayIndex; }
    public void setDayIndex(int dayIndex) { this.dayIndex = Math.max(0, dayIndex); }
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
    public String getTimeText() { return timeText; }
    public void setTimeText(String timeText) { this.timeText = timeText; }
    public String getTimeSection() { return normalizeTimeSection(timeSection); }
    public void setTimeSection(String timeSection) { this.timeSection = normalizeTimeSection(timeSection); }
    public int getIsCompleted() { return isCompleted; }
    public void setIsCompleted(int isCompleted) { this.isCompleted = isCompleted; }
    public String getStatus() { return normalizeStatus(status); }
    public void setStatus(String status) { this.status = normalizeStatus(status); }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isPending() {
        return STATUS_PENDING.equals(getStatus());
    }

    private String normalizeStatus(String value) {
        if (STATUS_PENDING.equals(value)) return STATUS_PENDING;
        return STATUS_NORMAL;
    }

    private String normalizeTimeSection(String value) {
        if (TIME_SECTION_MORNING.equals(value)) return TIME_SECTION_MORNING;
        if (TIME_SECTION_AFTERNOON.equals(value)) return TIME_SECTION_AFTERNOON;
        if (TIME_SECTION_EVENING.equals(value)) return TIME_SECTION_EVENING;
        return TIME_SECTION_PENDING;
    }
}
