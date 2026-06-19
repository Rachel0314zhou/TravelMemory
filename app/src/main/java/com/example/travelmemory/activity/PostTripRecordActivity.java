package com.example.travelmemory.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.PrePlanAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.PrePlan;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TripNavigator;
import com.example.travelmemory.utils.TravelConstants;

import java.util.List;

public class PostTripRecordActivity extends AppCompatActivity implements PrePlanAdapter.Listener {

    private DatabaseHelper dbHelper;
    private int tripId = -1;
    private int currentUserId = -1;
    private TextView tvSummary;
    private TextView tvEmpty;
    private RecyclerView rvPlans;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_trip_record);

        dbHelper = new DatabaseHelper(this);
        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);

        if (currentUserId <= 0 || tripId <= 0 || !dbHelper.isTripOwnedByUser(tripId, currentUserId)) {
            Toast.makeText(this, "请先选择自己的旅行", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        scrollView = findViewById(R.id.scroll_post_trip);
        tvSummary = findViewById(R.id.tv_post_plan_summary);
        tvEmpty = findViewById(R.id.tv_post_plan_empty);
        rvPlans = findViewById(R.id.rv_post_pre_plans);
        rvPlans.setLayoutManager(new LinearLayoutManager(this));
        rvPlans.setNestedScrollingEnabled(false);

        findViewById(R.id.btn_post_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_post_day_record).setOnClickListener(v -> openWithTrip(DayRecordActivity.class));
        findViewById(R.id.btn_post_photo).setOnClickListener(v -> openWithTrip(PhotoActivity.class));
        findViewById(R.id.btn_post_spot).setOnClickListener(v -> openWithTrip(SpotsActivity.class));
        findViewById(R.id.btn_post_expense).setOnClickListener(v -> openWithTrip(ExpenseActivity.class));
        findViewById(R.id.btn_post_confirm_plan).setOnClickListener(v -> {
            Toast.makeText(this, "请在下方逐项确认计划完成情况", Toast.LENGTH_SHORT).show();
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        });
        findViewById(R.id.btn_post_compare).setOnClickListener(v -> openWithTrip(PlanCompareActivity.class));
        findViewById(R.id.btn_post_report).setOnClickListener(v -> openWithTrip(TripReportActivity.class));
        findViewById(R.id.btn_post_memory).setOnClickListener(v -> openWithTrip(TripRecapActivity.class));

        loadPlans();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbHelper != null) {
            loadPlans();
        }
    }

    private void openWithTrip(Class<?> target) {
        TripNavigator.openFeature(this, target, tripId);
    }

    private void loadPlans() {
        List<PrePlan> plans = dbHelper.getPrePlansForTrip(tripId);
        int total = plans.size();
        int completed = dbHelper.countCompletedPrePlansForTrip(tripId);
        tvSummary.setText("已完成 " + completed + " / " + total + "，完成率 " + dbHelper.getPlanCompletionRate(tripId) + "%");
        if (plans.isEmpty()) {
            rvPlans.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvPlans.setVisibility(View.VISIBLE);
            rvPlans.setAdapter(new PrePlanAdapter(this, plans, this));
        }
    }

    @Override
    public void onToggle(PrePlan plan, boolean completed) {
        dbHelper.markPrePlanCompleted(plan.getId(), completed);
        Toast.makeText(this, completed ? "已标记为实际完成" : "已设为未完成", Toast.LENGTH_SHORT).show();
        loadPlans();
    }

    @Override
    public void onEdit(PrePlan plan) {
        Toast.makeText(this, "请到行前准备页面编辑计划", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDelete(PrePlan plan) {
        Toast.makeText(this, "请到行前准备页面删除计划", Toast.LENGTH_SHORT).show();
    }
}
