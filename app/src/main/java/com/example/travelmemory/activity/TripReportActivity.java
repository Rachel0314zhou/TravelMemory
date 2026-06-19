package com.example.travelmemory.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.DayRecord;
import com.example.travelmemory.model.Expense;
import com.example.travelmemory.model.Photo;
import com.example.travelmemory.model.PrePlan;
import com.example.travelmemory.model.RegretItem;
import com.example.travelmemory.model.Spot;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.ImageUtils;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TravelConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class TripReportActivity extends AppCompatActivity {

    private static final int MAX_TEMPLATE_BASE_HEIGHT = 2200;
    private static final int MAX_TEMPLATE_TIMELINE_DAYS = 3;

    private DatabaseHelper dbHelper;
    private int tripId = -1;
    private int currentUserId = -1;
    private LinearLayout layoutReportCapture;
    private ScrollView scrollTripReport;
    private File generatedReportFile;
    private final List<View> revealedSections = new ArrayList<>();

    private static class TimelineDay {
        final String date;
        final List<DayRecord> records = new ArrayList<>();
        final List<Photo> photos = new ArrayList<>();
        final List<Spot> spots = new ArrayList<>();
        final List<Expense> expenses = new ArrayList<>();

        TimelineDay(String date) {
            this.date = date;
        }

        DayRecord primaryRecord() {
            return records.isEmpty() ? null : records.get(0);
        }

        double expenseTotal() {
            double total = 0;
            for (Expense expense : expenses) {
                total += expense.getAmount();
            }
            return total;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_report);

        dbHelper = new DatabaseHelper(this);
        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);

        if (currentUserId <= 0 || tripId <= 0 || !dbHelper.isTripOwnedByUser(tripId, currentUserId)) {
            Toast.makeText(this, "请先选择自己的旅行", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        layoutReportCapture = findViewById(R.id.layout_report_capture);
        scrollTripReport = findViewById(R.id.scroll_trip_report);
        findViewById(R.id.btn_report_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_report_save).setOnClickListener(v -> saveReportImage());
        findViewById(R.id.btn_report_share).setOnClickListener(v -> shareReportImage());
        bindReport();
    }

    private void bindReport() {
        Trip trip = dbHelper.getTripForUser(tripId, currentUserId);
        if (trip == null) {
            Toast.makeText(this, "无权查看这份旅行纪念报告", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<DayRecord> dayRecords = dbHelper.getDayRecordsForTrip(tripId);
        List<Photo> photos = dbHelper.getPhotosForTrip(tripId);
        List<Spot> spots = dbHelper.getSpotsForTrip(tripId);
        List<Expense> expenses = dbHelper.getExpensesForTrip(tripId);
        List<PrePlan> plans = dbHelper.getPrePlansForTrip(tripId);
        List<RegretItem> regrets = dbHelper.getRegretsForTrip(tripId);

        hasReportContent(dayRecords, photos, spots, expenses, plans, regrets);
        String coverPath = selectCoverPath(trip, dayRecords, photos);
        bindHero(trip, dayRecords, photos, spots, coverPath);
        bindStats(dayRecords, photos, spots, expenses);
        bindTimelineByDay(dayRecords, photos, spots, expenses);
        bindHighlightsStory(dayRecords, photos, spots, expenses);
        bindPhotos(photos, coverPath);
        bindPlacesAndExpenses(spots, expenses);
        bindTravelEpilogue(plans, regrets);
        animateReportSections();
    }

    private void bindHero(Trip trip, List<DayRecord> dayRecords, List<Photo> photos, List<Spot> spots, String coverPath) {
        ImageView cover = findViewById(R.id.iv_report_cover);
        TextView title = findViewById(R.id.tv_report_title);
        TextView meta = findViewById(R.id.tv_report_meta);
        TextView summary = findViewById(R.id.tv_report_summary);

        ImageUtils.displayLocalImage(cover, coverPath, R.drawable.travel_placeholder);
        title.setText(fallback(trip.getName(), "旅行纪念报告"));
        meta.setText(fallback(trip.getDestination(), "目的地") + " · "
                + fallback(trip.getStartDate(), "开始日期") + " ~ " + fallback(trip.getEndDate(), "结束日期"));
        summary.setText("这是一份关于 " + fallback(trip.getDestination(), "这次旅行") + " 的旅行纪念报告："
                + dayRecords.size() + " 篇日记、" + photos.size() + " 张照片、"
                + spots.size() + " 个打卡点，被整理成一段可以慢慢翻看的旅行故事。");
    }

    private void bindStats(List<DayRecord> dayRecords, List<Photo> photos, List<Spot> spots, List<Expense> expenses) {
        LinearLayout container = findViewById(R.id.layout_report_stats);
        container.removeAllViews();
        LinearLayout card = createCard();
        card.addView(chapterEyebrow("Chapter 01 / 出发的概览"));
        card.addView(sectionTitle("旅行概览"));
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        grid.addView(statBlock("日记", dayRecords.size() + " 篇"));
        grid.addView(statBlock("照片", photos.size() + " 张"));
        grid.addView(statBlock("打卡", spots.size() + " 个"));
        grid.addView(statBlock("花费", money(totalExpense(expenses))));
        grid.addView(statBlock("计划完成", dbHelper.getPlanCompletionRate(tripId) + "%"));
        grid.addView(statBlock("遗憾", dbHelper.countRegretsForTrip(tripId) + " 个"));
        card.addView(grid);
        container.addView(card);
    }

    private void bindTimelineByDay(List<DayRecord> dayRecords, List<Photo> photos, List<Spot> spots, List<Expense> expenses) {
        LinearLayout container = findViewById(R.id.layout_report_timeline);
        container.removeAllViews();
        LinearLayout card = createCard();
        card.addView(chapterEyebrow("Chapter 02 / 按日期走过这段旅程"));
        card.addView(sectionTitle("旅行时间线"));

        List<TimelineDay> days = buildTimelineDays(dayRecords, photos, spots, expenses);
        if (!hasAnyTimelineContent(days)) {
            card.addView(bodyText("这趟旅行还没有可整理的日期记录，先把故事的位置留好。"));
        } else {
            for (TimelineDay day : days) {
                LinearLayout item = createSoftBlock();
                item.addView(subtitle(day.date + moodSuffix(day.primaryRecord())));
                item.addView(bodyText(timelineDayStory(day)));
                addTimelinePhoto(item, day);
                item.addView(hintText(timelineDayDetails(day)));
                card.addView(item);
            }
        }
        container.addView(card);
    }

    private void bindHighlightsStory(List<DayRecord> dayRecords, List<Photo> photos, List<Spot> spots, List<Expense> expenses) {
        LinearLayout container = findViewById(R.id.layout_report_highlights);
        container.removeAllViews();
        LinearLayout card = createCard();
        card.addView(chapterEyebrow("Chapter 03 / 值得停留的高光"));
        card.addView(sectionTitle("旅行高光"));

        List<ReportHighlight> highlights = buildNaturalHighlights(dayRecords, photos, spots, expenses);
        if (highlights.isEmpty()) {
            card.addView(bodyText("记录还不多，先不强行总结高光，把真实发生过的内容留给后续补充。"));
        } else {
            for (ReportHighlight highlight : highlights) {
                card.addView(highlightRow(highlight.title, highlight.content));
            }
        }
        container.addView(card);
    }

    private void bindTimeline(List<DayRecord> dayRecords, List<Photo> photos, List<Spot> spots, List<Expense> expenses) {
        LinearLayout container = findViewById(R.id.layout_report_timeline);
        container.removeAllViews();
        LinearLayout card = createCard();
        card.addView(sectionTitle("旅行时间线"));

        if (dayRecords.isEmpty()) {
            card.addView(bodyText("这次旅行还没有日记，报告已先整理照片、地点和花费记录。"));
        } else {
            for (DayRecord record : dayRecords) {
                LinearLayout item = createSoftBlock();
                item.addView(subtitle(record.getDate() + moodSuffix(record)));
                item.addView(bodyText(shortText(record.getNote(), 96, "这一天还没有写下日记。")));
                addMatchedPhoto(item, record, photos);
                item.addView(hintText("地点：" + namesForDate(spots, record.getDate(), "暂无打卡")
                        + " · 花费：" + money(expenseForDate(expenses, record.getDate()))));
                card.addView(item);
            }
        }
        container.addView(card);
    }

    private void bindHighlights(List<DayRecord> dayRecords, List<Photo> photos, List<Spot> spots, List<Expense> expenses) {
        LinearLayout container = findViewById(R.id.layout_report_highlights);
        container.removeAllViews();
        LinearLayout card = createCard();
        card.addView(sectionTitle("旅行高光"));
        card.addView(highlightRow("最常出现的心情", fallback(dbHelper.getMostFrequentMood(tripId), "还没有心情记录")));
        card.addView(highlightRow("最想记住的地点", fallback(dbHelper.getFavoriteSpot(tripId), favoriteSpotName(spots))));
        card.addView(highlightRow("照片最多的瞬间", photos.isEmpty() ? "还没有照片" : "精选 " + Math.min(photos.size(), 6) + " 张画面进入报告"));
        card.addView(highlightRow("主要消费", fallback(dbHelper.getHighestExpenseCategory(tripId), "还没有花费记录")));
        card.addView(highlightRow("最长的一段日记", shortText(dbHelper.getLongestNote(tripId), 80, "还没有长日记")));
        container.addView(card);
    }

    private void bindPhotos(List<Photo> photos, String coverPath) {
        LinearLayout container = findViewById(R.id.layout_report_photos);
        container.removeAllViews();
        LinearLayout card = createCard();
        card.addView(chapterEyebrow("Chapter 04 / 留下来的画面"));
        card.addView(sectionTitle("精选照片"));

        List<Photo> selectedPhotos = selectRepresentativePhotos(photos, coverPath, 4);
        if (selectedPhotos.isEmpty()) {
            card.addView(bodyText("还没有添加照片，这份报告会先用文字、地点和花费记录保留纪念感。"));
        } else {
            GridLayout grid = new GridLayout(this);
            grid.setColumnCount(2);
            for (Photo selectedPhoto : selectedPhotos) {
                ImageView imageView = new ImageView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = dp(132);
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                params.setMargins(dp(4), dp(4), dp(4), dp(4));
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setContentDescription("精选旅行照片");
                ImageUtils.displayLocalImage(imageView, selectedPhoto.getImagePath(), R.drawable.travel_placeholder);
                grid.addView(imageView);
            }
            card.addView(grid);
        }
        container.addView(card);
    }

    private void bindPhotos(List<Photo> photos) {
        LinearLayout container = findViewById(R.id.layout_report_photos);
        container.removeAllViews();
        LinearLayout card = createCard();
        card.addView(sectionTitle("精选照片"));
        if (photos.isEmpty()) {
            card.addView(bodyText("还没有添加照片，报告会先保留一个相册位置。"));
        } else {
            GridLayout grid = new GridLayout(this);
            grid.setColumnCount(2);
            int limit = Math.min(photos.size(), 6);
            for (int i = 0; i < limit; i++) {
                ImageView imageView = new ImageView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = dp(132);
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                params.setMargins(dp(4), dp(4), dp(4), dp(4));
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setContentDescription("精选旅行照片");
                ImageUtils.displayLocalImage(imageView, photos.get(i).getImagePath(), R.drawable.travel_placeholder);
                grid.addView(imageView);
            }
            card.addView(grid);
        }
        container.addView(card);
    }

    private void bindPlacesAndExpenses(List<Spot> spots, List<Expense> expenses) {
        LinearLayout container = findViewById(R.id.layout_report_places_expenses);
        container.removeAllViews();
        LinearLayout card = createCard();
        card.addView(chapterEyebrow("Chapter 05 / 经过的地方与日常"));
        card.addView(sectionTitle("地点、美食与花费"));
        card.addView(subtitle("这次去过"));
        if (spots.isEmpty()) {
            card.addView(bodyText("还没有地点或美食打卡。"));
        } else {
            int limit = Math.min(spots.size(), 5);
            for (int i = 0; i < limit; i++) {
                Spot spot = spots.get(i);
                card.addView(bodyText("· " + spot.getName() + " / " + fallback(spot.getType(), "地点")
                        + noteSuffix(spot.getNote())));
            }
        }
        card.addView(subtitle("这次花费"));
        card.addView(bodyText("总花费 " + money(totalExpense(expenses))
                + "，主要消费在 " + fallback(dbHelper.getHighestExpenseCategory(tripId), "暂无分类") + "。"));
        int limit = Math.min(expenses.size(), 4);
        for (int i = 0; i < limit; i++) {
            Expense expense = expenses.get(i);
            card.addView(hintText("· " + expense.getDate() + " " + fallback(expense.getCategory(), "其他")
                    + " " + money(expense.getAmount()) + noteSuffix(expense.getNote())));
        }
        container.addView(card);
    }

    private void bindTravelEpilogue(List<PrePlan> plans, List<RegretItem> regrets) {
        LinearLayout container = findViewById(R.id.layout_report_plans);
        container.removeAllViews();
        LinearLayout card = createCard();
        card.addView(chapterEyebrow("Chapter 06 / 旅行尾声"));
        card.addView(sectionTitle("旅行尾声"));

        int completed = dbHelper.countCompletedPrePlansForTrip(tripId);
        if (plans.isEmpty()) {
            card.addView(bodyText("这趟旅行没有提前列出计划，但路上的记录已经把它收束成一段回忆。"));
        } else {
            card.addView(bodyText("出发前写下的计划，最后完成了 " + completed + " / " + plans.size()
                    + "。完成的部分变成了旅程本身，没完成的部分留给下一次出发。"));
        }

        card.addView(subtitle("这次完成了"));
        int shown = 0;
        for (PrePlan plan : plans) {
            if (plan.getIsCompleted() == 1 && shown < 3) {
                card.addView(hintText("· " + plan.getTitle() + epiloguePlanSuffix(plan)));
                shown++;
            }
        }
        if (shown == 0) {
            card.addView(hintText("没有特别标记完成的计划，但这趟旅行仍然留下了属于它的答案。"));
        }

        card.addView(subtitle("留给下次"));
        if (regrets.isEmpty()) {
            card.addView(bodyText("没有明显遗憾，这趟旅行可以在这里安静收起。"));
        } else {
            int limit = Math.min(regrets.size(), 3);
            for (int i = 0; i < limit; i++) {
                RegretItem regret = regrets.get(i);
                card.addView(bodyText("· " + regret.getTitle() + "，" + fallback(regret.getReason(), "留给下一次出发时再完成")));
            }
        }
        container.addView(card);
    }

    private void bindPlans(List<PrePlan> plans, List<RegretItem> regrets) {
        LinearLayout container = findViewById(R.id.layout_report_plans);
        container.removeAllViews();
        LinearLayout card = createCard();
        card.addView(sectionTitle("计划与遗憾"));
        int completed = dbHelper.countCompletedPrePlansForTrip(tripId);
        card.addView(bodyText("行前计划完成 " + completed + " / " + plans.size()
                + "，完成率 " + dbHelper.getPlanCompletionRate(tripId) + "%。"));

        if (!plans.isEmpty()) {
            card.addView(subtitle("已经完成"));
            int shown = 0;
            for (PrePlan plan : plans) {
                if (plan.getIsCompleted() == 1 && shown < 3) {
                    card.addView(hintText("· " + plan.getTitle()));
                    shown++;
                }
            }
            if (shown == 0) card.addView(hintText("还没有标记完成的计划。"));
        }

        card.addView(subtitle("留待下次"));
        if (regrets.isEmpty()) {
            card.addView(bodyText("没有明显遗憾，这趟旅行已经很完整。"));
        } else {
            int limit = Math.min(regrets.size(), 3);
            for (int i = 0; i < limit; i++) {
                RegretItem regret = regrets.get(i);
                card.addView(bodyText("· " + regret.getTitle() + "，" + fallback(regret.getReason(), "留待下次完成")));
            }
        }
        container.addView(card);
    }

    private void saveReportImage() {
        Trip trip = dbHelper.getTripForUser(tripId, currentUserId);
        if (trip == null) {
            Toast.makeText(this, "保存纪念图片失败", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap = createMemorialTemplateBitmap(
                trip,
                dbHelper.getDayRecordsForTrip(tripId),
                dbHelper.getPhotosForTrip(tripId),
                dbHelper.getSpotsForTrip(tripId),
                dbHelper.getExpensesForTrip(tripId),
                dbHelper.getPrePlansForTrip(tripId),
                dbHelper.getRegretsForTrip(tripId));
        if (bitmap != null && saveBitmapToGallery(bitmap)) {
            Toast.makeText(this, "纪念图片已保存到系统相册", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "保存纪念图片失败", Toast.LENGTH_SHORT).show();
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    private void shareReportImage() {
        if (generatedReportFile == null || !generatedReportFile.exists()) {
            generatedReportFile = createTemplateImageFile();
        }
        if (generatedReportFile == null || !generatedReportFile.exists()) {
            Toast.makeText(this, "生成报告图片失败，暂时无法分享", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Uri fileUri = FileProvider.getUriForFile(this, "com.example.fileprovider", generatedReportFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "分享旅行纪念报告"));
        } catch (Exception e) {
            Toast.makeText(this, "分享报告失败，请检查文件权限", Toast.LENGTH_SHORT).show();
        }
    }

    private File createTemplateImageFile() {
        Trip trip = dbHelper.getTripForUser(tripId, currentUserId);
        if (trip == null) return null;
        Bitmap bitmap = createMemorialTemplateBitmap(
                trip,
                dbHelper.getDayRecordsForTrip(tripId),
                dbHelper.getPhotosForTrip(tripId),
                dbHelper.getSpotsForTrip(tripId),
                dbHelper.getExpensesForTrip(tripId),
                dbHelper.getPrePlansForTrip(tripId),
                dbHelper.getRegretsForTrip(tripId));
        if (bitmap == null) return null;
        try {
            File cacheDir = new File(getCacheDir(), "shared");
            if (!cacheDir.exists() && !cacheDir.mkdirs()) return null;
            File imageFile = new File(cacheDir, "trip_memorial_" + tripId + "_" + System.currentTimeMillis() + ".png");
            FileOutputStream output = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.flush();
            output.close();
            return imageFile;
        } catch (Exception e) {
            return null;
        } finally {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    private Bitmap createMemorialTemplateBitmap(Trip trip, List<DayRecord> dayRecords, List<Photo> photos,
                                                List<Spot> spots, List<Expense> expenses,
                                                List<PrePlan> plans, List<RegretItem> regrets) {
        final int width = 1080;
        final int outputWidth = 1440;
        final float outputScale = outputWidth / (float) width;
        String coverPath = selectCoverPath(trip, dayRecords, photos);
        String summary = travelSummary(photos.size(), dayRecords.size(), spots.size(), bestDiarySnippet(dayRecords, 62));
        List<String> detailPhotoPaths = collectDetailPhotoPaths(photos, coverPath);
        List<TimelineDay> timelineDays = limitTimelineDaysForExport(buildTimelineDays(dayRecords, photos, spots, expenses));
        int baseHeight = calculateTemplateBaseHeight(trip, summary, detailPhotoPaths, dayRecords, spots, expenses, plans, regrets);
        final int outputHeight = Math.round(baseHeight * outputScale);
        Bitmap bitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(outputScale, outputScale);
        canvas.drawColor(Color.rgb(247, 244, 238));

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(47, 83, 85));
        paint.setTextSize(28);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        canvas.drawText("TRAVEL MEMORY", 72, 82, paint);
        drawExportSummaryBadge(canvas, paint, photos.size(), dayRecords.size(), spots.size());

        RectF coverRect = new RectF(72, 116, 1008, 582);
        drawImageOrPlaceholder(canvas, coverPath, coverRect, "旅行封面");

        paint.setColor(Color.rgb(32, 35, 37));
        paint.setTextSize(54);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        float y = drawWrappedText(canvas, paint, fallback(trip.getName(), "这次旅行"), 72, 665, 936, 2, 62);

        paint.setColor(Color.rgb(97, 103, 106));
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        canvas.drawText(fallback(trip.getDestination(), "目的地未记录"), 72, y + 14, paint);
        canvas.drawText(fallback(trip.getStartDate(), "开始日期") + " - " + fallback(trip.getEndDate(), "结束日期"), 72, y + 54, paint);

        paint.setColor(Color.rgb(52, 55, 57));
        paint.setTextSize(32);
        y = drawWrappedText(canvas, paint, summary, 72, y + 126, 936, 3, 44);

        float statsTop = y + 28;
        drawStats(canvas, photos.size(), dayRecords.size(), spots.size(), totalExpense(expenses), statsTop);
        float photoTop = statsTop + 244;
        float momentsTop = drawAdaptivePhotoStory(canvas, detailPhotoPaths, photoTop) + 42;
        float timelineBottom = drawTimelineStory(canvas, timelineDays, momentsTop);
        drawTravelEpilogue(canvas, plans, regrets, timelineBottom + 42);

        paint.setColor(Color.rgb(47, 83, 85));
        paint.setTextSize(32);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("留给这次旅行的一份纪念", width / 2f, baseHeight - 72, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        return bitmap;
    }

    private boolean saveBitmapToGallery(Bitmap bitmap) {
        String fileName = "travel_memory_" + tripId + "_" + System.currentTimeMillis() + ".png";
        OutputStream output = null;
        Uri imageUri = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TravelMemory");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (imageUri == null) return false;
                output = getContentResolver().openOutputStream(imageUri);
                if (output == null) return false;
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
                output.flush();
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                getContentResolver().update(imageUri, values, null, null);
                return true;
            } else {
                File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File appDir = new File(picturesDir, "TravelMemory");
                if (!appDir.exists() && !appDir.mkdirs()) return false;
                File imageFile = new File(appDir, fileName);
                output = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
                output.flush();
                MediaScannerConnection.scanFile(this, new String[]{imageFile.getAbsolutePath()}, new String[]{"image/png"}, null);
                return true;
            }
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (output != null) output.close();
            } catch (Exception ignored) {
            }
        }
    }

    private String chooseCoverPath(Trip trip, List<Photo> photos) {
        if (trip != null && !isBlank(trip.getCoverPath())) return trip.getCoverPath();
        if (photos != null && !photos.isEmpty()) return photos.get(0).getImagePath();
        return "";
    }

    private void drawImageOrPlaceholder(Canvas canvas, String path, RectF rect, String placeholderText) {
        Bitmap bitmap = loadBitmap(path, Math.round(rect.width()), Math.round(rect.height()));
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (bitmap == null) {
            paint.setColor(Color.rgb(226, 232, 228));
            canvas.drawRoundRect(rect, dp(18), dp(18), paint);
            if (!isBlank(placeholderText)) {
                paint.setColor(Color.rgb(86, 108, 102));
                paint.setTextSize(34);
                paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(placeholderText, rect.centerX(), rect.centerY(), paint);
                paint.setTextAlign(Paint.Align.LEFT);
            }
            return;
        }
        Rect src = centerCropSource(bitmap, rect);
        paint.setFilterBitmap(true);
        canvas.save();
        canvas.clipRect(rect);
        canvas.drawBitmap(bitmap, src, rect, paint);
        canvas.restore();
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    private Bitmap loadBitmap(String path, int reqWidth, int reqHeight) {
        if (isBlank(path)) return null;
        File file = new File(path);
        if (!file.exists()) return null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
            return null;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height <= 0 || width <= 0) return inSampleSize;
        while ((height / inSampleSize) > reqHeight * 2 || (width / inSampleSize) > reqWidth * 2) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    private Rect centerCropSource(Bitmap bitmap, RectF target) {
        float bitmapRatio = bitmap.getWidth() / (float) bitmap.getHeight();
        float targetRatio = target.width() / target.height();
        if (bitmapRatio > targetRatio) {
            int srcWidth = Math.round(bitmap.getHeight() * targetRatio);
            int left = (bitmap.getWidth() - srcWidth) / 2;
            return new Rect(left, 0, left + srcWidth, bitmap.getHeight());
        }
        int srcHeight = Math.round(bitmap.getWidth() / targetRatio);
        int top = (bitmap.getHeight() - srcHeight) / 2;
        return new Rect(0, top, bitmap.getWidth(), top + srcHeight);
    }

    private float drawWrappedText(Canvas canvas, Paint paint, String text, float x, float y, float maxWidth,
                                  int maxLines, float lineHeight) {
        if (isBlank(text)) return y;
        String remaining = text.trim();
        int line = 0;
        while (!remaining.isEmpty() && line < maxLines) {
            int count = paint.breakText(remaining, true, maxWidth, null);
            if (count <= 0) break;
            String piece = remaining.substring(0, count);
            if (count < remaining.length()) {
                int lastBreak = Math.max(piece.lastIndexOf(' '), Math.max(piece.lastIndexOf('，'), piece.lastIndexOf('。')));
                if (lastBreak > 0) {
                    count = lastBreak + 1;
                    piece = remaining.substring(0, count);
                }
            }
            if (line == maxLines - 1 && count < remaining.length()) {
                piece = piece.trim() + "...";
            }
            canvas.drawText(piece.trim(), x, y + line * lineHeight, paint);
            remaining = remaining.substring(count).trim();
            line++;
        }
        return y + Math.max(line, 1) * lineHeight;
    }

    private void drawStats(Canvas canvas, int photoCount, int diaryCount, int spotCount, double expenseTotal, float top) {
        String[] labels = {"照片", "日记", "地点", "花费"};
        String[] values = {photoCount + " 张", diaryCount + " 篇", spotCount + " 个", money(expenseTotal)};
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        for (int i = 0; i < labels.length; i++) {
            float left = 72 + (i % 2) * 468;
            float rowTop = top + (i / 2) * 108;
            RectF rect = new RectF(left, rowTop, left + 432, rowTop + 86);
            paint.setColor(Color.rgb(238, 242, 237));
            canvas.drawRoundRect(rect, dp(16), dp(16), paint);
            paint.setColor(Color.rgb(98, 105, 108));
            paint.setTextSize(24);
            paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
            canvas.drawText(labels[i], left + 24, rowTop + 34, paint);
            paint.setColor(Color.rgb(47, 83, 85));
            paint.setTextSize(31);
            paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
            canvas.drawText(values[i], left + 120, rowTop + 35, paint);
        }
    }

    private List<String> collectDetailPhotoPaths(List<Photo> photos, String coverPath) {
        ArrayList<String> paths = new ArrayList<>();
        for (Photo photo : selectRepresentativePhotos(photos, coverPath, 3)) {
            String path = photo.getImagePath();
            if (!isBlank(path)) {
                paths.add(path);
            }
        }
        if (!paths.isEmpty()) return paths;
        for (Photo photo : photos) {
            if (paths.size() >= 3) break;
            String path = photo.getImagePath();
            if (isBlank(path)) continue;
            if (!isBlank(coverPath) && coverPath.equals(path)) continue;
            File file = new File(path);
            if (file.exists()) {
                paths.add(path);
            }
        }
        if (paths.isEmpty() && !isBlank(coverPath)) {
            File coverFile = new File(coverPath);
            if (coverFile.exists()) {
                paths.add(coverPath);
            }
        }
        return paths;
    }

    private float drawAdaptivePhotoStory(Canvas canvas, List<String> imagePaths, float top) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(32, 35, 37));
        paint.setTextSize(34);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        canvas.drawText("代表性画面", 72, top, paint);

        paint.setColor(Color.rgb(112, 118, 116));
        paint.setTextSize(24);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        canvas.drawText("把这趟旅程最值得回看的瞬间留在一起", 72, top + 38, paint);

        if (imagePaths.isEmpty()) {
            RectF emptyRect = new RectF(72, top + 70, 1008, top + 194);
            paint.setColor(Color.rgb(236, 239, 233));
            canvas.drawRoundRect(emptyRect, dp(18), dp(18), paint);
            paint.setColor(Color.rgb(86, 108, 102));
            paint.setTextSize(28);
            paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
            canvas.drawText("这次旅行的画面，先收藏在日记和地点里。", 112, top + 145, paint);
            return top + 214;
        }

        if (imagePaths.size() == 1) {
            drawImageOrPlaceholder(canvas, imagePaths.get(0), new RectF(72, top + 70, 1008, top + 334), "");
            return top + 354;
        }

        if (imagePaths.size() == 2) {
            drawImageOrPlaceholder(canvas, imagePaths.get(0), new RectF(72, top + 70, 528, top + 314), "");
            drawImageOrPlaceholder(canvas, imagePaths.get(1), new RectF(552, top + 70, 1008, top + 314), "");
            return top + 334;
        }

        drawImageOrPlaceholder(canvas, imagePaths.get(0), new RectF(72, top + 70, 552, top + 354), "");
        drawImageOrPlaceholder(canvas, imagePaths.get(1), new RectF(576, top + 70, 1008, top + 204), "");
        drawImageOrPlaceholder(canvas, imagePaths.get(2), new RectF(576, top + 220, 1008, top + 354), "");
        return top + 374;
    }

    private float drawTimelineStory(Canvas canvas, List<TimelineDay> days, float top) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float bottom = measureTimelineStoryBottom(days, top);
        RectF card = new RectF(72, top, 1008, bottom);
        paint.setColor(Color.rgb(255, 253, 248));
        canvas.drawRoundRect(card, dp(22), dp(22), paint);

        paint.setColor(Color.rgb(32, 35, 37));
        paint.setTextSize(34);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        canvas.drawText("按日期整理的旅程", 112, top + 58, paint);

        float y = top + 112;
        if (days.isEmpty()) {
            return drawMomentRow(canvas, "故事片段", "这趟旅行还没有按日期留下足够记录。", 112, y) + 20;
        }
        int limit = Math.min(days.size(), 3);
        for (int i = 0; i < limit; i++) {
            TimelineDay day = days.get(i);
            y = drawMomentRow(canvas, day.date, timelineDayStory(day), 112, y);
            y += 30;
        }
        return Math.max(bottom, y + 14);
    }

    private float drawTravelEpilogue(Canvas canvas, List<PrePlan> plans, List<RegretItem> regrets, float top) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float bottom = measureTravelEpilogueBottom(plans, regrets, top);
        RectF card = new RectF(72, top, 1008, bottom);
        paint.setColor(Color.rgb(255, 253, 248));
        canvas.drawRoundRect(card, dp(22), dp(22), paint);

        paint.setColor(Color.rgb(32, 35, 37));
        paint.setTextSize(34);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        canvas.drawText("旅行尾声", 112, top + 58, paint);

        int completed = 0;
        for (PrePlan plan : plans) {
            if (plan.getIsCompleted() == 1) completed++;
        }
        String done = plans.isEmpty()
                ? "这趟旅行没有提前写下计划，但它已经被路上的记录收束成一段回忆。"
                : "出发前的计划完成了 " + completed + " / " + plans.size() + "，完成的部分变成旅程，没完成的部分留给下一次。";
        float y = drawMomentRow(canvas, "完成与收束", done, 112, top + 112);

        String next = regrets.isEmpty()
                ? "没有明显遗憾，这份纪念可以安静收起。"
                : "留到下次的内容：" + shortText(regrets.get(0).getTitle(), 54, "下一次再出发");
        return Math.max(bottom, drawMomentRow(canvas, "留给下次", next, 112, y + 30) + 24);
    }

    private float drawTravelMoments(Canvas canvas, List<DayRecord> dayRecords, List<Spot> spots,
                                   List<Expense> expenses, float top) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float cardBottom = measureTravelMomentsBottom(dayRecords, spots, expenses, top);
        RectF card = new RectF(72, top, 1008, cardBottom);
        paint.setColor(Color.rgb(255, 253, 248));
        canvas.drawRoundRect(card, dp(22), dp(22), paint);

        paint.setColor(Color.rgb(32, 35, 37));
        paint.setTextSize(34);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        canvas.drawText("旅行片段", 112, top + 58, paint);

        float y = top + 112;
        y = drawMomentRow(canvas, "日记摘录", bestDiarySnippet(dayRecords, 78), 112, y);
        y = drawMomentRow(canvas, "去过这里", spotSummary(spots), 112, y + 30);
        String expense = expenseSummary(expenses);
        if (!isBlank(expense)) {
            y = drawMomentRow(canvas, "消费记忆", expense, 112, y + 30);
        }
        return Math.max(cardBottom, y + 44);
    }

    private float drawMomentRow(Canvas canvas, String label, String value, float x, float y) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        RectF row = new RectF(x - 22, y - 34, x + 858, y + 126);
        paint.setColor(Color.rgb(246, 248, 243));
        canvas.drawRoundRect(row, dp(14), dp(14), paint);

        paint.setColor(Color.rgb(47, 83, 85));
        paint.setTextSize(25);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        canvas.drawText(label, x, y, paint);

        paint.setColor(Color.rgb(52, 55, 57));
        paint.setTextSize(29);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        float textBottom = drawWrappedText(canvas, paint, value, x, y + 46, 840, 2, 42);
        return Math.max(textBottom, y + 126);
    }

    private int calculateTemplateBaseHeight(Trip trip, String summary, List<String> detailPhotoPaths,
                                            List<DayRecord> dayRecords, List<Spot> spots, List<Expense> expenses,
                                            List<PrePlan> plans, List<RegretItem> regrets) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(54);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        float y = measureWrappedTextBottom(paint, fallback(trip.getName(), "这次旅行"), 665, 936, 2, 62);

        paint.setTextSize(32);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        y = measureWrappedTextBottom(paint, summary, y + 126, 936, 3, 44);

        float statsTop = y + 28;
        float photoTop = statsTop + 244;
        float photoBottom = measureAdaptivePhotoStoryBottom(detailPhotoPaths, photoTop);
        float momentsTop = photoBottom + 42;
        float momentsBottom = measureTimelineStoryBottom(limitTimelineDaysForExport(buildTimelineDays(dayRecords, new ArrayList<>(), spots, expenses)), momentsTop);
        float epilogueBottom = measureTravelEpilogueBottom(plans, regrets, momentsBottom + 42);
        return Math.min(MAX_TEMPLATE_BASE_HEIGHT, Math.max(1920, Math.round(epilogueBottom + 168)));
    }

    private float measureAdaptivePhotoStoryBottom(List<String> imagePaths, float top) {
        if (imagePaths.isEmpty()) return top + 214;
        if (imagePaths.size() == 1) return top + 354;
        if (imagePaths.size() == 2) return top + 334;
        return top + 374;
    }

    private float measureTimelineStoryBottom(List<TimelineDay> days, float top) {
        float y = top + 112;
        if (days.isEmpty()) {
            return measureMomentRowBottom("这趟旅行还没有按日期留下足够记录。", y) + 64;
        }
        int limit = Math.min(days.size(), 3);
        for (int i = 0; i < limit; i++) {
            y = measureMomentRowBottom(timelineDayStory(days.get(i)), y) + 30;
        }
        return y + 44;
    }

    private float measureTravelEpilogueBottom(List<PrePlan> plans, List<RegretItem> regrets, float top) {
        String done = plans.isEmpty()
                ? "这趟旅行没有提前写下计划，但它已经被路上的记录收束成一段回忆。"
                : "出发前的计划完成了 0 / " + plans.size() + "，完成的部分变成旅程，没完成的部分留给下一次。";
        String next = regrets.isEmpty()
                ? "没有明显遗憾，这份纪念可以安静收起。"
                : "留到下次的内容：" + shortText(regrets.get(0).getTitle(), 54, "下一次再出发");
        float y = measureMomentRowBottom(done, top + 112);
        y = measureMomentRowBottom(next, y + 30);
        return y + 44;
    }

    private float measureTravelMomentsBottom(List<DayRecord> dayRecords, List<Spot> spots, List<Expense> expenses, float top) {
        float y = top + 112;
        y = measureMomentRowBottom(bestDiarySnippet(dayRecords, 78), y);
        y = measureMomentRowBottom(spotSummary(spots), y + 30);
        String expense = expenseSummary(expenses);
        if (!isBlank(expense)) {
            y = measureMomentRowBottom(expense, y + 30);
        }
        return y + 44;
    }

    private float measureMomentRowBottom(String value, float y) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(29);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        float textBottom = measureWrappedTextBottom(paint, value, y + 46, 840, 2, 42);
        return Math.max(textBottom, y + 126);
    }

    private float measureWrappedTextBottom(Paint paint, String text, float y, float maxWidth, int maxLines, float lineHeight) {
        if (isBlank(text)) return y;
        String remaining = text.trim();
        int line = 0;
        while (!remaining.isEmpty() && line < maxLines) {
            int count = paint.breakText(remaining, true, maxWidth, null);
            if (count <= 0) break;
            String piece = remaining.substring(0, count);
            if (count < remaining.length()) {
                int lastBreak = Math.max(piece.lastIndexOf(' '), Math.max(piece.lastIndexOf('，'), piece.lastIndexOf('。')));
                if (lastBreak > 0) {
                    count = lastBreak + 1;
                }
            }
            remaining = remaining.substring(count).trim();
            line++;
        }
        return y + Math.max(line, 1) * lineHeight;
    }

    private String bestDiarySnippet(List<DayRecord> dayRecords, int maxLength) {
        String best = "";
        for (DayRecord record : dayRecords) {
            String note = record.getNote();
            if (!isBlank(note) && note.length() > best.length()) {
                best = note;
            }
        }
        return shortText(best, maxLength, "这趟旅行还没有写下长日记，但已经留下了值得回看的路线。");
    }

    private String spotSummary(List<Spot> spots) {
        if (spots.isEmpty()) {
            return "还没有添加打卡地点，可以把最想记住的地方留到下一次整理。";
        }
        StringBuilder builder = new StringBuilder();
        int limit = Math.min(spots.size(), 3);
        for (int i = 0; i < limit; i++) {
            if (i > 0) builder.append(" · ");
            builder.append(fallback(spots.get(i).getName(), "未命名地点"));
        }
        return "这次去过 " + builder + "。";
    }

    private String expenseSummary(List<Expense> expenses) {
        if (expenses.isEmpty()) return "";
        return "记录了 " + expenses.size() + " 笔消费，总计 " + money(totalExpense(expenses))
                + "，也把旅途里的选择一起留了下来。";
    }

    private String travelSummary(int photoCount, int diaryCount, int spotCount, String diarySnippet) {
        return "这趟旅程留下了 " + photoCount + " 张画面、" + diaryCount
                + " 篇日记和 " + spotCount + " 个地点。" + diarySnippet;
    }

    private List<TimelineDay> buildTimelineDays(List<DayRecord> dayRecords, List<Photo> photos,
                                                List<Spot> spots, List<Expense> expenses) {
        TreeMap<String, TimelineDay> dayMap = new TreeMap<>();
        for (DayRecord record : dayRecords) {
            String date = normalizeDate(record.getDate(), record.getCreatedAt());
            dayFor(dayMap, date).records.add(record);
        }
        for (Photo photo : photos) {
            String date = dateForPhoto(photo, dayRecords);
            dayFor(dayMap, date).photos.add(photo);
        }
        for (Spot spot : spots) {
            String date = normalizeDate(spot.getCreatedAt(), "");
            dayFor(dayMap, date).spots.add(spot);
        }
        for (Expense expense : expenses) {
            String date = normalizeDate(expense.getDate(), "");
            dayFor(dayMap, date).expenses.add(expense);
        }
        return new ArrayList<>(dayMap.values());
    }

    private TimelineDay dayFor(TreeMap<String, TimelineDay> dayMap, String date) {
        TimelineDay day = dayMap.get(date);
        if (day == null) {
            day = new TimelineDay(date);
            dayMap.put(date, day);
        }
        return day;
    }

    private String timelineDayStory(TimelineDay day) {
        DayRecord record = day.primaryRecord();
        if (record != null && !isBlank(record.getNote())) {
            StringBuilder story = new StringBuilder(shortText(record.getNote(), 110, ""));
            if (!day.spots.isEmpty()) {
                story.append(" 这一天还经过了 ").append(joinSpotNames(day.spots, 2)).append("。");
            }
            return story.toString();
        }
        if (!day.spots.isEmpty()) return "这一天主要留在地点记录里，去过 " + joinSpotNames(day.spots, 3) + "。";
        if (!day.photos.isEmpty()) return "这一天没有写下长日记，但照片已经把路上的画面保存了下来。";
        if (!day.expenses.isEmpty()) return "这一天留下了 " + day.expenses.size() + " 笔消费记录，也是一段真实行程的痕迹。";
        return "这一天的记录还很轻，先作为旅程中的一个停顿。";
    }

    private String timelineDayDetails(TimelineDay day) {
        ArrayList<String> parts = new ArrayList<>();
        if (!day.photos.isEmpty()) parts.add(day.photos.size() + " 张照片");
        if (!day.spots.isEmpty()) parts.add("地点：" + joinSpotNames(day.spots, 3));
        if (!day.expenses.isEmpty()) parts.add("花费：" + money(day.expenseTotal()));
        return parts.isEmpty() ? "这一天还可以继续补充照片、地点或花费。" : joinText(parts, " · ");
    }

    private void addTimelinePhoto(LinearLayout item, TimelineDay day) {
        Photo photo = firstUsablePhoto(day.photos);
        if (photo == null) return;
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(150)));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setContentDescription("时间线照片");
        ImageUtils.displayLocalImage(imageView, photo.getImagePath(), R.drawable.travel_placeholder);
        item.addView(imageView);
    }

    private List<ReportHighlight> buildNaturalHighlights(List<DayRecord> dayRecords, List<Photo> photos,
                                                         List<Spot> spots, List<Expense> expenses) {
        ArrayList<ReportHighlight> highlights = new ArrayList<>();
        String mood = dbHelper.getMostFrequentMood(tripId);
        if (!isBlank(mood) && !dayRecords.isEmpty()) {
            highlights.add(new ReportHighlight("最常出现的心情", "这趟旅行最常出现的心情是「" + mood + "」，它给整段回忆定下了基调。"));
        }
        String favoriteSpot = favoriteSpotName(spots);
        if (!isBlank(favoriteSpot) && !spots.isEmpty()) {
            highlights.add(new ReportHighlight("最喜欢景点", "最值得记住的地方，是 " + favoriteSpot + "，它让这趟路有了清晰的落点。"));
        }
        if (!photos.isEmpty()) {
            highlights.add(new ReportHighlight("精选照片", "照片没有被全部堆进报告，只选出 " + selectRepresentativePhotos(photos, "", 6).size()
                    + " 张代表画面，让回忆保持干净。"));
        }
        String category = dbHelper.getHighestExpenseCategory(tripId);
        if (!isBlank(category) && !expenses.isEmpty()) {
            highlights.add(new ReportHighlight("花费最高项目", "花费主要落在「" + category + "」，这些选择也构成了旅途的日常部分。"));
        }
        String longest = dbHelper.getLongestNote(tripId);
        if (!isBlank(longest)) {
            highlights.add(new ReportHighlight("最长日记", "最长的一段日记写下了：" + shortText(longest, 72, "")));
        }
        return highlights;
    }

    private static class ReportHighlight {
        final String title;
        final String content;

        ReportHighlight(String title, String content) {
            this.title = title;
            this.content = content;
        }
    }

    private String selectCoverPath(Trip trip, List<DayRecord> dayRecords, List<Photo> photos) {
        if (trip != null && isUsablePath(trip.getCoverPath())) return trip.getCoverPath();
        for (DayRecord record : dayRecords) {
            for (Photo photo : photos) {
                if (photo.getDayRecordId() == record.getId() && isUsablePath(photo.getImagePath())) {
                    return photo.getImagePath();
                }
            }
        }
        for (Photo photo : photos) {
            if (isUsablePath(photo.getImagePath())) return photo.getImagePath();
        }
        return "";
    }

    private List<Photo> selectRepresentativePhotos(List<Photo> photos, String coverPath, int maxCount) {
        ArrayList<Photo> selected = new ArrayList<>();
        ArrayList<String> seenPaths = new ArrayList<>();
        for (Photo photo : photos) {
            if (selected.size() >= maxCount) break;
            String path = photo.getImagePath();
            if (isBlank(path) || seenPaths.contains(path)) continue;
            if (!isBlank(coverPath) && coverPath.equals(path) && photos.size() > 1) continue;
            selected.add(photo);
            seenPaths.add(path);
        }
        if (selected.isEmpty()) {
            for (Photo photo : photos) {
                String path = photo.getImagePath();
                if (!isBlank(path) && !seenPaths.contains(path)) {
                    selected.add(photo);
                    break;
                }
            }
        }
        return selected;
    }

    private String epiloguePlanSuffix(PrePlan plan) {
        if (!isBlank(plan.getLocation())) return "（" + plan.getLocation() + "）";
        if (!isBlank(plan.getTargetDate())) return "（" + plan.getTargetDate() + "）";
        return "";
    }

    private String dateForPhoto(Photo photo, List<DayRecord> dayRecords) {
        for (DayRecord record : dayRecords) {
            if (photo.getDayRecordId() == record.getId()) {
                return normalizeDate(record.getDate(), record.getCreatedAt());
            }
        }
        return normalizeDate(photo.getCreatedAt(), "");
    }

    private String normalizeDate(String primary, String fallbackDate) {
        if (!isBlank(primary)) return primary.trim();
        if (!isBlank(fallbackDate)) return fallbackDate.trim();
        return "未标日期";
    }

    private Photo firstUsablePhoto(List<Photo> photos) {
        for (Photo photo : photos) {
            if (!isBlank(photo.getImagePath())) return photo;
        }
        return null;
    }

    private String joinSpotNames(List<Spot> spots, int maxCount) {
        ArrayList<String> names = new ArrayList<>();
        for (Spot spot : spots) {
            if (names.size() >= maxCount) break;
            if (!isBlank(spot.getName())) names.add(spot.getName());
        }
        return names.isEmpty() ? "未命名地点" : joinText(names, "、");
    }

    private String joinText(List<String> values, String separator) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (builder.length() > 0) builder.append(separator);
            builder.append(value);
        }
        return builder.toString();
    }

    private boolean isUsablePath(String path) {
        return !isBlank(path) && new File(path).exists();
    }

    private boolean hasReportContent(List<DayRecord> dayRecords, List<Photo> photos, List<Spot> spots,
                                     List<Expense> expenses, List<PrePlan> plans, List<RegretItem> regrets) {
        return !dayRecords.isEmpty()
                || !photos.isEmpty()
                || !spots.isEmpty()
                || !expenses.isEmpty()
                || !plans.isEmpty()
                || !regrets.isEmpty();
    }

    private boolean hasAnyTimelineContent(List<TimelineDay> days) {
        for (TimelineDay day : days) {
            if (!day.records.isEmpty() || !day.photos.isEmpty() || !day.spots.isEmpty() || !day.expenses.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private List<TimelineDay> limitTimelineDaysForExport(List<TimelineDay> days) {
        ArrayList<TimelineDay> limited = new ArrayList<>();
        for (TimelineDay day : days) {
            if (limited.size() >= MAX_TEMPLATE_TIMELINE_DAYS) break;
            limited.add(day);
        }
        return limited;
    }

    private void drawExportSummaryBadge(Canvas canvas, Paint paint, int photoCount, int diaryCount, int spotCount) {
        RectF badge = new RectF(724, 48, 1008, 96);
        paint.setColor(Color.rgb(238, 242, 237));
        canvas.drawRoundRect(badge, dp(18), dp(18), paint);
        paint.setColor(Color.rgb(47, 83, 85));
        paint.setTextSize(22);
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("分享摘要 · " + photoCount + "/" + diaryCount + "/" + spotCount, badge.centerX(), 80, paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void animateReportSections() {
        setupScrollRevealSections();
    }

    private void setupScrollRevealSections() {
        revealedSections.clear();
        for (int i = 0; i < layoutReportCapture.getChildCount(); i++) {
            View child = layoutReportCapture.getChildAt(i);
            child.setAlpha(0f);
            child.setTranslationY(dp(24));
        }
        scrollTripReport.getViewTreeObserver().addOnScrollChangedListener(this::revealVisibleSections);
        scrollTripReport.post(this::revealVisibleSections);
    }

    private void revealVisibleSections() {
        for (int i = 0; i < layoutReportCapture.getChildCount(); i++) {
            View section = layoutReportCapture.getChildAt(i);
            if (revealedSections.contains(section)) continue;
            if (isSectionInRevealRange(section)) {
                revealedSections.add(section);
                section.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(280L)
                        .start();
            }
        }
    }

    private boolean isSectionInRevealRange(View section) {
        int scrollTop = scrollTripReport.getScrollY();
        int scrollBottom = scrollTop + scrollTripReport.getHeight();
        int triggerBottom = scrollBottom - dp(36);
        return section.getTop() < triggerBottom && section.getBottom() > scrollTop;
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.card_rounded_white));
        return card;
    }

    private LinearLayout createSoftBlock() {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(dp(12), dp(12), dp(12), dp(12));
        block.setBackground(ContextCompat.getDrawable(this, R.drawable.card_rounded_primary_light));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(10), 0, 0);
        block.setLayoutParams(params);
        return block;
    }

    private TextView sectionTitle(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        view.setTextSize(18);
        view.setTypeface(null, Typeface.BOLD);
        return view;
    }

    private TextView chapterEyebrow(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        view.setTextSize(12);
        view.setTypeface(null, Typeface.BOLD);
        view.setPadding(0, 0, 0, dp(6));
        return view;
    }

    private TextView subtitle(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        view.setTextSize(15);
        view.setTypeface(null, Typeface.BOLD);
        view.setPadding(0, dp(12), 0, dp(4));
        return view;
    }

    private TextView bodyText(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        view.setTextSize(14);
        view.setLineSpacing(dp(2), 1f);
        view.setPadding(0, dp(6), 0, 0);
        return view;
    }

    private TextView hintText(String text) {
        TextView view = bodyText(text);
        view.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        view.setTextSize(13);
        return view;
    }

    private View statBlock(String label, String value) {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setGravity(Gravity.CENTER);
        block.setPadding(dp(10), dp(12), dp(10), dp(12));
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(dp(4), dp(10), dp(4), 0);
        block.setLayoutParams(params);
        block.setBackground(ContextCompat.getDrawable(this, R.drawable.card_rounded_primary_light));
        block.addView(hintText(label));
        TextView valueView = bodyText(value);
        valueView.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        valueView.setTextSize(18);
        valueView.setTypeface(null, Typeface.BOLD);
        block.addView(valueView);
        return block;
    }

    private View highlightRow(String label, String value) {
        LinearLayout row = createSoftBlock();
        row.addView(subtitle(label));
        row.addView(bodyText(value));
        return row;
    }

    private void addMatchedPhoto(LinearLayout item, DayRecord record, List<Photo> photos) {
        Photo photo = null;
        for (Photo candidate : photos) {
            if (candidate.getDayRecordId() == record.getId()) {
                photo = candidate;
                break;
            }
        }
        if (photo == null && !photos.isEmpty()) {
            photo = photos.get(0);
        }
        if (photo == null) return;
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(150)));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setContentDescription("时间线照片");
        ImageUtils.displayLocalImage(imageView, photo.getImagePath(), R.drawable.travel_placeholder);
        item.addView(imageView);
    }

    private String namesForDate(List<Spot> spots, String date, String emptyText) {
        StringBuilder builder = new StringBuilder();
        for (Spot spot : spots) {
            if (date != null && date.equals(spot.getCreatedAt())) {
                if (builder.length() > 0) builder.append("、");
                builder.append(spot.getName());
            }
        }
        return builder.length() == 0 ? emptyText : builder.toString();
    }

    private double expenseForDate(List<Expense> expenses, String date) {
        double total = 0;
        for (Expense expense : expenses) {
            if (date != null && date.equals(expense.getDate())) {
                total += expense.getAmount();
            }
        }
        return total;
    }

    private double totalExpense(List<Expense> expenses) {
        double total = 0;
        for (Expense expense : expenses) {
            total += expense.getAmount();
        }
        return total;
    }

    private String favoriteSpotName(List<Spot> spots) {
        for (Spot spot : spots) {
            if (spot.getIsFavorite() == 1) return spot.getName();
        }
        return spots.isEmpty() ? "还没有地点记录" : spots.get(0).getName();
    }

    private String moodSuffix(DayRecord record) {
        if (record == null || isBlank(record.getMood())) return "";
        return isBlank(record.getMood()) ? "" : " · " + record.getMood();
    }

    private String noteSuffix(String note) {
        return isBlank(note) ? "" : "，" + note;
    }

    private String money(double amount) {
        return String.format(Locale.getDefault(), "¥ %.2f", amount);
    }

    private String fallback(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private String shortText(String value, int maxLength, String fallback) {
        if (isBlank(value)) return fallback;
        return value.length() <= maxLength ? value : value.substring(0, maxLength) + "...";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
