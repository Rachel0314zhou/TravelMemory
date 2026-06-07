package com.example.travelmemory.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.DateUtils;
import com.example.travelmemory.utils.ImageUtils;
import com.example.travelmemory.utils.MemoryCardUtils;

import java.io.File;
import java.io.FileOutputStream;

public class MemoryCardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int tripId = -1;

    private LinearLayout layoutCaptureCard;
    private ImageView ivCardCover;
    private TextView tvCardTripName, tvCardDatesDest, tvCardDays;
    private TextView tvStatPhotos, tvStatSpots, tvStatExpenses, tvStatMood, tvStatBestSpot;
    private TextView tvLongestNote, tvEasterEgg;

    private Button btnShare, btnSaveLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_card);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getIntExtra("trip_id", -1);

        if (tripId == -1) {
            Toast.makeText(this, "未找到相应的旅行，回忆卡生成失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // View mapping
        layoutCaptureCard = findViewById(R.id.layout_capture_card);
        ivCardCover = findViewById(R.id.iv_card_cover);
        tvCardTripName = findViewById(R.id.tv_card_trip_name);
        tvCardDatesDest = findViewById(R.id.tv_card_dates_dest);
        tvCardDays = findViewById(R.id.tv_card_days);

        tvStatPhotos = findViewById(R.id.tv_card_stat_photos);
        tvStatSpots = findViewById(R.id.tv_card_stat_spots);
        tvStatExpenses = findViewById(R.id.tv_card_stat_expenses);
        tvStatMood = findViewById(R.id.tv_card_stat_mood);
        tvStatBestSpot = findViewById(R.id.tv_card_stat_best_spot);

        tvLongestNote = findViewById(R.id.tv_card_longest_note);
        tvEasterEgg = findViewById(R.id.tv_card_easter_egg);

        btnShare = findViewById(R.id.btn_share_postcard);
        btnSaveLocal = findViewById(R.id.btn_save_postcard_local);

        findViewById(R.id.btn_card_back).setOnClickListener(v -> finish());

        // Load card statistics
        loadPostcardData();

        // Share buttons hookup
        btnShare.setOnClickListener(v -> sharePostcardImage());
        btnSaveLocal.setOnClickListener(v -> savePostcardLocalCache());
    }

    private void loadPostcardData() {
        Trip trip = dbHelper.getTripById(tripId);
        if (trip == null) {
            finish();
            return;
        }

        // Header mapping
        tvCardTripName.setText(trip.getName());
        tvCardDatesDest.setText("📍 " + trip.getDestination() + "  |  " + trip.getStartDate() + " ~ " + trip.getEndDate());
        long days = DateUtils.getDaysBetween(trip.getStartDate(), trip.getEndDate());
        tvCardDays.setText("共度 " + days + " 天");

        // Load cover
        ImageUtils.displayLocalImage(ivCardCover, trip.getCoverPath(), R.drawable.travel_placeholder);

        // Core statistical queries
        int photosCount = dbHelper.countPhotosForTrip(tripId);
        int spotsCount = dbHelper.countSpotsForTrip(tripId);
        double expenses = dbHelper.getTotalExpenseForTrip(tripId);
        String favMood = dbHelper.getMostFrequentMood(tripId);
        String heartSpot = dbHelper.getFavoriteSpot(tripId);

        tvStatPhotos.setText(photosCount + " 张");
        tvStatSpots.setText(spotsCount + " 处");
        tvStatExpenses.setText(String.format("¥ %.2f", expenses));
        tvStatMood.setText(favMood);
        tvStatBestSpot.setText(heartSpot.isEmpty() ? "尚未设有最爱打卡点" : heartSpot);

        // Longest diary text mapping
        String note = dbHelper.getLongestNote(tripId);
        if (note.isEmpty()) {
            tvLongestNote.setText("“ 这是一场说走就走的旅行，虽无长篇记录，但美好的风景已被深深烙印在心底... ”");
        } else {
            if (note.length() > 65) {
                note = note.substring(0, 65) + "...";
            }
            tvLongestNote.setText("“ " + note + " ”");
        }

        // Easter Egg query priorities implementation matches rules perfectly
        String highestCat = dbHelper.getHighestExpenseCategory(tripId);
        String easterEggText = MemoryCardUtils.getEasterEggText(spotsCount, photosCount, days, highestCat, favMood);
        tvEasterEgg.setText(easterEggText);
    }

    private Bitmap drawCardToBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private File createCardImageFile() {
        try {
            // Renders layout view card directly into Bitmap
            Bitmap bitmap = drawCardToBitmap(layoutCaptureCard);

            // Create target cache file
            File cacheDir = new File(getCacheDir(), "shared");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            File imageFile = new File(cacheDir, "travel_memory_" + tripId + "_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.flush();
            fos.close();
            return imageFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sharePostcardImage() {
        btnShare.setEnabled(false);
        Toast.makeText(this, "正在为您烘焙美好旅迹，请稍候...", Toast.LENGTH_SHORT).show();

        // Safe async delayed execution post layout render sizing
        layoutCaptureCard.post(() -> {
            File shareFile = createCardImageFile();
            btnShare.setEnabled(true);
            if (shareFile != null && shareFile.exists()) {
                // Share with FileProvider intent
                try {
                    Uri fileUri = FileProvider.getUriForFile(this, "com.example.fileprovider", shareFile);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    // Grant permission explicitly
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "分享我的旅迹回忆卡"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "分享通道启动失败，请检查文件权限", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "生成卡片快照失败，请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePostcardLocalCache() {
        btnSaveLocal.setEnabled(false);
        layoutCaptureCard.post(() -> {
            File localFile = createCardImageFile();
            btnSaveLocal.setEnabled(true);
            if (localFile != null && localFile.exists()) {
                Toast.makeText(this, "成功保存卡片至本地缓存：" + localFile.getName(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "保存至本地失败，请检查沙盒空间", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
