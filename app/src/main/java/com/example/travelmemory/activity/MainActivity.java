package com.example.travelmemory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.TripAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvGreeting;
    private RecyclerView rvTrips;
    private LinearLayout emptyStateLayout;
    private FloatingActionButton fabAddTrip;
    private ImageButton btnSearchHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        tvGreeting = findViewById(R.id.tv_greeting);
        rvTrips = findViewById(R.id.rv_trips);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        fabAddTrip = findViewById(R.id.fab_add_trip);
        btnSearchHeader = findViewById(R.id.btn_search_header);

        rvTrips.setLayoutManager(new LinearLayoutManager(this));

        // Click Actions
        fabAddTrip.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTripActivity.class);
            startActivity(intent);
        });

        btnSearchHeader.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Dynamic loading of nickname and trip details
        loadUserDataAndTrips();
    }

    private void loadUserDataAndTrips() {
        // Load User greeting
        User user = dbHelper.getUser();
        if (user != null && user.getNickname() != null) {
            tvGreeting.setText("你好，" + user.getNickname());
        }

        // Load Travels
        List<Trip> tripList = dbHelper.getAllTrips();
        if (tripList.isEmpty()) {
            rvTrips.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            rvTrips.setVisibility(View.VISIBLE);
            TripAdapter adapter = new TripAdapter(this, tripList);
            rvTrips.setAdapter(adapter);
        }
    }
}
