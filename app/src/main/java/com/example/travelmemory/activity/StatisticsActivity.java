package com.example.travelmemory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.utils.BottomNavHelper;
import com.example.travelmemory.utils.SessionManager;

public class StatisticsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        if (sessionManager.getCurrentUserId() <= 0) {
            goToLogin();
            return;
        }
        findViewById(R.id.btn_statistics_back).setOnClickListener(v -> finish());
        BottomNavHelper.setup(this, BottomNavHelper.TAB_STATISTICS);
        loadStatistics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }

    private void loadStatistics() {
        int userId = sessionManager.getCurrentUserId();
        if (userId <= 0) {
            goToLogin();
            return;
        }
        setText(R.id.tv_stat_trip_count, dbHelper.getTotalTripCount(userId) + " \u6b21");
        setText(R.id.tv_stat_travel_days, dbHelper.getTotalTravelDays(userId) + " \u5929");
        setText(R.id.tv_stat_photo_count, dbHelper.getTotalPhotoCount(userId) + " \u5f20");
        setText(R.id.tv_stat_spot_count, dbHelper.getTotalSpotCount(userId) + " \u4e2a");
        setText(R.id.tv_stat_total_expense, String.format("\u00a5%.2f", dbHelper.getTotalExpense(userId)));
        setText(R.id.tv_stat_common_mood, dbHelper.getMostCommonMood(userId));
        setText(R.id.tv_stat_recent_destination, dbHelper.getRecentDestination(userId));
        setText(R.id.tv_stat_plan_count, dbHelper.getTotalPrePlanCount(userId) + " \u4e2a");
        setText(R.id.tv_stat_completed_plan_count, dbHelper.getCompletedPrePlanCount(userId) + " \u4e2a");
        setText(R.id.tv_stat_avg_plan_rate, dbHelper.getAveragePlanCompletionRate(userId) + "%");
        setText(R.id.tv_stat_regret_count, dbHelper.countRegretsForUser(userId) + " \u4e2a");
        setText(R.id.tv_stat_avg_packing_rate, dbHelper.getAveragePackingCompletionRate(userId) + "%");
    }

    private void setText(int viewId, String text) {
        TextView textView = findViewById(viewId);
        if (textView != null) textView.setText(text);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
