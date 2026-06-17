package com.example.travelmemory.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.PrePlan;
import com.example.travelmemory.model.RegretItem;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.ImageUtils;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TripNavigator;
import com.example.travelmemory.utils.TravelConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTripActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ALBUM = 101;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int tripId = -1;
    private int currentUserId = -1;
    private String tripMode = TravelConstants.TripMode.SIMPLE;
    private String coverPath = "";

    private EditText etTripName;
    private EditText etDestination;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private TextView tvTitle;
    private TextView tvSelectedRegrets;
    private LinearLayout layoutRegretReminder;
    private ImageView ivCoverPreview;
    private final List<RegretItem> historyRegrets = new ArrayList<>();
    private final List<RegretItem> selectedRegrets = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        if (currentUserId <= 0) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tripMode = getIntent().getStringExtra(TravelConstants.EXTRA_TRIP_MODE);
        if (tripMode == null || tripMode.isEmpty()) tripMode = TravelConstants.TripMode.SIMPLE;

        tvTitle = findViewById(R.id.tv_title);
        etTripName = findViewById(R.id.et_trip_name);
        etDestination = findViewById(R.id.et_destination);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEndDate = findViewById(R.id.tv_end_date);
        tvSelectedRegrets = findViewById(R.id.tv_selected_regrets);
        layoutRegretReminder = findViewById(R.id.layout_regret_reminder);
        ivCoverPreview = findViewById(R.id.iv_cover_preview);
        Button btnChooseAlbum = findViewById(R.id.btn_choose_album);
        Button btnSaveTrip = findViewById(R.id.btn_save_trip);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);
        if (tripId != -1) {
            tvTitle.setText("编辑旅行");
            layoutRegretReminder.setVisibility(View.GONE);
            loadTripData();
        } else {
            tvTitle.setText("新建旅行");
            setupRegretReminder();
        }

        tvStartDate.setOnClickListener(v -> showDatePicker(tvStartDate));
        tvEndDate.setOnClickListener(v -> showDatePicker(tvEndDate));
        btnChooseAlbum.setOnClickListener(v -> openAlbum());
        btnSaveTrip.setOnClickListener(v -> saveTrip());
    }

    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_ALBUM);
    }

    private void setupRegretReminder() {
        historyRegrets.clear();
        historyRegrets.addAll(dbHelper.getRegretsForUser(currentUserId));
        if (historyRegrets.isEmpty() || !TravelConstants.isPlannedTrip(tripMode)) {
            layoutRegretReminder.setVisibility(View.GONE);
            return;
        }
        layoutRegretReminder.setVisibility(View.VISIBLE);
        findViewById(R.id.btn_view_regrets).setOnClickListener(v -> showRegretListDialog());
        findViewById(R.id.btn_pick_regrets).setOnClickListener(v -> showPickRegretsDialog());
        updateSelectedRegretsText();
    }

    private void showRegretListDialog() {
        StringBuilder message = new StringBuilder();
        for (RegretItem item : historyRegrets) {
            message.append("【").append(item.getType()).append("】")
                    .append(item.getTitle()).append("\n")
                    .append(item.getReason()).append("\n\n");
        }
        new AlertDialog.Builder(this)
                .setTitle("历史遗憾")
                .setMessage(message.toString().trim())
                .setPositiveButton("知道了", null)
                .show();
    }

    private void showPickRegretsDialog() {
        String[] titles = new String[historyRegrets.size()];
        boolean[] checked = new boolean[historyRegrets.size()];
        for (int i = 0; i < historyRegrets.size(); i++) {
            RegretItem item = historyRegrets.get(i);
            titles[i] = "【" + item.getType() + "】" + item.getTitle();
            checked[i] = selectedRegrets.contains(item);
        }
        new AlertDialog.Builder(this)
                .setTitle("加入本次计划")
                .setMultiChoiceItems(titles, checked, (dialog, which, isChecked) -> {
                    RegretItem item = historyRegrets.get(which);
                    if (isChecked && !selectedRegrets.contains(item)) {
                        selectedRegrets.add(item);
                    } else if (!isChecked) {
                        selectedRegrets.remove(item);
                    }
                })
                .setPositiveButton("确定", (dialog, which) -> updateSelectedRegretsText())
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateSelectedRegretsText() {
        tvSelectedRegrets.setText(selectedRegrets.isEmpty()
                ? "尚未选择遗憾"
                : "已选择 " + selectedRegrets.size() + " 个遗憾，将加入本次行前计划");
    }

    private void loadTripData() {
        Trip trip = dbHelper.getTripForUser(tripId, currentUserId);
        if (trip != null) {
            tripMode = trip.getMode();
            etTripName.setText(trip.getName());
            etDestination.setText(trip.getDestination());
            tvStartDate.setText(trip.getStartDate());
            tvEndDate.setText(trip.getEndDate());
            coverPath = trip.getCoverPath();
            ImageUtils.displayLocalImage(ivCoverPreview, coverPath, R.drawable.travel_placeholder);
        } else {
            Toast.makeText(this, "无权编辑这段旅行", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showDatePicker(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            String dateStr = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day);
            textView.setText(dateStr);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CODE_ALBUM && data != null && data.getData() != null) {
            String copiedPath = ImageUtils.copyImageToAppDirectory(this, data.getData());
            if (!copiedPath.isEmpty()) {
                coverPath = copiedPath;
                ImageUtils.displayLocalImage(ivCoverPreview, coverPath, R.drawable.travel_placeholder);
            }
        }
    }

    private void saveTrip() {
        String name = etTripName.getText().toString().trim();
        String destination = etDestination.getText().toString().trim();
        String startDate = tvStartDate.getText().toString().trim();
        String endDate = tvEndDate.getText().toString().trim();

        if (name.isEmpty() || destination.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "请完整填写旅行信息", Toast.LENGTH_SHORT).show();
            return;
        }
        if (startDate.compareTo(endDate) > 0) {
            Toast.makeText(this, "开始日期不能晚于结束日期", Toast.LENGTH_SHORT).show();
            return;
        }

        Trip trip = new Trip();
        trip.setUserId(currentUserId);
        trip.setName(name);
        trip.setDestination(destination);
        trip.setStartDate(startDate);
        trip.setEndDate(endDate);
        trip.setCoverPath(coverPath);
        trip.setMode(tripMode);
        trip.setStatus(TravelConstants.isPlannedTrip(tripMode) ? TravelConstants.TripState.PLANNING : TravelConstants.TripState.ACTIVE);
        trip.setCreatedAt("2026-06-09");

        if (tripId == -1) {
            long newTripId = dbHelper.addTrip(trip);
            if (newTripId != -1) {
                if (TravelConstants.isPlannedTrip(tripMode)) {
                    addSelectedRegretsToPlan((int) newTripId);
                }
                Toast.makeText(this, "旅行已创建", Toast.LENGTH_SHORT).show();
                TripNavigator.openTripDetail(this, (int) newTripId, TravelConstants.MODE_OVERVIEW);
            }
        } else {
            trip.setId(tripId);
            dbHelper.updateTrip(trip);
            Toast.makeText(this, "旅行已更新", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void addSelectedRegretsToPlan(int newTripId) {
        for (RegretItem regret : selectedRegrets) {
            PrePlan plan = new PrePlan();
            plan.setTripId(newTripId);
            plan.setTitle(regret.getTitle());
            plan.setType(regret.getType());
            plan.setLocation("");
            plan.setNote(regret.getReason());
            plan.setTargetDate("");
            plan.setIsCompleted(0);
            plan.setCreatedAt("2026-06-09");
            dbHelper.addPrePlan(plan);
        }
    }
}
