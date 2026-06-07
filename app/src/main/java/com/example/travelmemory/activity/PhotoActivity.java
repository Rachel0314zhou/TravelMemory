package com.example.travelmemory.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.PhotoAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.Photo;
import com.example.travelmemory.utils.ImageUtils;

import java.io.File;
import java.util.List;

public class PhotoActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ALBUM = 201;
    private static final int REQUEST_CODE_CAMERA = 202;

    private DatabaseHelper dbHelper;
    private int tripId = -1;

    private RecyclerView rvPhotos;
    private LinearLayout emptyState;
    private Button btnGallery, btnCamera;

    private File tempCameraFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getIntExtra("trip_id", -1);

        if (tripId == -1) {
            Toast.makeText(this, "未能定位到正确的旅行，相册无法加载", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Views Mapping
        rvPhotos = findViewById(R.id.rv_photos);
        emptyState = findViewById(R.id.photo_empty_view);
        btnGallery = findViewById(R.id.btn_import_gallery);
        btnCamera = findViewById(R.id.btn_import_camera);

        findViewById(R.id.btn_photo_back).setOnClickListener(v -> finish());

        rvPhotos.setLayoutManager(new GridLayoutManager(this, 3));

        // Album Trigger
        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_ALBUM);
        });

        // Camera trigger
        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                tempCameraFile = new File(getExternalCacheDir(), "photo_camera_temp_" + System.currentTimeMillis() + ".jpg");
                Uri tempUri = FileProvider.getUriForFile(this, "com.example.fileprovider", tempCameraFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "相机打开失败，请保证相机权限已打开", Toast.LENGTH_SHORT).show();
            }
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

            PhotoAdapter adapter = new PhotoAdapter(this, photoList,
                // Deletion trigger callbacks
                (photo, position) -> new AlertDialog.Builder(this)
                    .setTitle("删除照片")
                    .setMessage("确定要将这张照片移出当前旅行吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        dbHelper.deletePhoto(photo.getId());
                        // delete file
                        try {
                            File file = new File(photo.getImagePath());
                            if (file.exists()) {
                                file.delete();
                            }
                        } catch (Exception ignored) {}
                        loadPhotos(); // reload
                        Toast.makeText(this, "照片已删除", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show(),
                // Large picture preview click callbacks
                this::showImageLargePreview
            );
            rvPhotos.setAdapter(adapter);
        }
    }

    private void showImageLargePreview(Photo photo) {
        ImageView ivLarge = new ImageView(this);
        ivLarge.setAdjustViewBounds(true);
        ImageUtils.displayLocalImage(ivLarge, photo.getImagePath(), R.drawable.travel_placeholder);

        new AlertDialog.Builder(this)
            .setView(ivLarge)
            .create()
            .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String savedPath = "";
            if (requestCode == REQUEST_CODE_ALBUM && data != null) {
                Uri selectedUri = data.getData();
                if (selectedUri != null) {
                    savedPath = ImageUtils.copyImageToAppDirectory(this, selectedUri);
                }
            } else if (requestCode == REQUEST_CODE_CAMERA) {
                if (tempCameraFile != null && tempCameraFile.exists()) {
                    Uri cameraUri = Uri.fromFile(tempCameraFile);
                    savedPath = ImageUtils.copyImageToAppDirectory(this, cameraUri);
                    tempCameraFile.delete(); // Delete temporary cache file
                }
            }

            if (!savedPath.isEmpty()) {
                Photo photo = new Photo();
                photo.setTripId(tripId);
                photo.setDayRecordId(-1); // general photo
                photo.setImagePath(savedPath);
                photo.setCreatedAt("2026-06-07");

                dbHelper.addPhoto(photo);
                Toast.makeText(this, "照片保存成功！", Toast.LENGTH_SHORT).show();
                loadPhotos();
            } else {
                Toast.makeText(this, "添加照片失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
