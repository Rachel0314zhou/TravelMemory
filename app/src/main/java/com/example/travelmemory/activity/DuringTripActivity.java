package com.example.travelmemory.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TripNavigator;
import com.example.travelmemory.utils.TripStateManager;
import com.example.travelmemory.utils.TravelConstants;

public class DuringTripActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TripStateManager tripStateManager;
    private int tripId = -1;
    private int currentUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_during_trip);

        dbHelper = new DatabaseHelper(this);
        tripStateManager = new TripStateManager(dbHelper);
        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);

        if (currentUserId <= 0 || tripId <= 0 || !dbHelper.isTripOwnedByUser(tripId, currentUserId)) {
            Toast.makeText(this, "请先选择自己的旅行", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Trip trip = dbHelper.getTripForUser(tripId, currentUserId);
        TextView tvTripName = findViewById(R.id.tv_during_trip_name);
        if (trip != null) {
            tvTripName.setText(trip.getName() + " · " + trip.getDestination());
        }

        findViewById(R.id.btn_during_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_during_day_record).setOnClickListener(v -> openWithTrip(DayRecordActivity.class));
        findViewById(R.id.btn_during_photo).setOnClickListener(v -> openWithTrip(PhotoActivity.class));
        findViewById(R.id.btn_during_spot).setOnClickListener(v -> openWithTrip(SpotsActivity.class));
        findViewById(R.id.btn_during_expense).setOnClickListener(v -> openWithTrip(ExpenseActivity.class));
        findViewById(R.id.btn_enter_post_trip).setOnClickListener(v -> {
            tripStateManager.finishTrip(tripId);
            openWithTrip(PostTripRecordActivity.class);
        });
    }

    private void openWithTrip(Class<?> target) {
        TripNavigator.openFeature(this, target, tripId);
    }
}
