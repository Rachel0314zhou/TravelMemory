package com.example.travelmemory.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.PrePlan;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.BottomNavHelper;
import com.example.travelmemory.utils.DateUtils;
import com.example.travelmemory.utils.DrawerHelper;
import com.example.travelmemory.utils.SessionManager;

import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int currentUserId = -1;
    private int selectedTripId = -1;
    private LinearLayout layoutPlanTimeline;
    private TextView emptyState;
    private TextView tvPlanTripTitle;
    private TextView tvPlanTripDates;
    private DrawerHelper drawerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        if (currentUserId <= 0) {
            goToLogin();
            return;
        }
        drawerHelper = new DrawerHelper(this);
        drawerHelper.setup();
        tvPlanTripTitle = findViewById(R.id.tv_plan_trip_title);
        tvPlanTripDates = findViewById(R.id.tv_plan_trip_dates);
        layoutPlanTimeline = findViewById(R.id.layout_plan_timeline);
        emptyState = findViewById(R.id.search_empty_view);
        findViewById(R.id.btn_search_back).setOnClickListener(v -> drawerHelper.open());
        Button selectButton = findViewById(R.id.btn_plan_select_trip);
        selectButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        selectButton.setOnClickListener(v -> showTripPicker());
        BottomNavHelper.setup(this, BottomNavHelper.TAB_SEARCH);
        loadCurrentTripPlan();
    }

    private void loadCurrentTripPlan() {
        List<Trip> trips = dbHelper.getTripsForUser(currentUserId);
        if (trips.isEmpty()) {
            tvPlanTripTitle.setText("暂无旅行");
            tvPlanTripDates.setText("创建旅行后，可在这里查看行前计划。");
            showEmpty("还没有可展示的旅行计划");
            return;
        }

        Trip trip = findSelectedTrip(trips);
        if (trip == null) {
            trip = trips.get(0);
            selectedTripId = trip.getId();
        }
        tvPlanTripTitle.setText(trip.getName() + " · " + trip.getDestination());
        tvPlanTripDates.setText(trip.getStartDate() + " ~ " + trip.getEndDate());
        List<PrePlan> plans = dbHelper.getPrePlansForTripSorted(trip.getId());
        renderTimeline(trip, plans);
    }

    private Trip findSelectedTrip(List<Trip> trips) {
        for (Trip trip : trips) {
            if (trip.getId() == selectedTripId) return trip;
        }
        return null;
    }

    private void renderSelectedTripPlan(Trip trip) {
        tvPlanTripTitle.setText(trip.getName() + " 路 " + trip.getDestination());
        tvPlanTripDates.setText(trip.getStartDate() + " ~ " + trip.getEndDate());
        List<PrePlan> plans = dbHelper.getPrePlansForTripSorted(trip.getId());
        renderTimeline(trip, plans);
    }

    private void showTripPicker() {
        List<Trip> trips = dbHelper.getTripsForUser(currentUserId);
        if (trips.isEmpty()) {
            showEmpty("还没有旅行，先创建一趟旅行再规划行程。");
            return;
        }
        String[] names = new String[trips.size()];
        for (int i = 0; i < trips.size(); i++) {
            Trip trip = trips.get(i);
            names[i] = trip.getName() + " · " + trip.getDestination() + " · " + trip.getStartDate();
        }
        new AlertDialog.Builder(this)
                .setTitle("选择旅行")
                .setItems(names, (dialog, which) -> {
                    selectedTripId = trips.get(which).getId();
                    renderSelectedTripPlan(trips.get(which));
                })
                .show();
    }

    private void renderTimeline(Trip trip, List<PrePlan> plans) {
        layoutPlanTimeline.removeAllViews();
        if (plans == null || plans.isEmpty()) {
            showEmpty("还没有行前计划\n先到行前准备中添加想去的地方和想吃的美食。");
            return;
        }

        emptyState.setVisibility(TextView.GONE);
        layoutPlanTimeline.setVisibility(LinearLayout.VISIBLE);
        String lastDate = null;
        for (PrePlan plan : plans) {
            String date = safe(plan.getTargetDate());
            if (!date.equals(lastDate)) {
                layoutPlanTimeline.addView(createDayHeader(trip, date));
                lastDate = date;
            }
            layoutPlanTimeline.addView(createPlanCard(plan));
        }
    }

    private TextView createDayHeader(Trip trip, String date) {
        TextView header = new TextView(this);
        String title;
        if (date.isEmpty()) {
            title = "未设置日期";
        } else {
            long day = DateUtils.getDaysBetween(trip.getStartDate(), date);
            title = "Day " + day + " / " + date;
        }
        header.setText(title);
        header.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        header.setTextSize(15);
        header.setTypeface(null, Typeface.BOLD);
        header.setPadding(dp(4), dp(16), dp(4), dp(6));
        return header;
    }

    private LinearLayout createPlanCard(PrePlan plan) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(4), 0, dp(8));

        TextView time = new TextView(this);
        time.setText(safe(plan.getTimeText()).isEmpty() ? "待定" : plan.getTimeText());
        time.setTextColor(ContextCompat.getColor(this, R.color.primary));
        time.setTextSize(13);
        time.setTypeface(null, Typeface.BOLD);
        time.setGravity(Gravity.CENTER);
        row.addView(time, new LinearLayout.LayoutParams(dp(58), LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.card_rounded_white));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText(plan.getTitle());
        title.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        titleRow.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView status = createTag(plan.getIsCompleted() == 1 ? "已完成" : "未完成", plan.getIsCompleted() == 1);
        titleRow.addView(status);
        card.addView(titleRow);

        TextView type = createMeta(planTypeText(plan.getType()));
        type.setBackground(ContextCompat.getDrawable(this, R.drawable.tag_primary));
        type.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        type.setPadding(dp(10), dp(4), dp(10), dp(4));
        LinearLayout.LayoutParams typeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        typeParams.setMargins(0, dp(8), 0, 0);
        card.addView(type, typeParams);

        card.addView(createMeta("地点：" + blankFallback(plan.getLocation(), "未填写")));
        card.addView(createMeta("Tips：" + blankFallback(plan.getNote(), "暂无备注")));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(card, cardParams);
        return row;
    }

    private TextView createTag(String text, boolean completed) {
        TextView tag = new TextView(this);
        tag.setText(text);
        tag.setTextSize(12);
        tag.setTextColor(ContextCompat.getColor(this, completed ? R.color.primary_dark : R.color.accent));
        tag.setBackground(ContextCompat.getDrawable(this, completed ? R.drawable.card_rounded_primary_light : R.drawable.tag_warning));
        tag.setPadding(dp(10), dp(4), dp(10), dp(4));
        return tag;
    }

    private TextView createMeta(String text) {
        TextView meta = new TextView(this);
        meta.setText(text);
        meta.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        meta.setTextSize(13);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(8), 0, 0);
        meta.setLayoutParams(params);
        return meta;
    }

    private void showEmpty(String message) {
        layoutPlanTimeline.setVisibility(LinearLayout.GONE);
        emptyState.setText(message);
        emptyState.setVisibility(TextView.VISIBLE);
    }

    private String planTypeText(String type) {
        if (type == null) return "其他";
        if (type.contains("地方")) return "景点";
        if (type.contains("美食")) return "美食";
        return type;
    }

    private String blankFallback(String value, String fallback) {
        return safe(value).isEmpty() ? fallback : value;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
