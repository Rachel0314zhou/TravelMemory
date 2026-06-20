package com.example.travelmemory.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.R;
import com.example.travelmemory.activity.MainActivity;
import com.example.travelmemory.activity.MemoryListActivity;
import com.example.travelmemory.activity.SearchActivity;
import com.example.travelmemory.activity.SettingsActivity;

public class BottomNavHelper {

    public static final int TAB_HOME = 1;
    public static final int TAB_SEARCH = 2;
    public static final int TAB_MEMORY = 3;
    public static final int TAB_SETTINGS = 4;

    public static final int TAB_TRIP = TAB_HOME;
    public static final int TAB_PROFILE = TAB_SETTINGS;

    private BottomNavHelper() {}

    public static void setup(Activity activity, int activeTab) {
        bind(activity, R.id.bottom_nav_trip, MainActivity.class, activeTab == TAB_HOME);
        bind(activity, R.id.bottom_nav_search, SearchActivity.class, activeTab == TAB_SEARCH);
        bind(activity, R.id.bottom_nav_memory, MemoryListActivity.class, activeTab == TAB_MEMORY);
        bind(activity, R.id.bottom_nav_profile, SettingsActivity.class, activeTab == TAB_SETTINGS);
    }

    private static void bind(Activity activity, int viewId, Class<?> target, boolean active) {
        View view = activity.findViewById(viewId);
        if (view == null) return;
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(ContextCompat.getColor(activity, active ? R.color.primary : R.color.text_secondary));
            textView.setBackground(ContextCompat.getDrawable(activity, active ? R.drawable.card_rounded_primary_light : R.drawable.card_rounded_white));
        }
        view.setOnClickListener(v -> {
            if (activity.getClass().equals(target)) return;
            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);
        });
    }
}
