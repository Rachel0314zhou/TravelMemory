package com.example.travelmemory.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.DayRecord;
import com.example.travelmemory.model.TimelineItem;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.BottomNavHelper;
import com.example.travelmemory.utils.DrawerHelper;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TravelConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int currentUserId = -1;
    private int selectedTripId = -1;
    private int selectedDayIndex = 0;
    private LinearLayout layoutDayTabs;
    private LinearLayout layoutPlanTimeline;
    private TextView emptyState;
    private TextView tvPlanTripTitle;
    private TextView tvPlanTripDates;
    private TextView tvTodayDiarySummary;
    private TextView tvTodayMoodSummary;
    private Button btnAddPlan;
    private DrawerHelper drawerHelper;
    private final List<String> dayDates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        if (currentUserId <= 0) {
            goToLogin();
            return;
        }
        selectedTripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);

        drawerHelper = new DrawerHelper(this);
        drawerHelper.setup();
        tvPlanTripTitle = findViewById(R.id.tv_plan_trip_title);
        tvPlanTripDates = findViewById(R.id.tv_plan_trip_dates);
        tvTodayDiarySummary = findViewById(R.id.tv_today_diary_summary);
        tvTodayMoodSummary = findViewById(R.id.tv_today_mood_summary);
        layoutDayTabs = findViewById(R.id.layout_plan_day_tabs);
        layoutPlanTimeline = findViewById(R.id.layout_plan_timeline);
        emptyState = findViewById(R.id.search_empty_view);
        btnAddPlan = findViewById(R.id.btn_plan_add);

        findViewById(R.id.btn_search_back).setOnClickListener(v -> drawerHelper.open());
        Button selectButton = findViewById(R.id.btn_plan_select_trip);
        selectButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        selectButton.setOnClickListener(v -> showTripPicker());
        btnAddPlan.setOnClickListener(v -> showAddEntryMenu());
        View todayMemoryCard = (View) tvTodayDiarySummary.getParent();
        todayMemoryCard.setOnClickListener(v -> openDiaryForSelectedDay());
        todayMemoryCard.setClickable(true);

        BottomNavHelper.setup(this, BottomNavHelper.TAB_SEARCH);
        loadCurrentTripPlan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (drawerHelper != null) {
            drawerHelper.refreshUser();
        }
        if (dbHelper != null && currentUserId > 0) {
            loadCurrentTripPlan();
        }
    }

    private void loadCurrentTripPlan() {
        List<Trip> trips = dbHelper.getTripsForUser(currentUserId);
        if (trips.isEmpty()) {
            selectedTripId = -1;
            tvPlanTripTitle.setText("暂无旅行");
            tvPlanTripDates.setText("创建旅行后，可在这里按 Day 安排每日计划。");
            btnAddPlan.setVisibility(View.GONE);
            layoutDayTabs.removeAllViews();
            showEmpty("还没有可展示的旅行计划");
            return;
        }

        Trip trip = findSelectedTrip(trips);
        if (trip == null) {
            trip = trips.get(0);
            selectedTripId = trip.getId();
            selectedDayIndex = defaultDayIndex(trip);
        }
        renderSelectedTripPlan(trip);
    }

    private Trip findSelectedTrip(List<Trip> trips) {
        for (Trip trip : trips) {
            if (trip.getId() == selectedTripId) return trip;
        }
        return null;
    }

    private void renderSelectedTripPlan(Trip trip) {
        selectedTripId = trip.getId();
        tvPlanTripTitle.setText(blankFallback(trip.getName(), "未命名旅行"));
        tvPlanTripDates.setText(blankFallback(trip.getDestination(), "目的地未填写") + " · "
                + blankFallback(trip.getStartDate(), "开始日期") + " ~ " + blankFallback(trip.getEndDate(), "结束日期"));
        btnAddPlan.setVisibility(View.VISIBLE);
        renderDayTabs(trip);
        renderTodayMemorySummary();
        renderPlansForSelectedDay();
    }

    private void showTripPicker() {
        List<Trip> trips = dbHelper.getTripsForUser(currentUserId);
        if (trips.isEmpty()) {
            showEmpty("还没有旅行，先创建一趟旅行再规划行程。");
            return;
        }
        String[] names = new String[trips.size()];
        for (int i = 0; i < trips.size(); i++) {
            Trip trip = trips.get(i);
            names[i] = blankFallback(trip.getName(), "未命名旅行") + " · "
                    + blankFallback(trip.getDestination(), "目的地") + " · "
                    + blankFallback(trip.getStartDate(), "日期未定");
        }
        new AlertDialog.Builder(this)
                .setTitle("切换旅行")
                .setItems(names, (dialog, which) -> {
                    Trip trip = trips.get(which);
                    selectedTripId = trip.getId();
                    selectedDayIndex = defaultDayIndex(trip);
                    renderSelectedTripPlan(trip);
                })
                .show();
    }

    private void renderDayTabs(Trip trip) {
        dayDates.clear();
        dayDates.addAll(buildTripDates(trip));
        if (dayDates.isEmpty()) {
            dayDates.add(blankFallback(trip.getStartDate(), ""));
        }
        selectedDayIndex = Math.max(0, Math.min(selectedDayIndex, dayDates.size() - 1));
        layoutDayTabs.removeAllViews();

        for (int i = 0; i < dayDates.size(); i++) {
            TextView dayTab = new TextView(this);
            dayTab.setText("Day " + (i + 1) + "\n" + shortDate(dayDates.get(i)));
            dayTab.setTextSize(13);
            dayTab.setGravity(Gravity.CENTER);
            dayTab.setTypeface(null, Typeface.BOLD);
            dayTab.setPadding(dp(14), 0, dp(14), 0);
            dayTab.setTextColor(i == selectedDayIndex
                    ? ContextCompat.getColor(this, R.color.white)
                    : ContextCompat.getColor(this, R.color.primary));
            dayTab.setBackground(ContextCompat.getDrawable(this,
                    i == selectedDayIndex ? R.drawable.btn_primary : R.drawable.btn_secondary));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(86), dp(54));
            params.setMargins(0, 0, dp(8), 0);
            dayTab.setLayoutParams(params);
            final int index = i;
            dayTab.setOnClickListener(v -> {
                selectedDayIndex = index;
                renderDayTabs(dbHelper.getTripForUser(selectedTripId, currentUserId));
                renderTodayMemorySummary();
                renderPlansForSelectedDay();
            });
            layoutDayTabs.addView(dayTab);
        }
    }

    private void renderPlansForSelectedDay() {
        layoutPlanTimeline.removeAllViews();
        if (selectedTripId <= 0) {
            showEmpty("还没有可展示的旅行计划");
            return;
        }

        List<TimelineItem> items = dbHelper.getTimelineItemsForTripDay(selectedTripId, selectedDayIndex);
        if (items.isEmpty()) {
            showEmpty("这一天还没有计划\n点击右下角加号添加计划。");
            return;
        }

        emptyState.setVisibility(View.GONE);
        layoutPlanTimeline.setVisibility(View.VISIBLE);
        addSectionHeader("主时间轴");
        addTimedSections(items);
        addSection(TimelineItem.TIME_SLOT_MORNING, items);
        addSection(TimelineItem.TIME_SLOT_AFTERNOON, items);
        addSection(TimelineItem.TIME_SLOT_EVENING, items);
        renderPendingPlans(items);
    }

    private void addTimedSections(List<TimelineItem> items) {
        String currentTime = "";
        for (TimelineItem item : items) {
            String time = item.getTimeSlot();
            if (item.isPendingPlan()) continue;
            if (time == null || !time.matches("\\d{2}:\\d{2}")) continue;
            if (!time.equals(currentTime)) {
                currentTime = time;
                addSectionHeader(time);
            }
            layoutPlanTimeline.addView(createTimelineCard(item));
        }
    }

    private void renderTodayMemorySummary() {
        if (selectedTripId <= 0) return;
        DayRecord record = dbHelper.getDayRecordByTripAndDate(selectedTripId, selectedDate());
        if (record == null) {
            tvTodayMoodSummary.setText("今日心情：还没有记录");
            tvTodayDiarySummary.setText("今日日记：点击加号编写当天总结");
            return;
        }
        tvTodayMoodSummary.setText("今日心情：" + blankFallback(record.getMood(), "未选择"));
        tvTodayDiarySummary.setText("今日日记：" + trimText(blankFallback(record.getNote(), "还没有文字记录"), 42));
    }

    private void addSection(String section, List<TimelineItem> items) {
        boolean hasAny = false;
        for (TimelineItem item : items) {
            if (item.isPendingPlan()) continue;
            if (section.equals(item.getTimeSlot())) {
                hasAny = true;
                break;
            }
        }
        if (!hasAny) return;

        addSectionHeader(section);

        for (TimelineItem item : items) {
            if (item.isPendingPlan()) continue;
            if (section.equals(item.getTimeSlot())) {
                layoutPlanTimeline.addView(createTimelineCard(item));
            }
        }
    }

    private void renderPendingPlans(List<TimelineItem> items) {
        boolean hasAny = false;
        for (TimelineItem item : items) {
            if (item.isPendingPlan()) {
                hasAny = true;
                break;
            }
        }
        if (!hasAny) return;
        addSectionHeader("待定计划");
        for (TimelineItem item : items) {
            if (item.isPendingPlan()) {
                layoutPlanTimeline.addView(createTimelineCard(item));
            }
        }
    }

    private void addSectionHeader(String section) {
        TextView header = new TextView(this);
        header.setText(section);
        header.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        header.setTextSize(15);
        header.setTypeface(null, Typeface.BOLD);
        header.setPadding(dp(4), dp(18), dp(4), dp(8));
        layoutPlanTimeline.addView(header);
    }

    private LinearLayout createTimelineCard(TimelineItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.TOP);
        row.setPadding(0, dp(4), 0, dp(8));

        TextView status = new TextView(this);
        status.setText(statusMark(item));
        status.setTextSize(24);
        status.setGravity(Gravity.CENTER);
        status.setTextColor(statusColor(item));
        status.setOnClickListener(v -> {
            if (item.isPlanItem() && TimelineItem.STATUS_PLANNED.equals(item.getStatus())) {
                dbHelper.markTimelineItemDone(item.getId());
                renderPlansForSelectedDay();
            }
        });
        row.addView(status, new LinearLayout.LayoutParams(dp(44), dp(44)));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.card_rounded_white));
        card.setOnClickListener(v -> onTimelineItemClicked(item));

        TextView title = new TextView(this);
        title.setText(item.getTitle());
        title.setTextColor(statusColor(item));
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);
        card.addView(title);

        TextView meta = createMeta(itemMeta(item));
        card.addView(meta);

        if (!safe(item.getCheckinNote()).isEmpty()) {
            card.addView(createMeta(item.getCheckinNote()));
        }

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(card, cardParams);
        row.setAlpha(0f);
        row.setTranslationY(dp(10));
        row.animate().alpha(1f).translationY(0f).setDuration(220).start();
        return row;
    }

    private String statusMark(TimelineItem item) {
        return TimelineItem.STATUS_DONE.equals(item.getStatus()) ? "\u25cf" : "\u25cb";
    }

    private int statusColor(TimelineItem item) {
        if (TimelineItem.STATUS_SKIPPED.equals(item.getStatus())) {
            return ContextCompat.getColor(this, R.color.text_secondary);
        }
        if (TimelineItem.STATUS_DONE.equals(item.getStatus())) {
            return ContextCompat.getColor(this, R.color.primary);
        }
        return ContextCompat.getColor(this, R.color.text_primary);
    }

    private String itemMeta(TimelineItem item) {
        return kindText(item) + " / " + item.getTimeSlot() + " / " + blankFallback(item.getType(), TimelineItem.TYPE_OTHER) + " / " + statusText(item);
    }

    private String kindText(TimelineItem item) {
        if (item.isExtraCheckin()) return "临时打卡";
        if (item.isPendingPlan()) return "待定计划";
        return "具体计划";
    }

    private String statusText(TimelineItem item) {
        if (TimelineItem.STATUS_DONE.equals(item.getStatus())) return "已完成";
        if (TimelineItem.STATUS_SKIPPED.equals(item.getStatus())) return "已跳过";
        return "已计划";
    }

    private TextView createMeta(String text) {
        TextView meta = new TextView(this);
        meta.setText(text);
        meta.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        meta.setTextSize(13);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(8), 0, 0);
        meta.setLayoutParams(params);
        return meta;
    }

    private void openAddPlanPage() {
        if (selectedTripId <= 0) {
            Toast.makeText(this, "请先选择旅行", Toast.LENGTH_SHORT).show();
            return;
        }
        showPlanKindPicker();
    }

    private void showAddEntryMenu() {
        if (selectedTripId <= 0) {
            Toast.makeText(this, "请先选择旅行", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("新增当日记录")
                .setItems(new String[]{"添加计划", "编写旅行日记", "记录临时打卡"}, (dialog, which) -> {
                    if (which == 0) {
                        showPlanKindPicker();
                    } else if (which == 1) {
                        openDiaryForSelectedDay();
                    } else {
                        showExtraCheckinDialog();
                    }
                })
                .show();
    }

    private void showPlanKindPicker() {
        new AlertDialog.Builder(this)
                .setTitle("选择计划方式")
                .setItems(new String[]{"具体计划", "待定计划"}, (dialog, which) -> {
                    if (which == 0) {
                        showTimelineItemDialog(TimelineItem.KIND_PLAN_FIXED);
                    } else {
                        showTimelineItemDialog(TimelineItem.KIND_PLAN_PENDING);
                    }
                })
                .show();
    }

    private void openDiaryForSelectedDay() {
        Intent intent = new Intent(this, DayRecordActivity.class);
        intent.putExtra(TravelConstants.EXTRA_TRIP_ID, selectedTripId);
        intent.putExtra(TravelConstants.EXTRA_RECORD_DATE, selectedDate());
        startActivity(intent);
    }

    private void onTimelineItemClicked(TimelineItem item) {
        if (item.getId() < 0 && TimelineItem.STATUS_PLANNED.equals(item.getStatus())) {
            new AlertDialog.Builder(this)
                    .setTitle(item.getTitle())
                    .setItems(new String[]{"标记完成", "标记跳过"}, (dialog, which) -> {
                        if (which == 0) {
                            dbHelper.markTimelineItemDone(item.getId());
                            renderPlansForSelectedDay();
                        } else {
                            dbHelper.markTimelineItemSkipped(item.getId());
                            renderPlansForSelectedDay();
                        }
                    })
                    .show();
            return;
        }
        showTimelineDetailWithActions(item);
    }

    private void showTimelineItemDialog(String planKind) {
        PlanForm form = createPlanForm(planKind, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(TimelineItem.KIND_PLAN_PENDING.equals(planKind) ? "添加待定计划" : "添加具体计划")
                .setView(scrollableDialogView(form.root))
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = form.etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "请先填写计划标题或内容", Toast.LENGTH_SHORT).show();
                return;
            }
            TimelineItem item = new TimelineItem();
            item.setTripId(selectedTripId);
            item.setDayIndex(selectedDayIndex);
            item.setTitle(title);
            item.setType(form.selectedType);
            item.setKind(planKind);
            item.setTimeSlot(TimelineItem.KIND_PLAN_PENDING.equals(planKind)
                    ? TimelineItem.TIME_SLOT_PENDING
                    : formatPickedTime(form.timePicker));
            item.setStatus(TimelineItem.STATUS_PLANNED);
            item.setCreatedAt(todayText());
            dbHelper.addTimelineItem(item);
            renderPlansForSelectedDay();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void showEditTimelineItemDialog(TimelineItem existing) {
        String planKind = existing.getKind();
        PlanForm form = createPlanForm(planKind, existing);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("编辑计划")
                .setView(scrollableDialogView(form.root))
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = form.etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "请先填写计划标题或内容", Toast.LENGTH_SHORT).show();
                return;
            }
            existing.setTitle(title);
            existing.setType(form.selectedType);
            existing.setKind(planKind);
            existing.setTimeSlot(TimelineItem.KIND_PLAN_PENDING.equals(planKind)
                    ? TimelineItem.TIME_SLOT_PENDING
                    : formatPickedTime(form.timePicker));
            dbHelper.updateTimelineItem(existing);
            renderPlansForSelectedDay();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private PlanForm createPlanForm(String planKind, TimelineItem existing) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(16);
        layout.setPadding(padding, padding, padding, dp(8));

        layout.addView(stepTitle("步骤 1  先写计划内容"));
        layout.addView(stepHint("用一句话写清楚要做什么，例如：下午去博物馆。"));

        EditText etTitle = new EditText(this);
        etTitle.setHint("例如：上午参观厦门大学");
        etTitle.setSingleLine(false);
        etTitle.setMinLines(1);
        etTitle.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        etTitle.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint));
        if (existing != null) {
            etTitle.setText(existing.getTitle());
        }
        layout.addView(etTitle);

        layout.addView(stepTitle("步骤 2  再选择计划类型"));
        layout.addView(stepHint("请选择计划类型，便于后续按分类查看。"));
        PlanForm form = new PlanForm(layout, etTitle);
        layout.addView(createTypeGrid(form, existing == null ? TimelineItem.TYPE_SPOT : existing.getType()));

        if (TimelineItem.KIND_PLAN_PENDING.equals(planKind)) {
            layout.addView(stepTitle("步骤 3  放入待定区"));
            layout.addView(stepHint("待定计划会固定显示在当天页面底部，不参与时间排序。"));
        } else {
            layout.addView(stepTitle("步骤 3  最后选择计划时间"));
            layout.addView(stepHint("通过滑动时间选择器确定具体时间，避免手动输入格式错误。"));
            TimePicker timePicker = new TimePicker(this);
            timePicker.setIs24HourView(true);
            applyInitialTime(timePicker, existing == null ? null : existing.getTimeSlot());
            form.timePicker = timePicker;
            layout.addView(timePicker);
        }
        return form;
    }

    private void showExtraCheckinDialog() {
        PlanForm form = createPlanForm(TimelineItem.KIND_PLAN_FIXED, null);
        EditText etNote = new EditText(this);
        etNote.setHint("备注，例如：路边听到一段很好听的演奏");
        form.root.addView(stepTitle("补充临时打卡备注"));
        form.root.addView(etNote);

        EditText etPhoto = new EditText(this);
        etPhoto.setHint("照片路径，可选");
        form.root.addView(etPhoto);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("记录临时打卡")
                .setView(scrollableDialogView(form.root))
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = form.etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "请先填写临时打卡标题", Toast.LENGTH_SHORT).show();
                return;
            }
            TimelineItem item = new TimelineItem();
            item.setTripId(selectedTripId);
            item.setDayIndex(selectedDayIndex);
            item.setKind(TimelineItem.KIND_CHECKIN_EXTRA);
            item.setTitle(title);
            item.setType(form.selectedType);
            item.setTimeSlot(formatPickedTime(form.timePicker));
            item.setStatus(TimelineItem.STATUS_DONE);
            item.setCheckinNote(etNote.getText().toString().trim());
            item.setCheckinPhoto(etPhoto.getText().toString().trim());
            item.setCheckinTime(todayText());
            item.setCreatedAt(todayText());
            dbHelper.addTimelineItem(item);
            renderPlansForSelectedDay();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void showCheckinDialog(TimelineItem item) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(16);
        layout.setPadding(padding, padding, padding, 0);

        EditText etPhoto = new EditText(this);
        etPhoto.setHint("Photo path (optional)");
        layout.addView(etPhoto);

        EditText etNote = new EditText(this);
        etNote.setHint("Note");
        layout.addView(etNote);

        EditText etMood = new EditText(this);
        etMood.setHint("Mood");
        layout.addView(etMood);

        new AlertDialog.Builder(this)
                .setTitle("Check in")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    dbHelper.markTimelineItemDoneWithCheckin(
                            item.getId(),
                            etPhoto.getText().toString().trim(),
                            etNote.getText().toString().trim(),
                            etMood.getText().toString().trim(),
                            todayText());
                    renderPlansForSelectedDay();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showTimelineDetailWithActions(TimelineItem item) {
        String message = "类型：" + item.getType()
                + "\n时间：" + item.getTimeSlot()
                + "\n状态：" + statusText(item)
                + "\n照片：" + blankFallback(item.getCheckinPhoto(), "无")
                + "\n备注：" + blankFallback(item.getCheckinNote(), "无")
                + "\n心情：" + blankFallback(item.getCheckinMood(), "无")
                + "\n打卡时间：" + blankFallback(item.getCheckinTime(), "无");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(item.getTitle())
                .setMessage(message)
                .setPositiveButton("编辑", null)
                .setNegativeButton("删除", null)
                .setNeutralButton("关闭", null)
                .create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                dialog.dismiss();
                showEditTimelineItemDialog(item);
            });
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
                dbHelper.deleteTimelineItem(item.getId());
                renderPlansForSelectedDay();
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void showTimelineDetail(TimelineItem item) {
        String message = "类型：" + item.getType()
                + "\n时间：" + item.getTimeSlot()
                + "\n状态：" + statusText(item)
                + "\n照片：" + blankFallback(item.getCheckinPhoto(), "无")
                + "\n备注：" + blankFallback(item.getCheckinNote(), "无")
                + "\n心情：" + blankFallback(item.getCheckinMood(), "无")
                + "\n打卡时间：" + blankFallback(item.getCheckinTime(), "无");
        new AlertDialog.Builder(this)
                .setTitle(item.getTitle())
                .setMessage(message)
                .setPositiveButton("知道了", null)
                .show();
    }

    private TextView stepTitle(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(15);
        view.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        view.setTypeface(null, Typeface.BOLD);
        view.setPadding(0, dp(18), 0, dp(4));
        return view;
    }

    private TextView stepHint(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(13);
        view.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        view.setPadding(0, 0, 0, dp(8));
        return view;
    }

    private ScrollView scrollableDialogView(View content) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(false);
        scrollView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        scrollView.addView(content, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));
        return scrollView;
    }

    private GridLayout createTypeGrid(PlanForm form, String initialType) {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        String[] types = new String[]{
                TimelineItem.TYPE_SPOT,
                TimelineItem.TYPE_FOOD,
                TimelineItem.TYPE_TRANSPORT,
                TimelineItem.TYPE_SHOPPING,
                TimelineItem.TYPE_HOTEL,
                TimelineItem.TYPE_OTHER
        };
        for (String type : types) {
            Button button = new Button(this);
            button.setText(type);
            button.setAllCaps(false);
            button.setTextSize(14);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = dp(44);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(0, dp(4), dp(8), dp(4));
            button.setLayoutParams(params);
            button.setOnClickListener(v -> {
                form.selectedType = type;
                refreshTypeButtons(form);
            });
            form.typeButtons.add(button);
            grid.addView(button);
        }
        form.selectedType = initialType;
        refreshTypeButtons(form);
        return grid;
    }

    private void refreshTypeButtons(PlanForm form) {
        for (Button button : form.typeButtons) {
            boolean selected = button.getText().toString().equals(form.selectedType);
            button.setBackground(ContextCompat.getDrawable(this, selected ? R.drawable.btn_primary : R.drawable.btn_secondary));
            button.setTextColor(ContextCompat.getColor(this, selected ? R.color.white : R.color.primary));
        }
    }

    private void applyInitialTime(TimePicker timePicker, String time) {
        int hour = 9;
        int minute = 0;
        if (time != null && time.matches("\\d{2}:\\d{2}")) {
            hour = Integer.parseInt(time.substring(0, 2));
            minute = Integer.parseInt(time.substring(3, 5));
        }
        timePicker.setHour(hour);
        timePicker.setMinute(minute);
    }

    private String formatPickedTime(TimePicker timePicker) {
        return String.format(Locale.CHINA, "%02d:%02d", timePicker.getHour(), timePicker.getMinute());
    }

    private List<String> buildTripDates(Trip trip) {
        List<String> dates = new ArrayList<>();
        if (trip == null) return dates;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        try {
            Date start = format.parse(trip.getStartDate());
            Date end = format.parse(trip.getEndDate());
            if (start == null || end == null || end.before(start)) {
                dates.add(trip.getStartDate());
                return dates;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            while (!calendar.getTime().after(end)) {
                dates.add(format.format(calendar.getTime()));
                calendar.add(Calendar.DATE, 1);
            }
        } catch (ParseException e) {
            dates.add(blankFallback(trip.getStartDate(), ""));
        }
        return dates;
    }

    private int defaultDayIndex(Trip trip) {
        if (trip == null) return 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        String today = format.format(Calendar.getInstance().getTime());
        List<String> dates = buildTripDates(trip);
        for (int i = 0; i < dates.size(); i++) {
            if (today.equals(dates.get(i))) return i;
        }
        return 0;
    }

    private String shortDate(String date) {
        if (date == null || date.length() < 10) return "日期";
        return date.substring(5);
    }

    private String selectedDate() {
        if (dayDates.isEmpty()) return "";
        int safeIndex = Math.max(0, Math.min(selectedDayIndex, dayDates.size() - 1));
        return dayDates.get(safeIndex);
    }

    private void showEmpty(String message) {
        layoutPlanTimeline.setVisibility(View.GONE);
        emptyState.setText(message);
        emptyState.setVisibility(View.VISIBLE);
    }

    private String blankFallback(String value, String fallback) {
        return safe(value).isEmpty() ? fallback : value;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimText(String value, int maxLength) {
        String text = blankFallback(value, "");
        if (text.length() <= maxLength) return text;
        return text.substring(0, Math.max(0, maxLength - 1)) + "…";
    }

    private String todayText() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Calendar.getInstance().getTime());
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static class PlanForm {
        final LinearLayout root;
        final EditText etTitle;
        final List<Button> typeButtons = new ArrayList<>();
        String selectedType = TimelineItem.TYPE_SPOT;
        TimePicker timePicker;

        PlanForm(LinearLayout root, EditText etTitle) {
            this.root = root;
            this.etTitle = etTitle;
        }
    }
}
