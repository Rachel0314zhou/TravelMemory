package com.example.travelmemory.model;

public class TimelineItem {
    public static final String TYPE_SPOT = "景点";
    public static final String TYPE_FOOD = "美食";
    public static final String TYPE_TRANSPORT = "交通";
    public static final String TYPE_SHOPPING = "购物";
    public static final String TYPE_HOTEL = "住宿";
    public static final String TYPE_OTHER = "其他";

    public static final String TIME_SLOT_MORNING = "上午";
    public static final String TIME_SLOT_AFTERNOON = "下午";
    public static final String TIME_SLOT_EVENING = "晚上";
    public static final String TIME_SLOT_PENDING = "待定";

    public static final String STATUS_PLANNED = "planned";
    public static final String STATUS_DONE = "done";
    public static final String STATUS_SKIPPED = "skipped";

    public static final String KIND_PLAN_FIXED = "plan_fixed";
    public static final String KIND_PLAN_PENDING = "plan_pending";
    public static final String KIND_CHECKIN_EXTRA = "checkin_extra";

    private int id;
    private int tripId;
    private int dayIndex;
    private String title;
    private String type;
    private String timeSlot;
    private String status;
    private String kind;
    private String checkinPhoto;
    private String checkinNote;
    private String checkinMood;
    private String checkinTime;
    private String createdAt;

    public TimelineItem() {
        this.type = TYPE_OTHER;
        this.timeSlot = TIME_SLOT_PENDING;
        this.status = STATUS_PLANNED;
        this.kind = KIND_PLAN_FIXED;
    }

    public TimelineItem(int id, int tripId, int dayIndex, String title, String type, String timeSlot,
                        String status, String checkinPhoto, String checkinNote, String checkinMood,
                        String checkinTime, String createdAt) {
        this(id, tripId, dayIndex, title, type, timeSlot, status, KIND_PLAN_FIXED, checkinPhoto,
                checkinNote, checkinMood, checkinTime, createdAt);
    }

    public TimelineItem(int id, int tripId, int dayIndex, String title, String type, String timeSlot,
                        String status, String kind, String checkinPhoto, String checkinNote, String checkinMood,
                        String checkinTime, String createdAt) {
        this.id = id;
        this.tripId = tripId;
        this.dayIndex = Math.max(0, dayIndex);
        this.title = title;
        this.type = normalizeType(type);
        this.timeSlot = normalizeTimeSlot(timeSlot);
        this.status = normalizeStatus(status);
        this.kind = normalizeKind(kind);
        this.checkinPhoto = checkinPhoto;
        this.checkinNote = checkinNote;
        this.checkinMood = checkinMood;
        this.checkinTime = checkinTime;
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
    public String getType() { return normalizeType(type); }
    public void setType(String type) { this.type = normalizeType(type); }
    public String getTimeSlot() { return normalizeTimeSlot(timeSlot); }
    public void setTimeSlot(String timeSlot) { this.timeSlot = normalizeTimeSlot(timeSlot); }
    public String getStatus() { return normalizeStatus(status); }
    public void setStatus(String status) { this.status = normalizeStatus(status); }
    public String getKind() { return normalizeKind(kind); }
    public void setKind(String kind) { this.kind = normalizeKind(kind); }
    public String getCheckinPhoto() { return checkinPhoto; }
    public void setCheckinPhoto(String checkinPhoto) { this.checkinPhoto = checkinPhoto; }
    public String getCheckinNote() { return checkinNote; }
    public void setCheckinNote(String checkinNote) { this.checkinNote = checkinNote; }
    public String getCheckinMood() { return checkinMood; }
    public void setCheckinMood(String checkinMood) { this.checkinMood = checkinMood; }
    public String getCheckinTime() { return checkinTime; }
    public void setCheckinTime(String checkinTime) { this.checkinTime = checkinTime; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isDone() {
        return STATUS_DONE.equals(getStatus());
    }

    public boolean isSkipped() {
        return STATUS_SKIPPED.equals(getStatus());
    }

    public boolean isPlanItem() {
        return KIND_PLAN_FIXED.equals(getKind()) || KIND_PLAN_PENDING.equals(getKind());
    }

    public boolean isFixedPlan() {
        return KIND_PLAN_FIXED.equals(getKind());
    }

    public boolean isPendingPlan() {
        return KIND_PLAN_PENDING.equals(getKind());
    }

    public boolean isExtraCheckin() {
        return KIND_CHECKIN_EXTRA.equals(getKind());
    }

    private String normalizeType(String value) {
        if (TYPE_SPOT.equals(value)) return TYPE_SPOT;
        if (TYPE_FOOD.equals(value)) return TYPE_FOOD;
        if (TYPE_TRANSPORT.equals(value)) return TYPE_TRANSPORT;
        if (TYPE_SHOPPING.equals(value)) return TYPE_SHOPPING;
        if (TYPE_HOTEL.equals(value)) return TYPE_HOTEL;
        return TYPE_OTHER;
    }

    private String normalizeTimeSlot(String value) {
        if (value != null && value.matches("\\d{2}:\\d{2}")) return value;
        if (TIME_SLOT_MORNING.equals(value)) return TIME_SLOT_MORNING;
        if (TIME_SLOT_AFTERNOON.equals(value)) return TIME_SLOT_AFTERNOON;
        if (TIME_SLOT_EVENING.equals(value)) return TIME_SLOT_EVENING;
        return TIME_SLOT_PENDING;
    }

    private String normalizeStatus(String value) {
        if (STATUS_DONE.equals(value)) return STATUS_DONE;
        if (STATUS_SKIPPED.equals(value)) return STATUS_SKIPPED;
        return STATUS_PLANNED;
    }

    private String normalizeKind(String value) {
        if (KIND_PLAN_PENDING.equals(value)) return KIND_PLAN_PENDING;
        if (KIND_CHECKIN_EXTRA.equals(value)) return KIND_CHECKIN_EXTRA;
        return KIND_PLAN_FIXED;
    }
}
