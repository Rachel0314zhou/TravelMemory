package com.example.travelmemory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.TripAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TripNavigator;
import com.example.travelmemory.utils.TravelConstants;

import java.util.List;

public class SelectTripActivity extends AppCompatActivity implements TripAdapter.OnTripClickListener {

    private DatabaseHelper dbHelper;
    private int currentUserId = -1;
    private String mode = TravelConstants.MODE_OVERVIEW;
    private RecyclerView rvTrips;
    private TextView tvEmpty;
    private TextView tvSubtitle;
    private LinearLayout emptyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_trip);

        dbHelper = new DatabaseHelper(this);
        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        mode = getIntent().getStringExtra(TravelConstants.EXTRA_MODE);
        if (mode == null || mode.isEmpty()) mode = TravelConstants.MODE_OVERVIEW;

        if (currentUserId <= 0) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        rvTrips = findViewById(R.id.rv_select_trips);
        tvEmpty = findViewById(R.id.tv_select_trip_empty);
        tvSubtitle = findViewById(R.id.tv_select_trip_subtitle);
        emptyLayout = findViewById(R.id.layout_select_trip_empty);
        rvTrips.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.btn_select_trip_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_select_trip_add).setOnClickListener(v -> startActivity(new Intent(this, TripTypeActivity.class)));
        tvSubtitle.setText(subtitleForMode(mode));

        loadTrips();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbHelper != null && currentUserId > 0) loadTrips();
    }

    private void loadTrips() {
        List<Trip> trips = dbHelper.getTripsForUser(currentUserId);
        if (trips.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            rvTrips.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
            rvTrips.setVisibility(View.VISIBLE);
            rvTrips.setAdapter(new TripAdapter(this, trips, this));
        }
    }

    @Override
    public void onTripClick(Trip trip) {
        if (TravelConstants.MODE_DELETE_TRIP.equals(mode)) {
            confirmDeleteTrip(trip);
            return;
        }
        Toast.makeText(this, "已选择：" + trip.getName(), Toast.LENGTH_SHORT).show();
        TripNavigator.openTripDetail(this, trip.getId(), mode);
    }

    private String subtitleForMode(String mode) {
        if (TravelConstants.MODE_PRE_TRIP.equals(mode)) return "请选择要做行前准备的旅行";
        if (TravelConstants.MODE_PACKING.equals(mode)) return "请选择要整理行李的旅行";
        if (TravelConstants.MODE_DURING_TRIP.equals(mode)) return "请选择要记录的旅行";
        if (TravelConstants.MODE_POST_TRIP.equals(mode)) return "请选择要整理回忆的旅行";
        if (TravelConstants.MODE_MEMORY.equals(mode)) return "请选择要生成回忆卡片的旅行";
        if (TravelConstants.MODE_EXPENSE.equals(mode)) return "请选择要查看花费的旅行";
        if (TravelConstants.MODE_DELETE_TRIP.equals(mode)) return "请选择要删除的旅行，确认后将移除这趟旅行及相关记录";
        return "请选择一趟旅行继续操作";
    }

    private void confirmDeleteTrip(Trip trip) {
        new AlertDialog.Builder(this)
                .setTitle("删除旅行")
                .setMessage("确定删除“" + trip.getName() + "”吗？相关的日记、照片、打卡、花费、计划和行李清单也会一并移除。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> {
                    if (dbHelper.deleteTripForUser(trip.getId(), currentUserId)) {
                        Toast.makeText(this, "旅行已删除", Toast.LENGTH_SHORT).show();
                        loadTrips();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "无权删除这趟旅行", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}
