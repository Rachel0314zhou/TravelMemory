package com.example.travelmemory.utils;

public final class TravelConstants {
    public static final String EXTRA_TRIP_ID = "trip_id";
    public static final String EXTRA_TRIP_MODE = "trip_mode";
    public static final String EXTRA_RECORD_DATE = "record_date";
    public static final String EXTRA_MODE = "mode";

    /**
     * Page entry modes. These describe which feature panel should open,
     * not the lifecycle state of a trip.
     */
    public static final String MODE_OVERVIEW = "overview";
    public static final String MODE_PRE_TRIP = "pre_trip";
    public static final String MODE_PACKING = "packing";
    public static final String MODE_DURING_TRIP = "during_trip";
    public static final String MODE_POST_TRIP = "post_trip";
    public static final String MODE_MEMORY = "memory";
    public static final String MODE_EXPENSE = "expense";
    public static final String MODE_DELETE_TRIP = "delete_trip";

    public static final class TripState {
        public static final String PLANNING = "pre_trip";
        public static final String ACTIVE = "during_trip";
        public static final String REVIEWING = "post_trip";

        private TripState() {
        }
    }

    public static final class TripMode {
        public static final String SIMPLE = "simple_record";
        public static final String PLANNED = "planned_record";

        private TripMode() {
        }
    }

    public static String formatTripStatus(String status) {
        if (TripState.PLANNING.equals(status)) return "琛屽墠鍑嗗";
        if (TripState.ACTIVE.equals(status)) return "鏃呰璁板綍";
        if (TripState.REVIEWING.equals(status)) return "鏃呰鏁寸悊";
        if ("finished".equals(status)) return "宸插畬鎴?";
        return "鏃呰璁板綍";
    }

    public static boolean isPlannedTrip(String mode) {
        return TripMode.PLANNED.equals(mode);
    }

    public static String formatTripStatusText(String status) {
        if (TripState.PLANNING.equals(status)) return "\u884c\u524d\u51c6\u5907";
        if (TripState.ACTIVE.equals(status)) return "\u65c5\u884c\u8bb0\u5f55";
        if (TripState.REVIEWING.equals(status)) return "\u65c5\u884c\u6574\u7406";
        if ("finished".equals(status)) return "\u5df2\u5b8c\u6210";
        return "\u65c5\u884c\u8bb0\u5f55";
    }

    private TravelConstants() {
    }
}
