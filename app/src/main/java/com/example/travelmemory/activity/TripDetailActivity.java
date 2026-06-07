package com.example.travelmemory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.DayRecordAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.DayRecord;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.DateUtils;
import com.example.travelmemory.utils.ImageUtils;

import java.util.List;

public class TripDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int tripId = -1;

    private ImageView ivDetailCover;
    private TextView tvDetailName, tvDetailDestination, tvDetailDates, tvDetailDaysBubble;

    private TextView tvStatPhotosCount, tvStatSpotsCount, tvStatExpenseSum;
    private TextView tvTimelineEmpty;
    private RecyclerView rvTimeline;

    private ImageButton btnEdit, btnDelete;
    private Button btnMenuNotes, btnMenuPhotos, btnMenuSpots, btnMenuExpense, btnMenuMemory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        dbHelper = new DatabaseHelper(this);

        if (getIntent().hasExtra("trip_id")) {
            tripId = getIntent().getIntExtra("trip_id", -1);
        }

        if (tripId == -1) {
            Toast.makeText(this, "未能定位到正确的旅行，请重试", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mapping Views
        ivDetailCover = findViewById(R.id.iv_detail_cover);
        tvDetailName = findViewById(R.id.tv_detail_name);
        tvDetailDestination = findViewById(R.id.tv_detail_destination);
        tvDetailDates = findViewById(R.id.tv_detail_dates);
        tvDetailDaysBubble = findViewById(R.id.tv_detail_days_bubble);

        tvStatPhotosCount = findViewById(R.id.tv_stat_photos_count);
        tvStatSpotsCount = findViewById(R.id.tv_stat_spots_count);
        tvStatExpenseSum = findViewById(R.id.tv_stat_expense_sum);

        tvTimelineEmpty = findViewById(R.id.tv_detail_timeline_empty);
        rvTimeline = findViewById(R.id.rv_detail_timeline);

        btnEdit = findViewById(R.id.btn_detail_edit);
        btnDelete = findViewById(R.id.btn_detail_delete);

        btnMenuNotes = findViewById(R.id.btn_menu_notes);
        btnMenuPhotos = findViewById(R.id.btn_menu_photos);
        btnMenuSpots = findViewById(R.id.btn_menu_spots);
        btnMenuExpense = findViewById(R.id.btn_menu_expense);
        btnMenuMemory = findViewById(R.id.btn_menu_memory);

        rvTimeline.setLayoutManager(new LinearLayoutManager(this));

        // Back button
        findViewById(R.id.btn_detail_back).setOnClickListener(v -> finish());

        // Setup Operations triggers
        setupOnClickListeners();
    }

    private void setupOnClickListeners() {
        // Redirection buttons
        btnMenuNotes.setOnClickListener(v -> {
            Intent intent = new Intent(this, DayRecordActivity.class);
            intent.putExtra("trip_id", tripId);
            startActivity(intent);
        });

        btnMenuPhotos.setOnClickListener(v -> {
            Intent intent = new Intent(this, PhotoActivity.class);
            intent.putExtra("trip_id", tripId);
            startActivity(intent);
        });

        btnMenuSpots.setOnClickListener(v -> {
            Intent intent = new Intent(this, SpotsActivity.class);
            intent.putExtra("trip_id", tripId);
            startActivity(intent);
        });

        btnMenuExpense.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExpenseActivity.class);
            intent.putExtra("trip_id", tripId);
            startActivity(intent);
        });

        btnMenuMemory.setOnClickListener(v -> {
            Intent intent = new Intent(this, MemoryCardActivity.class);
            intent.putExtra("trip_id", tripId);
            startActivity(intent);
        });

        // Stats clicks redirect to relevant sheets
        findViewById(R.id.layout_stat_photos).setOnClickListener(v -> btnMenuPhotos.performClick());
        findViewById(R.id.layout_stat_spots).setOnClickListener(v -> btnMenuSpots.performClick());
        findViewById(R.id.layout_stat_expense).setOnClickListener(v -> btnMenuExpense.performClick());

        // Edit
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(TripDetailActivity.this, AddTripActivity.class);
            intent.putExtra("trip_id", tripId);
            startActivity(intent);
        });

        // Double checking deletion
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_msg)
                .setPositiveButton("坚持删除", (dialog, which) -> {
                    dbHelper.deleteTrip(tripId);
                    Toast.makeText(this, "那段美好的旅迹已被抹去...", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("留下来", null)
                .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fully refresh core stats & metadata
        loadTripDetails();
    }

    private void loadTripDetails() {
        Trip trip = dbHelper.getTripById(tripId);
        if (trip == null) {
            finish();
            return;
        }

        // General labels
        tvDetailName.setText(trip.getName());
        tvDetailDestination.setText("📍 " + trip.getDestination());
        tvDetailDates.setText(trip.getStartDate() + " ~ " + trip.getEndDate());

        long days = DateUtils.getDaysBetween(trip.getStartDate(), trip.getEndDate());
        tvDetailDaysBubble.setText(days + " 天");

        // Display background photo
        ImageUtils.displayLocalImage(ivDetailCover, trip.getCoverPath(), R.drawable.travel_placeholder);

        // Real-time statistics counters queries
        int photosCount = dbHelper.countPhotosForTrip(tripId);
        int spotsCount = dbHelper.countSpotsForTrip(tripId);
        double totalExpense = dbHelper.getTotalExpenseForTrip(tripId);

        tvStatPhotosCount.setText(String.valueOf(photosCount));
        tvStatSpotsCount.setText(String.valueOf(spotsCount));
        tvStatExpenseSum.setText(String.format("¥ %.2f", totalExpense));

        // Loading Timeline details
        List<DayRecord> records = dbHelper.getDayRecordsForTrip(tripId);
        if (records.isEmpty()) {
            rvTimeline.setVisibility(View.GONE);
            tvTimelineEmpty.setVisibility(View.VISIBLE);
        } else {
            tvTimelineEmpty.setVisibility(View.GONE);
            rvTimeline.setVisibility(View.VISIBLE);
            DayRecordAdapter adapter = new DayRecordAdapter(this, records);
            rvTimeline.setAdapter(adapter);
        }
    }
}
