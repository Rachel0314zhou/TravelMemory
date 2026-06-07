package com.example.travelmemory.activity;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.DayRecord;
import com.example.travelmemory.model.Trip;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DayRecordActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int tripId = -1;
    private String selectedDate = "";
    private String selectedMood = "开心 😊"; // default

    private TextView tvTripName, tvSelectDate;
    private EditText etNote;
    private Button btnSave;

    // Mood text-view registers for easy toggle highlighting
    private final Map<String, TextView> moodViews = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_record);

        dbHelper = new DatabaseHelper(this);

        tripId = getIntent().getIntExtra("trip_id", -1);
        if (getIntent().hasExtra("record_date")) {
            selectedDate = getIntent().getStringExtra("record_date");
        }

        if (tripId == -1) {
            Toast.makeText(this, "未定位到具体旅行，无法创建日记", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTripName = findViewById(R.id.tv_record_trip_name);
        tvSelectDate = findViewById(R.id.tv_record_select_date);
        etNote = findViewById(R.id.et_record_note);
        btnSave = findViewById(R.id.btn_save_record);

        findViewById(R.id.btn_record_back).setOnClickListener(v -> finish());

        // Initialize Mood View mapping
        moodViews.put("开心 😊", findViewById(R.id.mood_happy));
        moodViews.put("平静 😐", findViewById(R.id.mood_calm));
        moodViews.put("疲惫 😴", findViewById(R.id.mood_tired));
        moodViews.put("兴奋 🤩", findViewById(R.id.mood_excited));
        moodViews.put("感动 🥹", findViewById(R.id.mood_touched));
        moodViews.put("遗憾 😔", findViewById(R.id.mood_regret));

        setupMoodClickListeners();

        // Get Trip parameters
        Trip trip = dbHelper.getTripById(tripId);
        if (trip != null) {
            tvTripName.setText("当前旅途程：毕业前的厦门之旅 ➟ " + trip.getName());
            if (selectedDate == null || selectedDate.isEmpty()) {
                selectedDate = trip.getStartDate(); // Default to started date
            }
        }

        tvSelectDate.setText(selectedDate);
        tvSelectDate.setOnClickListener(v -> showDatePicker());

        // Select default mood
        updateMoodSelection(selectedMood);

        // Try load existing journal for selected date
        tryLoadExistingRecord();

        btnSave.setOnClickListener(v -> saveRecord());
    }

    private void setupMoodClickListeners() {
        for (Map.Entry<String, TextView> entry : moodViews.entrySet()) {
            String moodText = entry.getKey();
            TextView view = entry.getValue();
            view.setOnClickListener(v -> updateMoodSelection(moodText));
        }
    }

    private void updateMoodSelection(String targetMood) {
        selectedMood = targetMood;

        // Colors mapping
        Map<String, Integer> colorMap = new HashMap<>();
        colorMap.put("开心 😊", R.color.mood_happy);
        colorMap.put("平静 😐", R.color.mood_calm);
        colorMap.put("疲惫 😴", R.color.mood_tired);
        colorMap.put("兴奋 🤩", R.color.mood_excited);
        colorMap.put("感动 🥹", R.color.mood_touched);
        colorMap.put("遗憾 😔", R.color.mood_regret);

        for (Map.Entry<String, TextView> entry : moodViews.entrySet()) {
            String mText = entry.getKey();
            TextView view = entry.getValue();
            if (mText.equals(targetMood)) {
                // Highlight Selected Mood
                int colorRes = colorMap.get(mText);
                int activeCol = ContextCompat.getColor(this, colorRes);
                view.setBackgroundTintList(ColorStateList.valueOf(activeCol));
                view.setTextColor(ContextCompat.getColor(this, R.color.black));
                view.setScaleX(1.05f);
                view.setScaleY(1.05f);
            } else {
                // Dim Unselected Mood
                view.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
                view.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                view.setScaleX(1.0f);
                view.setScaleY(1.0f);
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, selectYear, selectMonth, selectDay) -> {
            selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", selectYear, selectMonth + 1, selectDay);
            tvSelectDate.setText(selectedDate);
            // Refresh and check if there's an existing record for the newly selected date!
            tryLoadExistingRecord();
        }, year, month, day);
        dialog.show();
    }

    private void tryLoadExistingRecord() {
        DayRecord record = dbHelper.getDayRecordByTripAndDate(tripId, selectedDate);
        if (record != null) {
            etNote.setText(record.getNote());
            updateMoodSelection(record.getMood());
            Toast.makeText(this, "已载入当前日期先前的日记", Toast.LENGTH_SHORT).show();
        } else {
            etNote.setText("");
        }
    }

    private void saveRecord() {
        String noteText = etNote.getText().toString().trim();
        if (noteText.isEmpty()) {
            Toast.makeText(this, "总要写点什么再保存吧！", Toast.LENGTH_SHORT).show();
            return;
        }

        DayRecord record = new DayRecord();
        record.setTripId(tripId);
        record.setDate(selectedDate);
        record.setNote(noteText);
        record.setMood(selectedMood);
        record.setCreatedAt("2026-06-07");

        long result = dbHelper.addOrUpdateDayRecord(record);
        if (result != -1) {
            Toast.makeText(this, "日记保存成功！", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "日记保存失败，请检查设置", Toast.LENGTH_SHORT).show();
        }
    }
}
