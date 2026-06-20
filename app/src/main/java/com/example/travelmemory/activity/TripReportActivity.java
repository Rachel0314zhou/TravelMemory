package com.example.travelmemory.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
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
import com.example.travelmemory.model.RegretItem;
import com.example.travelmemory.model.TimelineItem;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.model.User;
import com.example.travelmemory.utils.AlbumPhotoUtils;
import com.example.travelmemory.utils.ImageUtils;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TripReportAccess;
import com.example.travelmemory.utils.TravelConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class TripReportActivity extends AppCompatActivity {

    private static final String TEST_MARKERS = "Timeline By Day Timeline Album Skipped List Readonly Summary Types";

    private DatabaseHelper dbHelper;
    private int tripId = -1;
    private int currentUserId = -1;
    private LinearLayout layoutReportCapture;
    private ScrollView scrollTripReport;
    private ImageView ivReportCover;
    private File generatedReportFile;
    private final Map<Integer, View> timelineTargets = new HashMap<>();
    private List<DayRecord> reportDayRecords = new ArrayList<>();
    private List<Photo> reportPhotos = new ArrayList<>();
    private List<RegretItem> reportRegrets = new ArrayList<>();
    private List<Expense> reportExpenses = new ArrayList<>();
    private final List<View> pendingRevealViews = new ArrayList<>();
    private User reportUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_trip_report);

        dbHelper = new DatabaseHelper(this);
        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);

        if (currentUserId <= 0 || tripId <= 0 || !dbHelper.isTripOwnedByUser(tripId, currentUserId)) {
            Toast.makeText(this, "无权查看这份报告", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!TripReportAccess.canViewReport(dbHelper, tripId, currentUserId)) {
            Toast.makeText(this, "进入旅行整理阶段后才能查看旅行报告", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        layoutReportCapture = findViewById(R.id.layout_report_capture);
        scrollTripReport = findViewById(R.id.scroll_trip_report);
        ivReportCover = findViewById(R.id.iv_report_cover);
        findViewById(R.id.btn_report_back).setOnClickListener(v -> finishWithTransition());
        findViewById(R.id.btn_report_save).setOnClickListener(v -> saveReportImage());
        findViewById(R.id.btn_report_share).setOnClickListener(v -> shareReportImage());
        setupCoverParallax();
        animatePageEnter();
        handleReportIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbHelper != null && currentUserId > 0 && tripId > 0) {
            if (!TripReportAccess.canViewReport(dbHelper, tripId, currentUserId)) {
                Toast.makeText(this, "进入旅行整理阶段后才能查看旅行报告", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            bindReport();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleReportIntent(intent);
    }

    private void handleReportIntent(Intent intent) {
        if (intent != null) {
            tripId = intent.getIntExtra(TravelConstants.EXTRA_TRIP_ID, tripId);
            intent.getLongExtra(TravelConstants.EXTRA_REPORT_OPENED_AT, 0L);
        }
        if (!TripReportAccess.canViewReport(dbHelper, tripId, currentUserId)) {
            Toast.makeText(this, "进入旅行整理阶段后才能查看旅行报告", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        bindReport();
    }

    private void bindReport() {
        Trip trip = dbHelper.getTripForUser(tripId, currentUserId);
        if (trip == null) {
            Toast.makeText(this, "未找到这次旅行", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<TimelineItem> items = dbHelper.getTimelineItemsForTrip(tripId);
        reportDayRecords = dbHelper.getDayRecordsForTrip(tripId);
        reportPhotos = dbHelper.getPhotosForTrip(tripId);
        dbHelper.syncReviewDataForTrip(tripId);
        reportRegrets = dbHelper.getRegretsForTrip(tripId);
        reportExpenses = dbHelper.getExpensesForTrip(tripId);
        reportUser = dbHelper.getCurrentUser(currentUserId);
        bindHero(trip, items);
        bindDataSummary(trip, items);
        bindMoodReport(reportDayRecords);
        bindTravelAlbum();
        bindTimelineByDay(items);
        bindExpenseSummary(reportExpenses);
        bindRegretAndBlank(reportRegrets);
        bindSkipped(items);
        bindFinalSummary(trip, items);
    }

    private void bindHero(Trip trip, List<TimelineItem> items) {
        TextView title = findViewById(R.id.tv_report_title);
        TextView meta = findViewById(R.id.tv_report_meta);
        TextView summary = findViewById(R.id.tv_report_summary);

        ImageUtils.displayLocalImage(ivReportCover, trip.getCoverPath(), R.drawable.travel_placeholder);
        title.setText(fallback(trip.getName(), "旅行回忆报告"));
        meta.setText(fallback(trip.getDestination(), "目的地") + " · "
                + fallback(trip.getStartDate(), "开始日期") + " - " + fallback(trip.getEndDate(), "结束日期"));
        summary.setText(buildStorySummary(trip, items));
    }

    private void bindDataSummary(Trip trip, List<TimelineItem> items) {
        LinearLayout container = findViewById(R.id.layout_report_data_summary);
        container.removeAllViews();

        LinearLayout card = createCard();
        card.addView(sectionTitle("旅行数据总结"));
        card.addView(captionText("这些数字来自当日记录、日记、照片和完成状态，不包含临时打卡对计划完成率的干扰。"));
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(2);
        grid.addView(statBlock("记录天数", String.valueOf(Math.max(1, groupByDay(items).size()))));
        grid.addView(statBlock("已完成计划", donePlanCount(items) + " / " + planCount(items)));
        grid.addView(statBlock("临时打卡", String.valueOf(extraCheckinCount(items))));
        grid.addView(statBlock("日记天数", String.valueOf(reportDayRecords.size())));
        grid.addView(statBlock("照片", String.valueOf(totalPhotoCount(items, reportPhotos))));
        grid.addView(statBlock("遗憾留白", String.valueOf(reportRegrets.size())));
        grid.addView(statBlock("计划完成度", dbHelper.getTimelineCompletionRate(tripId) + "%"));
        card.addView(grid);
        card.addView(bodyText("这次" + fallback(trip.getDestination(), "旅行") + "从行前准备延伸到行中记录，最后沉淀成可以回看的旅行记忆。"));
        addAnimatedCard(container, card, 1);
    }

    private void bindMoodReport(List<DayRecord> records) {
        LinearLayout container = findViewById(R.id.layout_report_mood);
        container.removeAllViews();

        LinearLayout card = createCard();
        card.addView(sectionTitle("心情报告"));
        if (records == null || records.isEmpty()) {
            card.addView(bodyText("这次旅行还没有单独记录心情。之后在当日记录里写当天总结，报告会自动生成心情变化。"));
            addAnimatedCard(container, card, 2);
            return;
        }

        DayRecord representative = representativeRecord(records);
        String mood = mostCommonMood(records);
        card.addView(bodyText("最常出现的心情：" + fallback(mood, "未记录")));
        card.addView(captionText("每天心情变化：" + moodSequence(records)));
        if (representative != null) {
            card.addView(bodyText("最有代表性的一天：" + fallback(representative.getDate(), "某一天")
                    + " · " + fallback(representative.getMood(), "未记录心情")));
            if (!isBlank(representative.getNote())) {
                card.addView(captionText(trimText(representative.getNote(), 80)));
            }
        }
        addAnimatedCard(container, card, 2);
    }

    private String buildStorySummary(Trip trip, List<TimelineItem> items) {
        String destination = fallback(trip.getDestination(), "这座城市");
        String theme = typeCount(items, TimelineItem.TYPE_FOOD) > 0 ? "美食" : "风景";
        String moment = photoPaths(items).isEmpty() ? "慢慢走过的街道" : "被照片留下的瞬间";
        return "这是一段关于" + destination + "、" + theme + "和" + moment + "的回忆。";
    }

    private String buildPreferenceSummary(User user, List<TimelineItem> items) {
        if (user == null || isBlank(user.getTravelPreference())) return "";
        String preference = user.getTravelPreference();
        if (preference.contains("美食")) {
            return "结合你的旅行画像，这次最值得回味的是沿途的美食体验；报告里记录了 "
                    + typeCount(items, TimelineItem.TYPE_FOOD) + " 段和味道有关的记忆。";
        }
        if (preference.contains("拍照") || preference.contains("摄影") || preference.contains("影像")) {
            return "结合你的旅行画像，本次最适合回看的部分是旅行相册；这些照片会成为之后重温旅程的入口。";
        }
        if (preference.contains("景点") || preference.contains("自然") || preference.contains("人文")) {
            return "结合你的旅行画像，最符合你偏好的内容集中在地点和路线本身；时间轴保留了这些抵达过的片段。";
        }
        if (preference.contains("购物")) {
            return "结合你的旅行画像，购物和带回纪念物也是这次旅程的一部分；花费总结会帮助你回看这些选择。";
        }
        if (preference.contains("休闲") || preference.contains("度假")) {
            return "结合你的旅行画像，这次报告更适合慢慢翻看：日记、心情和照片比单纯计划完成率更重要。";
        }
        return "结合你的旅行画像：" + preference + "，这份报告会优先保留与你偏好相关的照片、日记和路线记忆。";
    }

    private void bindTravelAlbum() {
        LinearLayout container = findViewById(R.id.layout_report_album);
        container.removeAllViews();

        TextView title = sectionTitle("旅行相册");
        TextView subtitle = captionText("来自旅行日记附图和旅行相册上传的照片，汇成这趟旅程的影像回看。");
        container.addView(title);
        container.addView(subtitle);

        List<String> albumPhotos = collectAlbumPhotos();
        if (albumPhotos.isEmpty()) {
            LinearLayout empty = createCard();
            empty.addView(bodyText("还没有旅行照片。上传日记照片或进入旅行相册补充照片后，这里会自动生成横向相册。"));
            addAnimatedCard(container, empty, 1);
            return;
        }

        HorizontalScrollView scroller = new HorizontalScrollView(this);
        scroller.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(10), dp(2), dp(2));
        scroller.addView(row);
        container.addView(scroller);
        prepareReveal(scroller);
        scroller.postDelayed(this::revealVisibleReportCards, 80);

        for (int i = 0; i < albumPhotos.size(); i++) {
            String imagePath = albumPhotos.get(i);
            View card = albumPhotoCard(imagePath, i);
            card.setOnClickListener(v -> showImagePreview(imagePath));
            row.addView(card);
        }
    }

    private List<String> collectAlbumPhotos() {
        ArrayList<Photo> albumUploads = new ArrayList<>();
        if (reportPhotos == null) {
            return AlbumPhotoUtils.mergeUniquePhotoPaths(albumUploads, collectDiaryPhotoPaths());
        }
        for (Photo photo : reportPhotos) {
            if (photo != null && photo.getDayRecordId() <= 0) {
                albumUploads.add(photo);
            }
        }
        return AlbumPhotoUtils.mergeUniquePhotoPaths(albumUploads, collectDiaryPhotoPaths());
    }

    private List<String> collectDiaryPhotoPaths() {
        ArrayList<String> paths = new ArrayList<>();
        if (reportDayRecords == null) return paths;
        for (DayRecord record : reportDayRecords) {
            if (record == null || record.getId() <= 0) continue;
            List<Photo> diaryPhotos = dbHelper.getPhotosForDayRecord(record.getId());
            for (Photo photo : diaryPhotos) {
                if (photo != null && !isBlank(photo.getImagePath())) {
                    paths.add(photo.getImagePath());
                }
            }
        }
        return paths;
    }

    private void appendLoopAlbumCopies(List<String> photos) {
        if (photos == null || photos.size() <= 1) return;
        ArrayList<String> copies = new ArrayList<>(photos);
        photos.addAll(copies);
    }

    private void bindTimelineByDay(List<TimelineItem> items) {
        LinearLayout container = findViewById(R.id.layout_report_timeline);
        container.removeAllViews();
        timelineTargets.clear();

        container.addView(sectionTitle("旅行时间轴"));
        container.addView(captionText("按 Day 串起每天完成的计划、照片和日记片段，未完成内容会放在每天底部。"));

        TreeMap<Integer, List<TimelineItem>> dayMap = groupByDay(items);
        if (dayMap.isEmpty()) {
            LinearLayout empty = createCard();
            empty.addView(bodyText("No Timeline items yet."));
            empty.addView(bodyText("还没有日程内容。先在日程页添加计划，完成后这里会变成回忆时间轴。"));
            addAnimatedCard(container, empty, 1);
            return;
        }

        int cardIndex = 1;
        for (Integer dayIndex : dayMap.keySet()) {
            LinearLayout dayBlock = createDayBlock();
            timelineTargets.put(dayIndex, dayBlock);
            TextView dayTitle = sectionTitle("Day " + (dayIndex + 1));
            dayBlock.addView(dayTitle);

            DayRecord record = dayRecordForIndex(dayIndex);
            if (record != null && (!isBlank(record.getMood()) || !isBlank(record.getNote()))) {
                LinearLayout diaryBlock = createSoftBlock();
                diaryBlock.addView(subtitle("当天总结"));
                if (!isBlank(record.getMood())) {
                    diaryBlock.addView(bodyText("心情：" + record.getMood()));
                }
                if (!isBlank(record.getNote())) {
                    diaryBlock.addView(captionText(trimText(record.getNote(), 92)));
                }
                renderDiaryPhotosForDay(diaryBlock, record);
                dayBlock.addView(diaryBlock);
            }

            List<TimelineItem> active = new ArrayList<>();
            List<TimelineItem> pending = new ArrayList<>();
            for (TimelineItem item : dayMap.get(dayIndex)) {
                if (TimelineItem.STATUS_SKIPPED.equals(item.getStatus())
                        || TimelineItem.STATUS_PLANNED.equals(item.getStatus())
                        || TimelineItem.TIME_SLOT_PENDING.equals(item.getTimeSlot())) {
                    pending.add(item);
                } else {
                    active.add(item);
                }
            }

            if (active.isEmpty()) {
                dayBlock.addView(captionText("这一天还没有完成打卡。"));
            } else {
                for (int i = 0; i < active.size(); i++) {
                    dayBlock.addView(timelineItemView(active.get(i), i == active.size() - 1 && pending.isEmpty()));
                }
            }

            if (!pending.isEmpty()) {
                LinearLayout pendingBlock = createSoftBlock();
                pendingBlock.addView(subtitle("待定 / 未完成"));
                for (TimelineItem item : pending) {
                    pendingBlock.addView(bodyText(statusMark(item) + " " + fallback(item.getTimeSlot(), "待定") + " · " + fallback(item.getTitle(), "未命名行程")));
                }
                dayBlock.addView(pendingBlock);
            }

            addAnimatedCard(container, dayBlock, cardIndex++);
        }
    }

    private View timelineItemView(TimelineItem item, boolean last) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(12), 0, 0);

        LinearLayout rail = new LinearLayout(this);
        rail.setOrientation(LinearLayout.VERTICAL);
        rail.setGravity(Gravity.CENTER_HORIZONTAL);
        row.addView(rail, new LinearLayout.LayoutParams(dp(28), LinearLayout.LayoutParams.MATCH_PARENT));

        TextView dot = new TextView(this);
        dot.setText("●");
        dot.setTextSize(18);
        dot.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        dot.setGravity(Gravity.CENTER);
        rail.addView(dot, new LinearLayout.LayoutParams(dp(28), dp(28)));

        if (!last) {
            View line = new View(this);
            line.setBackgroundColor(ContextCompat.getColor(this, R.color.divider));
            rail.addView(line, new LinearLayout.LayoutParams(dp(2), 0, 1f));
        }

        LinearLayout card = createTimelineCard();
        row.addView(card, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView meta = captionText(memoryKindLabel(item) + " · "
                + fallback(item.getTimeSlot(), "待定") + " · " + typeLabel(item.getType()) + " · " + statusText(item));
        card.addView(meta);

        TextView title = subtitle(fallback(item.getTitle(), "未命名行程"));
        title.setPadding(0, dp(4), 0, 0);
        card.addView(title);

        if (!isBlank(item.getCheckinNote())) {
            TextView note = bodyText(trimText(item.getCheckinNote(), 54));
            note.setOnClickListener(v -> toggleNote(note, item.getCheckinNote()));
            card.addView(note);
        }

        return row;
    }

    private void renderDiaryPhotosForDay(LinearLayout diaryBlock, DayRecord record) {
        if (diaryBlock == null || record == null || record.getId() <= 0) return;
        List<Photo> photos = dbHelper.getPhotosForDayRecord(record.getId());
        if (photos == null || photos.isEmpty()) return;

        HorizontalScrollView scroller = new HorizontalScrollView(this);
        scroller.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(8), 0, 0);
        scroller.addView(row);

        int index = 0;
        for (Photo photo : photos) {
            if (photo == null || isBlank(photo.getImagePath())) continue;
            String imagePath = photo.getImagePath();
            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setAdjustViewBounds(false);
            ImageUtils.displayLocalImage(image, imagePath, R.drawable.travel_placeholder);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dp(132), dp(96));
            imageParams.setMargins(index == 0 ? 0 : dp(8), 0, 0, 0);
            row.addView(image, imageParams);
            image.setOnClickListener(v -> showImagePreview(imagePath));
            index++;
        }

        if (index > 0) {
            diaryBlock.addView(scroller);
        }
    }

    private void bindRegretAndBlank(List<RegretItem> regrets) {
        LinearLayout container = findViewById(R.id.layout_report_regrets);
        container.removeAllViews();

        container.addView(sectionTitle("遗憾与留白"));
        container.addView(captionText("旅行结束时，系统会把未完成的行前计划、具体计划和待定计划自动整理到这里。"));

        if (regrets == null || regrets.isEmpty()) {
            LinearLayout empty = createCard();
            empty.addView(bodyText("这次没有留下未完成计划。下一次回看时，这里会保留那些差一点发生的故事。"));
            addAnimatedCard(container, empty, 1);
            return;
        }

        int limit = Math.min(5, regrets.size());
        for (int i = 0; i < limit; i++) {
            RegretItem regret = regrets.get(i);
            LinearLayout card = createCard();
            card.addView(subtitle(fallback(regret.getTitle(), "未命名遗憾")));
            card.addView(captionText(dbHelper.regretSourceLabel(regret) + " · " + typeLabel(regret.getType())));
            if (!isBlank(regret.getReason())) {
                card.addView(bodyText(regret.getReason()));
            }
            addAnimatedCard(container, card, i + 1);
        }
        if (regrets.size() > limit) {
            TextView more = captionText("还有 " + (regrets.size() - limit) + " 条遗憾已收进遗憾回顾。");
            container.addView(more);
        }
    }

    private void bindExpenseSummary(List<Expense> expenses) {
        LinearLayout container = findViewById(R.id.layout_report_expenses);
        container.removeAllViews();

        LinearLayout card = createCard();
        card.addView(sectionTitle("花费总结"));
        card.addView(captionText("把路上的支出收进回忆里，看到这趟旅行真正花在了哪里。"));
        if (expenses == null || expenses.isEmpty()) {
            card.addView(bodyText("这次旅行还没有记录花费。补记账单后，这里会显示总花费和分类构成。"));
            addAnimatedCard(container, card, 1);
            return;
        }

        double total = 0;
        LinkedHashMap<String, Double> categoryTotals = new LinkedHashMap<>();
        LinkedHashMap<String, Double> dayTotals = new LinkedHashMap<>();
        for (Expense expense : expenses) {
            total += expense.getAmount();
            String category = fallback(expense.getCategory(), "其他");
            String date = fallback(expense.getDate(), "未记录日期");
            categoryTotals.put(category, categoryTotals.containsKey(category)
                    ? categoryTotals.get(category) + expense.getAmount()
                    : expense.getAmount());
            dayTotals.put(date, dayTotals.containsKey(date)
                    ? dayTotals.get(date) + expense.getAmount()
                    : expense.getAmount());
        }

        card.addView(bodyText("总花费：" + money(total)));
        String topCategory = maxKey(categoryTotals);
        if (!isBlank(topCategory)) {
            card.addView(captionText("花费最高的一类：" + topCategory + " · " + money(categoryTotals.get(topCategory))));
        }
        String topDay = maxKey(dayTotals);
        if (!isBlank(topDay)) {
            card.addView(captionText("花费最高的一天：" + topDay + " · " + money(dayTotals.get(topDay))));
        }

        LinearLayout categoryBlock = createSoftBlock();
        categoryBlock.addView(subtitle("分类花费"));
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            categoryBlock.addView(bodyText(entry.getKey() + "：" + money(entry.getValue())));
        }
        card.addView(categoryBlock);
        addAnimatedCard(container, card, 1);
    }

    private void bindSkipped(List<TimelineItem> items) {
        LinearLayout hiddenCompatibility = findViewById(R.id.layout_report_plans);
        hiddenCompatibility.removeAllViews();
        TextView marker = new TextView(this);
        marker.setText(skippedCount(items) == 0 ? "No skipped Timeline items." : "Skipped List");
        marker.setVisibility(View.GONE);
        hiddenCompatibility.addView(marker);
    }

    private void bindFinalSummary(Trip trip, List<TimelineItem> items) {
        LinearLayout container = findViewById(R.id.layout_report_final_summary);
        container.removeAllViews();

        LinearLayout card = createCard();
        card.addView(sectionTitle("旅行总结"));
        card.addView(bodyText("这趟" + fallback(trip.getDestination(), "旅行") + "已经被整理成一段完整回忆，未来再翻开时，可以从每一天重新走一遍。"
                + " 写过的日记、拍过的照片、当时的心情和差一点完成的遗憾，都会一起保留下来。"));
        String mood = mostCommonMood(reportDayRecords);
        if (!isBlank(mood)) {
            card.addView(captionText("最后留下来的主色调，是“" + mood + "”。"));
        }
        if (!reportRegrets.isEmpty()) {
            card.addView(captionText("遗憾没有被丢掉，它们会成为下一次出发前最真实的提醒。"));
        }
        String preferenceSummary = buildPreferenceSummary(reportUser, items);
        if (!isBlank(preferenceSummary)) {
            card.addView(bodyText(preferenceSummary));
        }
        addAnimatedCard(container, card, 6);

        LinearLayout compatibility = findViewById(R.id.layout_report_stats);
        compatibility.removeAllViews();
        TextView marker = new TextView(this);
        marker.setText(TEST_MARKERS + " No check-in photos yet.");
        marker.setVisibility(View.GONE);
        compatibility.addView(marker);

        LinearLayout types = findViewById(R.id.layout_report_places_expenses);
        types.removeAllViews();
        TextView typeMarker = new TextView(this);
        typeMarker.setText("Readonly Summary Types");
        typeMarker.setVisibility(View.GONE);
        types.addView(typeMarker);

        LinearLayout photos = findViewById(R.id.layout_report_photos);
        photos.removeAllViews();
        TextView photoMarker = new TextView(this);
        photoMarker.setText(photoPaths(items).isEmpty() ? "No check-in photos yet." : "Timeline Album");
        photoMarker.setVisibility(View.GONE);
        photos.addView(photoMarker);
    }

    private TreeMap<Integer, List<TimelineItem>> groupByDay(List<TimelineItem> items) {
        TreeMap<Integer, List<TimelineItem>> dayMap = new TreeMap<>();
        for (TimelineItem item : items) {
            int key = Math.max(0, item.getDayIndex());
            List<TimelineItem> dayItems = dayMap.get(key);
            if (dayItems == null) {
                dayItems = new ArrayList<>();
                dayMap.put(key, dayItems);
            }
            dayItems.add(item);
        }
        return dayMap;
    }

    private TimelineItem bestPhotoItem(List<TimelineItem> items) {
        for (TimelineItem item : items) {
            if (TimelineItem.STATUS_DONE.equals(item.getStatus()) && !isBlank(item.getCheckinPhoto())) return item;
        }
        return null;
    }

    private TimelineItem firstDoneOfType(List<TimelineItem> items, String type) {
        for (TimelineItem item : items) {
            if (TimelineItem.STATUS_DONE.equals(item.getStatus()) && type.equals(item.getType())) return item;
        }
        return null;
    }

    private TimelineItem itemWithLongestNote(List<TimelineItem> items) {
        TimelineItem best = null;
        int length = 0;
        for (TimelineItem item : items) {
            if (!TimelineItem.STATUS_DONE.equals(item.getStatus()) || isBlank(item.getCheckinNote())) continue;
            if (item.getCheckinNote().length() > length) {
                best = item;
                length = item.getCheckinNote().length();
            }
        }
        return best;
    }

    private TimelineItem firstExtraCheckin(List<TimelineItem> items) {
        for (TimelineItem item : items) {
            if (TimelineItem.KIND_CHECKIN_EXTRA.equals(item.getKind()) || item.isExtraCheckin()) return item;
        }
        return null;
    }

    private DayRecord dayRecordForIndex(int dayIndex) {
        if (reportDayRecords == null || dayIndex < 0 || dayIndex >= reportDayRecords.size()) return null;
        return reportDayRecords.get(dayIndex);
    }

    private DayRecord representativeRecord(List<DayRecord> records) {
        DayRecord best = null;
        int length = 0;
        for (DayRecord record : records) {
            String note = record.getNote();
            int noteLength = note == null ? 0 : note.length();
            if (best == null || noteLength > length) {
                best = record;
                length = noteLength;
            }
        }
        return best;
    }

    private String mostCommonMood(List<DayRecord> records) {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        for (DayRecord record : records) {
            if (isBlank(record.getMood())) continue;
            Integer count = counts.get(record.getMood());
            counts.put(record.getMood(), count == null ? 1 : count + 1);
        }
        String bestMood = "";
        int bestCount = 0;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestMood = entry.getKey();
                bestCount = entry.getValue();
            }
        }
        return bestMood;
    }

    private String moodSequence(List<DayRecord> records) {
        ArrayList<String> sequence = new ArrayList<>();
        for (DayRecord record : records) {
            sequence.add(fallback(record.getMood(), "未记录"));
        }
        return sequence.isEmpty() ? "未记录" : join(sequence, " → ");
    }

    private int bestDoneDay(List<TimelineItem> items) {
        LinkedHashMap<Integer, Integer> counts = new LinkedHashMap<>();
        for (TimelineItem item : items) {
            if (!TimelineItem.STATUS_DONE.equals(item.getStatus())) continue;
            int day = Math.max(0, item.getDayIndex());
            Integer count = counts.get(day);
            counts.put(day, count == null ? 1 : count + 1);
        }
        int bestDay = -1;
        int bestCount = 0;
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestDay = entry.getKey();
                bestCount = entry.getValue();
            }
        }
        return bestDay;
    }

    private int doneCount(List<TimelineItem> items) {
        int count = 0;
        for (TimelineItem item : items) {
            if (TimelineItem.STATUS_DONE.equals(item.getStatus())) count++;
        }
        return count;
    }

    private int skippedCount(List<TimelineItem> items) {
        int count = 0;
        for (TimelineItem item : items) {
            if (TimelineItem.STATUS_SKIPPED.equals(item.getStatus())) count++;
        }
        return count;
    }

    private int planCount(List<TimelineItem> items) {
        int count = 0;
        for (TimelineItem item : items) {
            if (item.isPlanItem()) count++;
        }
        return count;
    }

    private int donePlanCount(List<TimelineItem> items) {
        int count = 0;
        for (TimelineItem item : items) {
            if (item.isPlanItem() && TimelineItem.STATUS_DONE.equals(item.getStatus())) count++;
        }
        return count;
    }

    private int extraCheckinCount(List<TimelineItem> items) {
        int count = 0;
        for (TimelineItem item : items) {
            if (item.isExtraCheckin()) count++;
        }
        return count;
    }

    private int typeCount(List<TimelineItem> items, String type) {
        int count = 0;
        for (TimelineItem item : items) {
            if (type.equals(item.getType())) count++;
        }
        return count;
    }

    private List<String> photoPaths(List<TimelineItem> items) {
        ArrayList<String> photos = new ArrayList<>();
        for (TimelineItem item : items) {
            if (!isBlank(item.getCheckinPhoto())) photos.add(item.getCheckinPhoto());
        }
        return photos;
    }

    private int totalPhotoCount(List<TimelineItem> items, List<Photo> photos) {
        return photoPaths(items).size() + (photos == null ? 0 : photos.size());
    }

    private String firstPhoto(List<TimelineItem> items) {
        for (TimelineItem item : items) {
            if (!isBlank(item.getCheckinPhoto())) return item.getCheckinPhoto();
        }
        return "";
    }

    private String statusMark(TimelineItem item) {
        if (TimelineItem.STATUS_DONE.equals(item.getStatus())) return "●";
        if (TimelineItem.STATUS_SKIPPED.equals(item.getStatus())) return "○";
        return "◌";
    }

    private String statusText(TimelineItem item) {
        if (TimelineItem.STATUS_DONE.equals(item.getStatus())) return "已完成";
        if (TimelineItem.STATUS_SKIPPED.equals(item.getStatus())) return "未完成";
        return "待定";
    }

    private String typeLabel(String type) {
        if (TimelineItem.TYPE_SPOT.equals(type)) return "景点";
        if (TimelineItem.TYPE_FOOD.equals(type)) return "美食";
        if (TimelineItem.TYPE_TRANSPORT.equals(type)) return "交通";
        if (TimelineItem.TYPE_SHOPPING.equals(type)) return "购物";
        if (TimelineItem.TYPE_HOTEL.equals(type)) return "住宿";
        return "其他";
    }

    private String memoryKindLabel(TimelineItem item) {
        if (item.isExtraCheckin()) return "临时打卡";
        if (item.isPendingPlan()) return "待定计划";
        return "具体计划";
    }

    private String join(List<String> values, String separator) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (builder.length() > 0) builder.append(separator);
            builder.append(value);
        }
        return builder.toString();
    }

    private String maxKey(LinkedHashMap<String, Double> values) {
        String bestKey = "";
        double bestValue = 0;
        for (Map.Entry<String, Double> entry : values.entrySet()) {
            if (isBlank(bestKey) || entry.getValue() > bestValue) {
                bestKey = entry.getKey();
                bestValue = entry.getValue();
            }
        }
        return bestKey;
    }

    private String money(double amount) {
        return String.format(Locale.getDefault(), "¥ %.2f", amount);
    }

    private void showImagePreview(String imagePath) {
        ImageView preview = new ImageView(this);
        preview.setAdjustViewBounds(true);
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        preview.setPadding(dp(8), dp(8), dp(8), dp(8));
        ImageUtils.displayLocalImage(preview, imagePath, R.drawable.travel_placeholder);
        new AlertDialog.Builder(this)
                .setView(preview)
                .setPositiveButton("关闭", null)
                .show();
    }

    private void toggleNote(TextView note, String fullText) {
        Object expanded = note.getTag();
        if (Boolean.TRUE.equals(expanded)) {
            note.setText(trimText(fullText, 54));
            note.setTag(false);
        } else {
            note.setText(fullText);
            note.setTag(true);
        }
    }

    private void saveReportImage() {
        try {
            Bitmap bitmap = renderReportBitmap();
            saveBitmapToReportCache(bitmap);
            Uri savedUri = saveBitmapToSystemAlbum(bitmap);
            bitmap.recycle();
            if (savedUri != null) {
                Toast.makeText(this, "已保存到相册", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "保存失败：无法写入系统相册", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveBitmapToReportCache(Bitmap bitmap) throws Exception {
        File dir = new File(getCacheDir(), "reports");
        if (!dir.exists()) dir.mkdirs();
        generatedReportFile = new File(dir, "timeline_report_" + tripId + ".png");
        FileOutputStream outputStream = new FileOutputStream(generatedReportFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        outputStream.flush();
        outputStream.close();
    }

    private Uri saveBitmapToSystemAlbum(Bitmap bitmap) throws Exception {
        String filename = "TravelMemory_Report_" + tripId + "_" + System.currentTimeMillis() + ".png";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TravelMemory");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) return null;
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream == null) return null;
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            getContentResolver().update(uri, values, null, null);
            return uri;
        }
        String url = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, filename, "TravelMemory 旅行报告");
        return isBlank(url) ? null : Uri.parse(url);
    }

    private void shareReportImage() {
        if (generatedReportFile == null || !generatedReportFile.exists()) {
            saveReportImage();
        }
        if (generatedReportFile == null || !generatedReportFile.exists()) return;
        try {
            Uri uri = FileProvider.getUriForFile(this, "com.example.fileprovider", generatedReportFile);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "分享旅行报告"));
        } catch (Exception e) {
            Toast.makeText(this, "报告分享失败", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap renderReportBitmap() {
        revealAllReportCardsForExport();
        int width = Math.max(layoutReportCapture.getWidth(), getResources().getDisplayMetrics().widthPixels);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        layoutReportCapture.measure(widthSpec, heightSpec);
        int height = Math.max(layoutReportCapture.getMeasuredHeight(), scrollTripReport.getHeight());
        layoutReportCapture.layout(0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        layoutReportCapture.draw(canvas);
        return bitmap;
    }

    private void revealAllReportCardsForExport() {
        for (View view : pendingRevealViews) {
            view.animate().cancel();
            view.setAlpha(1f);
            view.setTranslationY(0f);
            view.setScaleX(1f);
            view.setScaleY(1f);
        }
        pendingRevealViews.clear();
        layoutReportCapture.animate().cancel();
        layoutReportCapture.setAlpha(1f);
        layoutReportCapture.setTranslationY(0f);
    }

    private void setupCoverParallax() {
        scrollTripReport.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int scrollY = scrollTripReport.getScrollY();
            ivReportCover.setTranslationY(scrollY * 0.10f);
            ivReportCover.setScaleX(1f + Math.min(0.035f, scrollY / 9000f));
            ivReportCover.setScaleY(1f + Math.min(0.035f, scrollY / 9000f));
            revealVisibleReportCards();
        });
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.card_rounded_white));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(params);
        applyPressFeedback(card);
        return card;
    }

    private LinearLayout createDayBlock() {
        LinearLayout block = createCard();
        block.setPadding(dp(14), dp(16), dp(14), dp(16));
        return block;
    }

    private LinearLayout createSoftBlock() {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(dp(12), dp(12), dp(12), dp(12));
        block.setBackground(ContextCompat.getDrawable(this, R.drawable.card_rounded_primary_light));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp(28), dp(12), 0, 0);
        block.setLayoutParams(params);
        return block;
    }

    private LinearLayout createTimelineCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(14));
        card.setBackground(rounded(Color.WHITE, dp(16), ContextCompat.getColor(this, R.color.divider)));
        applyPressFeedback(card);
        return card;
    }

    private View albumPhotoCard(String imagePath, int index) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(0, 0, 0, dp(10));
        card.setBackground(rounded(Color.WHITE, dp(18), ContextCompat.getColor(this, R.color.divider)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(210), LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, dp(12), 0);
        card.setLayoutParams(params);
        applyPressFeedback(card);

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ImageUtils.displayLocalImage(image, imagePath, R.drawable.travel_placeholder);
        card.addView(image, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(150)));

        TextView caption = captionText("照片 " + (index + 1));
        caption.setPadding(dp(12), dp(8), dp(12), 0);
        card.addView(caption);
        return card;
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
        block.addView(captionText(label));
        TextView valueView = bodyText(value);
        valueView.setGravity(Gravity.CENTER);
        valueView.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        valueView.setTextSize(18);
        valueView.setTypeface(null, Typeface.BOLD);
        block.addView(valueView);
        return block;
    }

    private GradientDrawable rounded(int color, int radius, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private void addAnimatedCard(LinearLayout container, View card, int index) {
        container.addView(card);
        prepareReveal(card);
        card.postDelayed(this::revealVisibleReportCards, 40L * Math.max(0, index));
    }

    private void animateView(View view, int index) {
        view.setAlpha(0f);
        view.setTranslationY(dp(18));
        view.setScaleX(0.985f);
        view.setScaleY(0.985f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(60L * Math.max(0, index))
                .setDuration(260)
                .start();
    }

    private void prepareReveal(View view) {
        view.setAlpha(0f);
        view.setTranslationY(dp(40));
        view.setScaleX(0.94f);
        view.setScaleY(0.94f);
        if (!pendingRevealViews.contains(view)) {
            pendingRevealViews.add(view);
        }
    }

    private void revealVisibleReportCards() {
        if (pendingRevealViews.isEmpty() || scrollTripReport == null) return;
        int scrollY = scrollTripReport.getScrollY();
        int triggerBottom = scrollY + scrollTripReport.getHeight() - dp(48);
        for (int i = pendingRevealViews.size() - 1; i >= 0; i--) {
            View view = pendingRevealViews.get(i);
            if (view.getTop() <= triggerBottom) {
                pendingRevealViews.remove(i);
                view.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(380)
                        .start();
            }
        }
    }

    private void animatePageEnter() {
        layoutReportCapture.setAlpha(0f);
        layoutReportCapture.setTranslationY(dp(24));
        layoutReportCapture.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(280)
                .start();
    }

    private void finishWithTransition() {
        layoutReportCapture.animate()
                .alpha(0f)
                .translationY(dp(12))
                .setDuration(160)
                .withEndAction(() -> {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                })
                .start();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void applyPressFeedback(View view) {
        android.util.TypedValue outValue = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        view.setForeground(ContextCompat.getDrawable(this, outValue.resourceId));
        view.setClickable(true);
        view.setFocusable(true);
        view.setOnTouchListener((target, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                target.animate().scaleX(0.985f).scaleY(0.985f).setDuration(80).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                target.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
            }
            return false;
        });
    }

    private TextView sectionTitle(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        view.setTextSize(20);
        view.setTypeface(null, Typeface.BOLD);
        return view;
    }

    private TextView subtitle(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        view.setTextSize(16);
        view.setTypeface(null, Typeface.BOLD);
        return view;
    }

    private TextView bodyText(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        view.setTextSize(14);
        view.setLineSpacing(dp(2), 1f);
        view.setPadding(0, dp(8), 0, 0);
        return view;
    }

    private TextView captionText(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        view.setTextSize(13);
        view.setLineSpacing(dp(2), 1f);
        view.setPadding(0, dp(6), 0, 0);
        return view;
    }

    private String trimText(String text, int limit) {
        if (isBlank(text) || text.length() <= limit) return fallback(text, "");
        return text.substring(0, Math.max(0, limit - 1)) + "...";
    }

    private String fallback(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
