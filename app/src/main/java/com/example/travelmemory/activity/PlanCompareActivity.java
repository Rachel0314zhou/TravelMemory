package com.example.travelmemory.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.RegretAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.RegretItem;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TravelConstants;

import java.util.List;

public class PlanCompareActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int tripId = -1;
    private int currentUserId = -1;
    private TextView tvSummary;
    private TextView tvEmpty;
    private RecyclerView rvRegrets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_compare);

        dbHelper = new DatabaseHelper(this);
        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);

        if (currentUserId <= 0 || tripId <= 0 || !dbHelper.isTripOwnedByUser(tripId, currentUserId)) {
            Toast.makeText(this, "请先选择自己的旅行", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvSummary = findViewById(R.id.tv_compare_summary);
        tvEmpty = findViewById(R.id.tv_regret_empty);
        rvRegrets = findViewById(R.id.rv_regrets);
        rvRegrets.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btn_compare_back).setOnClickListener(v -> finish());
        dbHelper.generateRegretsForTrip(tripId);
        loadCompareData();
    }

    private void loadCompareData() {
        int total = dbHelper.countPrePlansForTrip(tripId);
        int completed = dbHelper.countCompletedPrePlansForTrip(tripId);
        int unfinished = Math.max(total - completed, 0);
        int rate = dbHelper.getPlanCompletionRate(tripId);
        int regretCount = dbHelper.countRegretsForTrip(tripId);
        tvSummary.setText("原计划 " + total + " 项，已完成 " + completed + " 项"
                + "\n这次没来得及完成 " + unfinished + " 项，完成率 " + rate + "%"
                + "\n已自动整理遗憾回顾 " + regretCount + " 条");

        List<RegretItem> regrets = dbHelper.getRegretsForTrip(tripId);
        if (regrets.isEmpty()) {
            rvRegrets.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvRegrets.setVisibility(View.VISIBLE);
            rvRegrets.setAdapter(new RegretAdapter(this, regrets));
        }
    }
}
