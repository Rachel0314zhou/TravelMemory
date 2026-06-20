package com.example.travelmemory.activity;

import android.app.AlertDialog;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.TimelineItem;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TravelConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PreTripPlanActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int tripId = -1;
    private int currentUserId = -1;
    private RecyclerView rvPrePlans;
    private RecyclerView rvPendingPlans;
    private TextView tvEmpty;
    private TextView tvSummary;
    private TextView tvPendingTitle;
    private TextView tvPageTitle;
    private LinearLayout layoutDayTabs;
    private final List<String> dayDates = new ArrayList<>();
    private int selectedDayIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_trip_plan);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);
        selectedDayIndex = Math.max(0, getIntent().getIntExtra(TravelConstants.EXTRA_DAY_INDEX, 0));

        if (currentUserId <= 0) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (tripId <= 0 || !dbHelper.isTripOwnedByUser(tripId, currentUserId)) {
            List<Trip> trips = dbHelper.getTripsForUser(currentUserId);
            if (trips.isEmpty()) {
                Toast.makeText(this, "还没有可用旅行", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            tripId = trips.get(0).getId();
        }

        rvPrePlans = findViewById(R.id.rv_pre_plans);
        rvPendingPlans = findViewById(R.id.rv_pre_plan_pending);
        tvEmpty = findViewById(R.id.tv_pre_plan_empty);
        tvSummary = findViewById(R.id.tv_pre_plan_summary);
        tvPendingTitle = findViewById(R.id.tv_pre_plan_pending_title);
        tvPageTitle = findViewById(R.id.tv_pre_plan_page_title);
        layoutDayTabs = findViewById(R.id.layout_pre_plan_day_tabs);
        rvPrePlans.setLayoutManager(new LinearLayoutManager(this));
        rvPendingPlans.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btn_pre_plan_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_pre_plan).setOnClickListener(v -> showTimelineDialog(null));
        renderDayTabs();
        loadTimelineItems();
    }

    private void loadTimelineItems() {
        List<TimelineItem> items = dbHelper.getTimelineItemsForTripDay(tripId, selectedDayIndex);
        renderPlansForSelectedDay(items);
    }

    private void renderPlansForSelectedDay(List<TimelineItem> items) {
        List<TimelineItem> dayItems = new ArrayList<>();
        List<TimelineItem> pendingItems = new ArrayList<>();
        for (TimelineItem item : items) {
            if (TimelineItem.TIME_SLOT_PENDING.equals(item.getTimeSlot())) {
                pendingItems.add(item);
            } else {
                dayItems.add(item);
            }
        }

        int total = dbHelper.countTimelineItemsForTrip(tripId);
        int done = dbHelper.countDoneTimelineItemsForTrip(tripId);
        tvSummary.setText("已完成 " + done + " / " + total + "，完成率 " + dbHelper.getTimelineCompletionRate(tripId) + "%");

        Trip trip = dbHelper.getTripForUser(tripId, currentUserId);
        if (trip != null) {
            tvPageTitle.setText(trip.getName());
        }

        boolean hasAnyForDay = !dayItems.isEmpty() || !pendingItems.isEmpty();
        tvEmpty.setVisibility(hasAnyForDay ? View.GONE : View.VISIBLE);
        rvPrePlans.setVisibility(dayItems.isEmpty() ? View.GONE : View.VISIBLE);
        tvPendingTitle.setVisibility(pendingItems.isEmpty() ? View.GONE : View.VISIBLE);
        rvPendingPlans.setVisibility(pendingItems.isEmpty() ? View.GONE : View.VISIBLE);
        rvPrePlans.setAdapter(new TimelineAdapter(dayItems));
        rvPendingPlans.setAdapter(new TimelineAdapter(pendingItems));
    }

    private void renderDayTabs() {
        dayDates.clear();
        layoutDayTabs.removeAllViews();
        Trip trip = dbHelper.getTripForUser(tripId, currentUserId);
        if (trip == null) return;
        dayDates.addAll(buildTripDates(trip));
        for (int i = 0; i < dayDates.size(); i++) {
            TextView dayTab = new TextView(this);
            dayTab.setText("Day " + (i + 1) + "\n" + shortDate(dayDates.get(i)));
            dayTab.setTextSize(14);
            dayTab.setGravity(Gravity.CENTER);
            dayTab.setPadding(dp(16), 0, dp(16), 0);
            dayTab.setTextColor(i == selectedDayIndex ? getColor(R.color.white) : getColor(R.color.primary));
            dayTab.setBackgroundResource(i == selectedDayIndex ? R.drawable.btn_primary : R.drawable.btn_secondary);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(40));
            params.setMargins(0, 0, dp(8), 0);
            dayTab.setLayoutParams(params);
            final int index = i;
            dayTab.setOnClickListener(v -> {
                selectedDayIndex = index;
                renderDayTabs();
                loadTimelineItems();
            });
            layoutDayTabs.addView(dayTab);
        }
    }

    private void showTimelineDialog(TimelineItem editingItem) {
        PlanForm form = createPlanForm(editingItem);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(editingItem == null ? "添加行前计划" : "编辑行前计划")
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
            TimelineItem item = editingItem == null ? new TimelineItem() : editingItem;
            item.setTripId(tripId);
            item.setDayIndex(selectedDayIndex);
            item.setTitle(title);
            item.setType(form.selectedType);
            item.setTimeSlot(formatPickedTime(form.timePicker));
            item.setStatus(TimelineItem.STATUS_PLANNED);
            item.setCreatedAt(todayText());
            if (editingItem == null) {
                dbHelper.addTimelineItem(item);
            } else {
                dbHelper.updateTimelineItem(item);
            }
            loadTimelineItems();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private PlanForm createPlanForm(TimelineItem editingItem) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(16);
        layout.setPadding(padding, padding, padding, dp(8));

        layout.addView(stepTitle("步骤 1  先写计划内容"));
        layout.addView(stepHint("用一句话写清楚要做什么，例如：上午去鼓浪屿。"));

        EditText etTitle = new EditText(this);
        etTitle.setHint("例如：晚上吃沙茶面");
        etTitle.setSingleLine(false);
        etTitle.setMinLines(1);
        etTitle.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        etTitle.setHintTextColor(ContextCompat.getColor(this, R.color.text_hint));
        layout.addView(etTitle);

        layout.addView(stepTitle("步骤 2  再选择计划类型"));
        layout.addView(stepHint("请选择计划类型，便于后续按分类查看。"));

        PlanForm form = new PlanForm(layout, etTitle);
        String initialType = TimelineItem.TYPE_SPOT;
        if (editingItem != null) {
            etTitle.setText(editingItem.getTitle());
            initialType = editingItem.getType();
        }
        layout.addView(createTypeGrid(form, initialType));

        layout.addView(stepTitle("步骤 3  最后选择计划时间"));
        layout.addView(stepHint("通过滑动时间选择器确定具体时间，避免手动输入格式错误。"));
        TimePicker timePicker = new TimePicker(this);
        timePicker.setIs24HourView(true);
        applyInitialTime(timePicker, editingItem == null ? null : editingItem.getTimeSlot());
        form.timePicker = timePicker;
        layout.addView(timePicker);
        return form;
    }

    private TextView stepTitle(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(15);
        view.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        view.setTypeface(null, android.graphics.Typeface.BOLD);
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
        scrollView.setVerticalScrollBarEnabled(true);
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

    private String statusText(TimelineItem item) {
        if (TimelineItem.STATUS_DONE.equals(item.getStatus())) return "已完成";
        if (TimelineItem.STATUS_SKIPPED.equals(item.getStatus())) return "已跳过";
        return "已计划";
    }

    private List<String> buildTripDates(Trip trip) {
        List<String> dates = new ArrayList<>();
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
            dates.add(trip.getStartDate());
        }
        return dates;
    }

    private String shortDate(String date) {
        if (date == null || date.length() < 10) return "日期";
        return date.substring(5);
    }

    private String todayText() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Calendar.getInstance().getTime());
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {
        private final List<TimelineItem> items;

        TimelineAdapter(List<TimelineItem> items) {
            this.items = items;
        }

        @Override
        public TimelineViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            LinearLayout root = new LinearLayout(PreTripPlanActivity.this);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(dp(16), dp(12), dp(16), dp(12));
            root.setBackground(ContextCompat.getDrawable(PreTripPlanActivity.this, R.drawable.card_rounded_white));
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT);
            params.setMargins(dp(16), dp(6), dp(16), dp(6));
            root.setLayoutParams(params);

            TextView title = new TextView(PreTripPlanActivity.this);
            title.setTextSize(16);
            title.setTextColor(ContextCompat.getColor(PreTripPlanActivity.this, R.color.text_primary));
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            root.addView(title);

            TextView meta = new TextView(PreTripPlanActivity.this);
            meta.setTextSize(13);
            meta.setTextColor(ContextCompat.getColor(PreTripPlanActivity.this, R.color.text_secondary));
            LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            metaParams.setMargins(0, dp(6), 0, 0);
            root.addView(meta, metaParams);

            return new TimelineViewHolder(root, title, meta);
        }

        @Override
        public void onBindViewHolder(TimelineViewHolder holder, int position) {
            TimelineItem item = items.get(position);
            holder.title.setText(item.getTitle());
            holder.meta.setText(item.getTimeSlot() + " / " + item.getType() + " / " + statusText(item));
            holder.itemView.setOnClickListener(v -> showTimelineDialog(item));
        }

        @Override
        public int getItemCount() {
            return items == null ? 0 : items.size();
        }

        class TimelineViewHolder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView meta;

            TimelineViewHolder(View itemView, TextView title, TextView meta) {
                super(itemView);
                this.title = title;
                this.meta = meta;
            }
        }
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
