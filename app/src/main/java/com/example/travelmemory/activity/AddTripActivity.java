package com.example.travelmemory.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.ImageUtils;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

public class AddTripActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ALBUM = 101;
    private static final int REQUEST_CODE_CAMERA = 102;

    private DatabaseHelper dbHelper;
    private int tripId = -1; // -1 means Add Mode, >= 1 means Edit Mode
    private String coverPath = "";

    private EditText etTripName, etDestination;
    private TextView tvStartDate, tvEndDate, tvTitle;
    private ImageView ivCoverPreview;
    private Button btnChooseAlbum, btnTakePhoto, btnSaveTrip;

    private File tempCameraFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        dbHelper = new DatabaseHelper(this);

        // Views mapping
        tvTitle = findViewById(R.id.tv_title);
        etTripName = findViewById(R.id.et_trip_name);
        etDestination = findViewById(R.id.et_destination);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEndDate = findViewById(R.id.tv_end_date);
        ivCoverPreview = findViewById(R.id.iv_cover_preview);
        btnChooseAlbum = findViewById(R.id.btn_choose_album);
        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnSaveTrip = findViewById(R.id.btn_save_trip);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Get Trip ID if in edit mode
        if (getIntent().hasExtra("trip_id")) {
            tripId = getIntent().getIntExtra("trip_id", -1);
        }

        if (tripId != -1) {
            tvTitle.setText(R.string.edit_trip_title);
            loadTripData();
        } else {
            tvTitle.setText(R.string.add_trip_title);
        }

        // Set Date Pickers
        tvStartDate.setOnClickListener(v -> showDatePicker(tvStartDate));
        tvEndDate.setOnClickListener(v -> showDatePicker(tvEndDate));

        // Choose picture from album
        btnChooseAlbum.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_ALBUM);
        });

        // Snap picture from camera
        btnTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                tempCameraFile = new File(getExternalCacheDir(), "camera_temp_" + System.currentTimeMillis() + ".jpg");
                if (tempCameraFile.exists()) {
                    tempCameraFile.delete();
                }
                Uri tempUri = FileProvider.getUriForFile(this, "com.example.fileprovider", tempCameraFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                // Grant URI permission explicitly
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "相机无法启动，请检查权限设置", Toast.LENGTH_SHORT).show();
            }
        });

        // Form submission
        btnSaveTrip.setOnClickListener(v -> saveTrip());
    }

    private void loadTripData() {
        Trip trip = dbHelper.getTripById(tripId);
        if (trip != null) {
            etTripName.setText(trip.getName());
            etDestination.setText(trip.getDestination());
            tvStartDate.setText(trip.getStartDate());
            tvEndDate.setText(trip.getEndDate());
            coverPath = trip.getCoverPath();
            ImageUtils.displayLocalImage(ivCoverPreview, coverPath, R.drawable.travel_placeholder);
        }
    }

    private void showDatePicker(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, selectYear, selectMonth, selectDay) -> {
            String dateStr = String.format(Locale.getDefault(), "%d-%02d-%02d", selectYear, selectMonth + 1, selectDay);
            textView.setText(dateStr);
        }, year, month, day);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_ALBUM && data != null) {
                Uri selectedUri = data.getData();
                if (selectedUri != null) {
                    // Copy selected album image immediately into private sandbox storage
                    String copiedPath = ImageUtils.copyImageToAppDirectory(this, selectedUri);
                    if (!copiedPath.isEmpty()) {
                        coverPath = copiedPath;
                        ImageUtils.displayLocalImage(ivCoverPreview, coverPath, R.drawable.travel_placeholder);
                    }
                }
            } else if (requestCode == REQUEST_CODE_CAMERA) {
                if (tempCameraFile != null && tempCameraFile.exists()) {
                    // Copy camera captured file immediately to private files storage
                    Uri cameraUri = Uri.fromFile(tempCameraFile);
                    String copiedPath = ImageUtils.copyImageToAppDirectory(this, cameraUri);
                    if (!copiedPath.isEmpty()) {
                        coverPath = copiedPath;
                        ImageUtils.displayLocalImage(ivCoverPreview, coverPath, R.drawable.travel_placeholder);
                    }
                    tempCameraFile.delete(); // Delete temporary file
                }
            }
        }
    }

    private void saveTrip() {
        String name = etTripName.getText().toString().trim();
        String destination = etDestination.getText().toString().trim();
        String startDate = tvStartDate.getText().toString().trim();
        String endDate = tvEndDate.getText().toString().trim();

        if (name.isEmpty() || destination.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, R.string.please_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        // Compare dates
        if (startDate.compareTo(endDate) > 0) {
            Toast.makeText(this, "开始日期不能晚于结束日期哦", Toast.LENGTH_SHORT).show();
            return;
        }

        Trip trip = new Trip();
        trip.setName(name);
        trip.setDestination(destination);
        trip.setStartDate(startDate);
        trip.setEndDate(endDate);
        trip.setCoverPath(coverPath);
        trip.setCreatedAt("2026-06-07");

        if (tripId == -1) {
            // New Addition
            dbHelper.addTrip(trip);
            Toast.makeText(this, "成功开启新一段旅程！", Toast.LENGTH_SHORT).show();
        } else {
            // Edit modification
            trip.setId(tripId);
            dbHelper.updateTrip(trip);
            Toast.makeText(this, "修改旅行成功！", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
