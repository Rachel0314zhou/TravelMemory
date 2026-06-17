package com.example.travelmemory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.PhotoAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.Photo;
import com.example.travelmemory.utils.ImageUtils;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TravelConstants;

import java.util.List;

public class PhotoActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ALBUM = 201;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int tripId = -1;
    private int currentUserId = -1;
    private RecyclerView rvPhotos;
    private LinearLayout emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);
        if (currentUserId <= 0 || tripId <= 0 || !dbHelper.isTripOwnedByUser(tripId, currentUserId)) {
            Toast.makeText(this, "请先选择自己的旅行", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvPhotos = findViewById(R.id.rv_photos);
        emptyState = findViewById(R.id.photo_empty_view);
        Button btnGallery = findViewById(R.id.btn_import_gallery);
        findViewById(R.id.btn_photo_back).setOnClickListener(v -> finish());
        rvPhotos.setLayoutManager(new GridLayoutManager(this, 3));

        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_ALBUM);
        });

        loadPhotos();
    }

    private void loadPhotos() {
        List<Photo> photoList = dbHelper.getPhotosForTrip(tripId);
        if (photoList.isEmpty()) {
            rvPhotos.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvPhotos.setVisibility(View.VISIBLE);
            rvPhotos.setAdapter(new PhotoAdapter(this, photoList,
                    (photo, position) -> new AlertDialog.Builder(this)
                            .setTitle("移除照片记录")
                            .setMessage("确定要将这张照片从这趟旅行里移除吗？本次只移除数据库记录，不删除本地图片文件。")
                            .setPositiveButton("移除记录", (dialog, which) -> {
                                dbHelper.deletePhoto(photo.getId());
                                loadPhotos();
                                Toast.makeText(this, "照片记录已移除", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("取消", null)
                            .show(),
                    this::showImageLargePreview));
        }
    }

    private void showImageLargePreview(Photo photo) {
        ImageView ivLarge = new ImageView(this);
        ivLarge.setAdjustViewBounds(true);
        ImageUtils.displayLocalImage(ivLarge, photo.getImagePath(), R.drawable.travel_placeholder);
        new AlertDialog.Builder(this).setView(ivLarge).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        String savedPath = "";
        if (requestCode == REQUEST_CODE_ALBUM && data != null && data.getData() != null) {
            savedPath = ImageUtils.copyImageToAppDirectory(this, data.getData());
        }

        if (!savedPath.isEmpty()) {
            Photo photo = new Photo();
            photo.setTripId(tripId);
            photo.setDayRecordId(-1);
            photo.setImagePath(savedPath);
            photo.setCreatedAt("2026-06-09");
            dbHelper.addPhoto(photo);
            Toast.makeText(this, "照片保存成功", Toast.LENGTH_SHORT).show();
            loadPhotos();
        } else {
            Toast.makeText(this, "添加照片失败", Toast.LENGTH_SHORT).show();
        }
    }
}
