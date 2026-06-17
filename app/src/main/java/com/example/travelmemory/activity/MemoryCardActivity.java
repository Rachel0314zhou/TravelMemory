package com.example.travelmemory.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.Photo;
import com.example.travelmemory.model.RegretItem;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.DateUtils;
import com.example.travelmemory.utils.ImageUtils;
import com.example.travelmemory.utils.MemoryCardUtils;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TravelConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class MemoryCardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int tripId = -1;
    private int currentUserId = -1;
    private LinearLayout layoutCaptureCard;
    private LinearLayout layoutCardActions;
    private LinearLayout layoutAlbumContent;
    private GridLayout gridMemoryAlbum;
    private TextView tvAlbumEmpty;
    private TextView tabMemoryCard;
    private TextView tabTravelAlbum;
    private ImageView ivCardCover;
    private TextView tvCardTripName, tvCardDatesDest, tvCardDays;
    private TextView tvStatPhotos, tvStatSpots, tvStatExpenses, tvStatMood, tvStatBestSpot;
    private TextView tvPlanRate, tvRegretCount, tvRegretTop3, tvPackingRate;
    private TextView tvLongestNote, tvEasterEgg;
    private Button btnShare, btnGenerate;
    private File generatedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_card);

        dbHelper = new DatabaseHelper(this);
        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);
        if (currentUserId <= 0 || tripId <= 0 || !dbHelper.isTripOwnedByUser(tripId, currentUserId)) {
            Toast.makeText(this, "请先选择自己的旅行", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        layoutCaptureCard = findViewById(R.id.layout_capture_card);
        layoutCardActions = findViewById(R.id.layout_memory_card_actions);
        layoutAlbumContent = findViewById(R.id.layout_memory_album_content);
        gridMemoryAlbum = findViewById(R.id.grid_memory_album);
        tvAlbumEmpty = findViewById(R.id.tv_memory_album_empty);
        tabMemoryCard = findViewById(R.id.tab_memory_card);
        tabTravelAlbum = findViewById(R.id.tab_travel_album);
        ivCardCover = findViewById(R.id.iv_card_cover);
        tvCardTripName = findViewById(R.id.tv_card_trip_name);
        tvCardDatesDest = findViewById(R.id.tv_card_dates_dest);
        tvCardDays = findViewById(R.id.tv_card_days);
        tvStatPhotos = findViewById(R.id.tv_card_stat_photos);
        tvStatSpots = findViewById(R.id.tv_card_stat_spots);
        tvStatExpenses = findViewById(R.id.tv_card_stat_expenses);
        tvStatMood = findViewById(R.id.tv_card_stat_mood);
        tvStatBestSpot = findViewById(R.id.tv_card_stat_best_spot);
        tvPlanRate = findViewById(R.id.tv_card_plan_rate);
        tvRegretCount = findViewById(R.id.tv_card_regret_count);
        tvRegretTop3 = findViewById(R.id.tv_card_regret_top3);
        tvPackingRate = findViewById(R.id.tv_card_packing_rate);
        tvLongestNote = findViewById(R.id.tv_card_longest_note);
        tvEasterEgg = findViewById(R.id.tv_card_easter_egg);
        btnShare = findViewById(R.id.btn_share_postcard);
        btnGenerate = findViewById(R.id.btn_save_postcard_local);

        findViewById(R.id.btn_card_back).setOnClickListener(v -> finish());
        tabMemoryCard.setOnClickListener(v -> showMemoryCardTab());
        tabTravelAlbum.setOnClickListener(v -> showAlbumTab());
        btnGenerate.setOnClickListener(v -> generateCardImage());
        btnShare.setOnClickListener(v -> shareMemoryImage());
        loadPostcardData();
        loadAlbumData();
        showMemoryCardTab();
    }

    private void loadPostcardData() {
        Trip trip = dbHelper.getTripForUser(tripId, currentUserId);
        if (trip == null) {
            Toast.makeText(this, "无权生成这段旅行的回忆卡片", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvCardTripName.setText(trip.getName());
        tvCardDatesDest.setText(trip.getDestination() + " | " + trip.getStartDate() + " ~ " + trip.getEndDate());
        long days = DateUtils.getDaysBetween(trip.getStartDate(), trip.getEndDate());
        tvCardDays.setText("共度 " + days + " 天");
        ImageUtils.displayLocalImage(ivCardCover, trip.getCoverPath(), R.drawable.travel_placeholder);

        int photosCount = dbHelper.countPhotosForTrip(tripId);
        int spotsCount = dbHelper.countSpotsForTrip(tripId);
        double expenses = dbHelper.getTotalExpenseForTrip(tripId);
        String favMood = dbHelper.getMostFrequentMood(tripId);
        String heartSpot = dbHelper.getFavoriteSpot(tripId);
        int planRate = dbHelper.getPlanCompletionRate(tripId);
        int regretCount = dbHelper.countRegretsForTrip(tripId);
        int packingRate = dbHelper.getPackingCompletionRate(tripId);

        tvStatPhotos.setText(photosCount + " 张");
        tvStatSpots.setText(spotsCount + " 个");
        tvStatExpenses.setText(String.format("¥%.2f", expenses));
        tvStatMood.setText(isBlank(favMood) ? "暂无记录" : favMood);
        tvStatBestSpot.setText(isBlank(heartSpot) ? "暂无最爱" : heartSpot);
        tvPlanRate.setText(planRate + "%");
        tvRegretCount.setText(regretCount + " 个");
        tvPackingRate.setText(packingRate + "%");
        tvRegretTop3.setText(buildRegretTop3Text(dbHelper.getRegretsForTrip(tripId)));

        String note = dbHelper.getLongestNote(tripId);
        if (isBlank(note)) {
            tvLongestNote.setText("“这是一场说走就走的旅行，美好的风景已经被收藏在心里。”");
        } else {
            if (note.length() > 50) {
                note = note.substring(0, 50) + "...";
            }
            tvLongestNote.setText("“" + note + "”");
        }

        String highestCat = dbHelper.getHighestExpenseCategory(tripId);
        tvEasterEgg.setText(MemoryCardUtils.getEasterEggText(planRate, regretCount, packingRate, highestCat, favMood));
    }

    private String buildRegretTop3Text(List<RegretItem> regrets) {
        if (regrets == null || regrets.isEmpty()) {
            return "没有留下明显遗憾";
        }
        StringBuilder builder = new StringBuilder("遗憾 Top3：");
        int limit = Math.min(3, regrets.size());
        for (int i = 0; i < limit; i++) {
            if (i > 0) builder.append(" / ");
            builder.append(regrets.get(i).getTitle());
        }
        return builder.toString();
    }

    private void showMemoryCardTab() {
        layoutCaptureCard.setVisibility(View.VISIBLE);
        layoutCardActions.setVisibility(View.VISIBLE);
        layoutAlbumContent.setVisibility(View.GONE);
        tabMemoryCard.setBackgroundResource(R.drawable.btn_primary);
        tabMemoryCard.setTextColor(getResources().getColor(R.color.white));
        tabTravelAlbum.setBackgroundResource(R.drawable.btn_secondary);
        tabTravelAlbum.setTextColor(getResources().getColor(R.color.text_secondary));
    }

    private void showAlbumTab() {
        layoutCaptureCard.setVisibility(View.GONE);
        layoutCardActions.setVisibility(View.GONE);
        layoutAlbumContent.setVisibility(View.VISIBLE);
        tabMemoryCard.setBackgroundResource(R.drawable.btn_secondary);
        tabMemoryCard.setTextColor(getResources().getColor(R.color.text_secondary));
        tabTravelAlbum.setBackgroundResource(R.drawable.btn_primary);
        tabTravelAlbum.setTextColor(getResources().getColor(R.color.white));
        loadAlbumData();
    }

    private void loadAlbumData() {
        List<Photo> photos = dbHelper.getPhotosForTrip(tripId);
        gridMemoryAlbum.removeAllViews();
        if (photos == null || photos.isEmpty()) {
            tvAlbumEmpty.setVisibility(View.VISIBLE);
            gridMemoryAlbum.setVisibility(View.GONE);
            return;
        }

        tvAlbumEmpty.setVisibility(View.GONE);
        gridMemoryAlbum.setVisibility(View.VISIBLE);
        for (Photo photo : photos) {
            gridMemoryAlbum.addView(createAlbumImage(photo));
        }
    }

    private View createAlbumImage(Photo photo) {
        ImageView imageView = new ImageView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = dp(116);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dp(4), dp(4), dp(4), dp(4));
        imageView.setLayoutParams(params);
        imageView.setBackgroundResource(R.drawable.card_rounded_white);
        imageView.setClipToOutline(true);
        imageView.setContentDescription("旅行照片");
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ImageUtils.displayLocalImage(imageView, photo.getImagePath(), R.drawable.travel_placeholder);
        return imageView;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void generateCardImage() {
        btnGenerate.setEnabled(false);
        Toast.makeText(this, "正在生成图片，请稍候", Toast.LENGTH_SHORT).show();
        layoutCaptureCard.post(() -> {
            generatedImageFile = createCardImageFile();
            btnGenerate.setEnabled(true);
            if (generatedImageFile != null && generatedImageFile.exists()) {
                Toast.makeText(this, "图片已生成：" + generatedImageFile.getName(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "生成图片失败，请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareMemoryImage() {
        btnShare.setEnabled(false);
        layoutCaptureCard.post(() -> {
            if (generatedImageFile == null || !generatedImageFile.exists()) {
                generatedImageFile = createCardImageFile();
            }
            btnShare.setEnabled(true);
            if (generatedImageFile == null || !generatedImageFile.exists()) {
                Toast.makeText(this, "生成图片失败，暂时无法分享", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                Uri fileUri = FileProvider.getUriForFile(this, "com.example.fileprovider", generatedImageFile);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/png");
                intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "分享回忆"));
            } catch (Exception e) {
                Toast.makeText(this, "分享回忆失败，请检查文件权限", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File createCardImageFile() {
        try {
            Bitmap bitmap = drawCardToBitmap(layoutCaptureCard);
            if (bitmap == null) return null;
            File cacheDir = new File(getCacheDir(), "shared");
            if (!cacheDir.exists() && !cacheDir.mkdirs()) {
                return null;
            }
            File imageFile = new File(cacheDir, "travel_memory_" + tripId + "_" + System.currentTimeMillis() + ".png");
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            return imageFile;
        } catch (Exception e) {
            return null;
        }
    }

    private Bitmap drawCardToBitmap(View view) {
        int width = view.getWidth();
        int height = view.getHeight();
        if (width <= 0 || height <= 0) {
            int widthSpec = View.MeasureSpec.makeMeasureSpec(getResources().getDisplayMetrics().widthPixels, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            view.measure(widthSpec, heightSpec);
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            width = view.getMeasuredWidth();
            height = view.getMeasuredHeight();
        }
        if (width <= 0 || height <= 0) return null;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
