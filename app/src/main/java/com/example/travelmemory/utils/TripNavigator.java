package com.example.travelmemory.utils;

import android.content.Context;
import android.content.Intent;

import com.example.travelmemory.activity.TripDetailActivity;

public final class TripNavigator {

    public static void openTripDetail(Context context, int tripId, String tripMode) {
        Intent intent = new Intent(context, TripDetailActivity.class);
        intent.putExtra(TravelConstants.EXTRA_TRIP_ID, tripId);
        intent.putExtra(TravelConstants.EXTRA_MODE, tripMode);
        context.startActivity(intent);
    }

    public static void openFeature(Context context, Class<?> targetActivityClass, int tripId) {
        Intent intent = new Intent(context, targetActivityClass);
        intent.putExtra(TravelConstants.EXTRA_TRIP_ID, tripId);
        context.startActivity(intent);
    }

    public static boolean openReport(Context context, int tripId) {
        return TripReportAccess.openReport(context, tripId);
    }

    public static void openWithTripAndDate(Context context, Class<?> targetActivityClass, int tripId, String recordDate) {
        Intent intent = new Intent(context, targetActivityClass);
        intent.putExtra(TravelConstants.EXTRA_TRIP_ID, tripId);
        intent.putExtra(TravelConstants.EXTRA_RECORD_DATE, recordDate);
        context.startActivity(intent);
    }

    private TripNavigator() {
    }
}
