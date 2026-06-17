package com.example.travelmemory.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.MemoryTripAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.BottomNavHelper;
import com.example.travelmemory.utils.DrawerHelper;
import com.example.travelmemory.utils.SessionManager;

import java.util.List;

public class MemoryListActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private RecyclerView rvMemoryTrips;
    private TextView tvEmpty;
    private LinearLayout layoutIntro;
    private DrawerHelper drawerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_list);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        drawerHelper = new DrawerHelper(this);
        drawerHelper.setup();
        rvMemoryTrips = findViewById(R.id.rv_memory_trips);
        tvEmpty = findViewById(R.id.tv_memory_empty);
        layoutIntro = findViewById(R.id.layout_memory_intro);
        rvMemoryTrips.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.btn_memory_list_back).setOnClickListener(v -> drawerHelper.open());
        BottomNavHelper.setup(this, BottomNavHelper.TAB_MEMORY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTrips();
    }

    private void loadTrips() {
        List<Trip> trips = dbHelper.getTripsForUser(sessionManager.getCurrentUserId());
        if (trips.isEmpty()) {
            rvMemoryTrips.setVisibility(View.GONE);
            layoutIntro.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            layoutIntro.setVisibility(View.VISIBLE);
            rvMemoryTrips.setVisibility(View.VISIBLE);
            rvMemoryTrips.setAdapter(new MemoryTripAdapter(this, trips));
        }
    }
}
