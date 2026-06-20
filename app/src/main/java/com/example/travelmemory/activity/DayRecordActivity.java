package com.example.travelmemory.activity;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.DayRecord;
import com.example.travelmemory.model.Photo;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.ImageUtils;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TravelConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DayRecordActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ALBUM = 201;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int tripId = -1;
    private int currentUserId = -1;
    private String selectedDate = "";
    private String selectedMood = "开心";
    private TextView tvTripName, tvSelectDate, tvPhotoStatus;
    private EditText etNote;
    private final List<String> pendingPhotoPaths = new ArrayList<>();
    private final List<String> existingPhotoPaths = new ArrayList<>();
    private final Map<String, TextView> moodViews = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_record);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);
        selectedDate = getIntent().getStringExtra(TravelConstants.EXTRA_RECORD_DATE);

        if (tripId == -1) {
            Toast.makeText(this, "未定位到具体旅行，无法创建日记", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTripName = findViewById(R.id.tv_record_trip_name);
        tvSelectDate = findViewById(R.id.tv_record_select_date);
        tvPhotoStatus = findViewById(R.id.tv_record_photo_status);
        etNote = findViewById(R.id.et_record_note);
        Button btnSave = findViewById(R.id.btn_save_record);
        findViewById(R.id.btn_record_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_record_add_photo).setOnClickListener(v -> choosePhotoFromAlbum());

        moodViews.put("开心", findViewById(R.id.mood_happy));
        moodViews.put("平静", findViewById(R.id.mood_calm));
        moodViews.put("疲惫", findViewById(R.id.mood_tired));
        moodViews.put("兴奋", findViewById(R.id.mood_excited));
        moodViews.put("感动", findViewById(R.id.mood_touched));
        moodViews.put("遗憾", findViewById(R.id.mood_regret));
        setupMoodClickListeners();

        Trip trip = dbHelper.getTripForUser(tripId, currentUserId);
        if (trip == null) {
            Toast.makeText(this, "无权记录这段旅行", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (trip != null) {
            tvTripName.setText("当前旅程：" + trip.getName());
            if (selectedDate == null || selectedDate.isEmpty()) {
                selectedDate = trip.getStartDate();
            }
        }

        tvSelectDate.setText(selectedDate);
        tvSelectDate.setOnClickListener(v -> showDatePicker());
        updateMoodSelection(selectedMood);
        tryLoadExistingRecord();
        btnSave.setOnClickListener(v -> saveRecord());
    }

    private void choosePhotoFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "选择旅行照片"), REQUEST_CODE_ALBUM);
    }

    private void setupMoodClickListeners() {
        for (Map.Entry<String, TextView> entry : moodViews.entrySet()) {
            String moodText = entry.getKey();
            entry.getValue().setOnClickListener(v -> updateMoodSelection(moodText));
        }
    }

    private void updateMoodSelection(String targetMood) {
        selectedMood = targetMood;
        for (Map.Entry<String, TextView> entry : moodViews.entrySet()) {
            TextView view = entry.getValue();
            if (entry.getKey().equals(targetMood)) {
                view.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, moodColor(targetMood))));
                view.setTextColor(ContextCompat.getColor(this, R.color.black));
                view.setScaleX(1.05f);
                view.setScaleY(1.05f);
            } else {
                view.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
                view.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                view.setScaleX(1.0f);
                view.setScaleY(1.0f);
            }
        }
    }

    private int moodColor(String mood) {
        switch (mood) {
            case "平静": return R.color.mood_calm;
            case "疲惫": return R.color.mood_tired;
            case "兴奋": return R.color.mood_excited;
            case "感动": return R.color.mood_touched;
            case "遗憾": return R.color.mood_regret;
            default: return R.color.mood_happy;
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day);
            tvSelectDate.setText(selectedDate);
            tryLoadExistingRecord();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void tryLoadExistingRecord() {
        pendingPhotoPaths.clear();
        existingPhotoPaths.clear();
        DayRecord record = dbHelper.getDayRecordByTripAndDate(tripId, selectedDate);
        if (record != null) {
            etNote.setText(record.getNote());
            updateMoodSelection(record.getMood());
            List<Photo> photos = dbHelper.getPhotosForDayRecord(record.getId());
            for (Photo photo : photos) {
                existingPhotoPaths.add(photo.getImagePath());
            }
        } else {
            etNote.setText("");
        }
        updatePhotoStatus();
    }

    private void saveRecord() {
        String noteText = etNote.getText().toString().trim();
        if (noteText.isEmpty() && pendingPhotoPaths.isEmpty() && existingPhotoPaths.isEmpty()) {
            Toast.makeText(this, "请先写点今天的旅行记录", Toast.LENGTH_SHORT).show();
            return;
        }

        if (noteText.isEmpty()) {
            noteText = "添加了旅行照片";
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
            savePhotosForRecord();
            finish();
        } else {
            Toast.makeText(this, "日记保存失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePhotosForRecord() {
        if (pendingPhotoPaths.isEmpty()) return;
        DayRecord savedRecord = dbHelper.getDayRecordByTripAndDate(tripId, selectedDate);
        int dayRecordId = savedRecord == null ? -1 : savedRecord.getId();
        if (dayRecordId > 0) {
            dbHelper.replacePhotosForDayRecord(tripId, dayRecordId, pendingPhotoPaths, selectedDate);
        }
    }

    private void updatePhotoStatus() {
        if (!pendingPhotoPaths.isEmpty()) {
            tvPhotoStatus.setText("已选择 " + pendingPhotoPaths.size() + " 张新照片，保存后将覆盖原照片");
        } else if (!existingPhotoPaths.isEmpty()) {
            tvPhotoStatus.setText("已保存 " + existingPhotoPaths.size() + " 张照片，可重新选择后覆盖");
        } else {
            tvPhotoStatus.setText("还未选择照片");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode != REQUEST_CODE_ALBUM || data == null) return;

        ClipData clipData = data.getClipData();
        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                addSelectedPhoto(clipData.getItemAt(i).getUri());
            }
            updatePhotoStatus();
            return;
        }

        if (data.getData() != null) {
            addSelectedPhoto(data.getData());
            updatePhotoStatus();
        }
    }

    private void addSelectedPhoto(Uri uri) {
        String savedPath = ImageUtils.copyImageToAppDirectory(this, uri);
            if (!savedPath.isEmpty()) {
                pendingPhotoPaths.add(savedPath);
                updatePhotoStatus();
                tvPhotoStatus.setText("已选择 " + pendingPhotoPaths.size() + " 张照片");
            } else {
                Toast.makeText(this, "添加照片失败", Toast.LENGTH_SHORT).show();
            }
    }
}
