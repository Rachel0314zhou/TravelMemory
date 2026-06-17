package com.example.travelmemory.utils;

import android.content.Context;

import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.Trip;

public class TripStateManager {
    private final DatabaseHelper dbHelper;

    public TripStateManager(Context context) {
        this(new DatabaseHelper(context));
    }

    public TripStateManager(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void startTrip(int tripId) {
        dbHelper.updateTripStatus(tripId, TravelConstants.TripState.ACTIVE);
    }

    public void finishTrip(int tripId) {
        dbHelper.updateTripStatus(tripId, TravelConstants.TripState.REVIEWING);
    }

    public boolean isPlanning(Trip trip) {
        return trip != null && TravelConstants.TripState.PLANNING.equals(trip.getStatus());
    }

    public boolean isActive(Trip trip) {
        return trip != null && TravelConstants.TripState.ACTIVE.equals(trip.getStatus());
    }

    public boolean isReviewing(Trip trip) {
        return trip != null && TravelConstants.TripState.REVIEWING.equals(trip.getStatus());
    }

    public boolean canOpenPreTrip(Trip trip) {
        return trip != null && TravelConstants.isPlannedTrip(trip.getMode());
    }

    public boolean canOpenDuringTrip(Trip trip) {
        return trip != null && !isReviewing(trip);
    }

    public boolean canOpenPostTrip(Trip trip) {
        return isReviewing(trip);
    }
}
