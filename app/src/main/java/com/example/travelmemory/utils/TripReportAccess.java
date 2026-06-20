package com.example.travelmemory.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.travelmemory.activity.TripReportActivity;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.Trip;

public final class TripReportAccess {

    private TripReportAccess() {}

    public static boolean canViewReport(DatabaseHelper dbHelper, int tripId, int userId) {
        if (dbHelper == null || tripId <= 0 || userId <= 0) return false;
        Trip trip = dbHelper.getTripForUser(tripId, userId);
        return trip != null && TravelConstants.TripState.REVIEWING.equals(trip.getStatus());
    }

    public static boolean openReport(Context context, int tripId) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SessionManager sessionManager = new SessionManager(context);
        int userId = sessionManager.getCurrentUserId();
        if (!canViewReport(dbHelper, tripId, userId)) {
            Toast.makeText(context, "进入旅行整理阶段后才能查看旅行报告", Toast.LENGTH_SHORT).show();
            return false;
        }
        Intent intent = new Intent(context, TripReportActivity.class);
        intent.putExtra(TravelConstants.EXTRA_TRIP_ID, tripId);
        intent.putExtra(TravelConstants.EXTRA_REPORT_OPENED_AT, System.currentTimeMillis());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
        return true;
    }
}
