package com.example.travelmemory.activity;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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

        btnGallery.setOnClickListener(v -> choosePhotosFromAlbum());

        loadPhotos();
    }

    private void choosePhotosFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "选择旅行照片"), REQUEST_CODE_ALBUM);
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
        if (requestCode != REQUEST_CODE_ALBUM || data == null) return;

        int savedCount = 0;
        ClipData clipData = data.getClipData();
        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                if (saveSelectedAlbumPhoto(clipData.getItemAt(i).getUri())) {
                    savedCount++;
                }
            }
        } else if (data.getData() != null) {
            if (saveSelectedAlbumPhoto(data.getData())) {
                savedCount++;
            }
        }

        if (savedCount > 0) {
            Toast.makeText(this, "已保存 " + savedCount + " 张照片", Toast.LENGTH_SHORT).show();
            loadPhotos();
        } else {
            Toast.makeText(this, "添加照片失败", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean saveSelectedAlbumPhoto(Uri uri) {
        String savedPath = ImageUtils.copyImageToAppDirectory(this, uri);
        if (savedPath.isEmpty()) return false;
        Photo photo = new Photo();
        photo.setTripId(tripId);
        photo.setDayRecordId(-1);
        photo.setImagePath(savedPath);
        photo.setCreatedAt(todayText());
        return dbHelper.addPhoto(photo) != -1;
    }

    private String todayText() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Calendar.getInstance().getTime());
    }
}
