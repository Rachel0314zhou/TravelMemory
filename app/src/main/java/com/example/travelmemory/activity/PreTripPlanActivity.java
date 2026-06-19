package com.example.travelmemory.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.PrePlanAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.PrePlan;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TripNavigator;
import com.example.travelmemory.utils.TripStateManager;
import com.example.travelmemory.utils.TravelConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PreTripPlanActivity extends AppCompatActivity implements PrePlanAdapter.Listener {

    private static final String[] PLAN_TYPES = {"想去的地方", "想吃的美食", "想买的东西", "想做的事情", "其他"};

    private DatabaseHelper dbHelper;
    private TripStateManager tripStateManager;
    private SessionManager sessionManager;
    private int tripId = -1;
    private int currentUserId = -1;
    private RecyclerView rvPrePlans;
    private RecyclerView rvPendingPlans;
    private TextView tvEmpty;
    private TextView tvSummary;
    private TextView tvPendingTitle;
    private TextView tvPageTitle;
    private Button btnSelectTrip;
    private LinearLayout layoutDayTabs;
    private final List<String> dayDates = new ArrayList<>();
    private int selectedDayIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_trip_plan);

        dbHelper = new DatabaseHelper(this);
        tripStateManager = new TripStateManager(dbHelper);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);

        if (currentUserId <= 0) {
            Toast.makeText(this, "请先选择自己的旅行", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (tripId <= 0 || !dbHelper.isTripOwnedByUser(tripId, currentUserId)) {
            List<Trip> trips = dbHelper.getTripsForUser(currentUserId);
            if (trips.isEmpty()) {
                Toast.makeText(this, "暂无可选择的旅行", Toast.LENGTH_SHORT).show();
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
        btnSelectTrip = findViewById(R.id.btn_pre_plan_select_trip);
        layoutDayTabs = findViewById(R.id.layout_pre_plan_day_tabs);
        rvPrePlans.setLayoutManager(new LinearLayoutManager(this));
        rvPendingPlans.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btn_pre_plan_back).setOnClickListener(v -> finish());
        btnSelectTrip.setOnClickListener(v -> showTripPicker());
        findViewById(R.id.btn_skip_pre_trip).setOnClickListener(v -> {
            tripStateManager.startTrip(tripId);
            TripNavigator.openTripDetail(this, tripId, TravelConstants.MODE_DURING_TRIP);
            finish();
        });
        renderDayTabs();
        loadPlans();
    }

    private void loadPlans() {
        List<PrePlan> plans = dbHelper.getPrePlansForTripSorted(tripId);
        renderPlansForSelectedDay(plans);
    }

    private void renderPlansForSelectedDay(List<PrePlan> plans) {
        List<PrePlan> dayPlans = new ArrayList<>();
        List<PrePlan> pendingPlans = new ArrayList<>();
        String selectedDate = selectedDate();
        for (PrePlan plan : plans) {
            if (!sameDay(plan.getTargetDate(), selectedDate)) continue;
            if (plan.isPending()) {
                pendingPlans.add(plan);
            } else {
                dayPlans.add(plan);
            }
        }
        int total = plans.size();
        int completed = dbHelper.countCompletedPrePlansForTrip(tripId);
        tvSummary.setText("已完成 " + completed + " / " + total + "，完成率 " + dbHelper.getPlanCompletionRate(tripId) + "%");
        Trip trip = dbHelper.getTripForUser(tripId, currentUserId);
        if (trip != null) {
            tvPageTitle.setText(trip.getName());
            btnSelectTrip.setText("选择旅行：" + trip.getName());
        }
        boolean hasAnyForDay = !dayPlans.isEmpty() || !pendingPlans.isEmpty();
        tvEmpty.setVisibility(hasAnyForDay ? View.GONE : View.VISIBLE);
        rvPrePlans.setVisibility(dayPlans.isEmpty() ? View.GONE : View.VISIBLE);
        tvPendingTitle.setVisibility(pendingPlans.isEmpty() ? View.GONE : View.VISIBLE);
        rvPendingPlans.setVisibility(pendingPlans.isEmpty() ? View.GONE : View.VISIBLE);
        if (!dayPlans.isEmpty()) {
            rvPrePlans.setAdapter(new PrePlanAdapter(this, dayPlans, this));
        }
        if (!pendingPlans.isEmpty()) {
            rvPendingPlans.setAdapter(new PrePlanAdapter(this, pendingPlans, this));
        }
    }

    private void showTripPicker() {
        List<Trip> trips = dbHelper.getTripsForUser(currentUserId);
        if (trips.isEmpty()) {
            Toast.makeText(this, "暂无可选择的旅行", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] names = new String[trips.size()];
        for (int i = 0; i < trips.size(); i++) {
            names[i] = trips.get(i).getName();
        }
        new AlertDialog.Builder(this)
                .setTitle("选择旅行")
                .setItems(names, (dialog, which) -> {
                    tripId = trips.get(which).getId();
                    selectedDayIndex = 0;
                    renderDayTabs();
                    loadPlans();
                })
                .show();
    }

    private void renderDayTabs() {
        dayDates.clear();
        layoutDayTabs.removeAllViews();
        Trip trip = dbHelper.getTripForUser(tripId, currentUserId);
        if (trip == null) return;
        dayDates.addAll(buildTripDates(trip));
        for (int i = 0; i < dayDates.size(); i++) {
            TextView dayTab = new TextView(this);
            dayTab.setText("Day " + (i + 1));
            dayTab.setTextSize(14);
            dayTab.setGravity(android.view.Gravity.CENTER);
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
                loadPlans();
            });
            layoutDayTabs.addView(dayTab);
        }
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

    private String selectedDate() {
        if (dayDates.isEmpty()) return "";
        int safeIndex = Math.max(0, Math.min(selectedDayIndex, dayDates.size() - 1));
        return dayDates.get(safeIndex);
    }

    private boolean sameDay(String planDate, String selectedDate) {
        if (selectedDate == null || selectedDate.isEmpty()) return true;
        return selectedDate.equals(planDate);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void showTypePicker() {
        new AlertDialog.Builder(this)
                .setTitle("选择计划类型")
                .setItems(PLAN_TYPES, (dialog, which) -> showPlanDialog(null, PLAN_TYPES[which]))
                .show();
    }

    private void showPlanDialog(PrePlan editingPlan, String type) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, 0);

        EditText etTitle = new EditText(this);
        etTitle.setHint(getTitleHint(type));
        layout.addView(etTitle);

        EditText etLocation = new EditText(this);
        etLocation.setHint(getLocationHint(type));
        layout.addView(etLocation);

        EditText etDate = new EditText(this);
        etDate.setHint("计划日期");
        etDate.setFocusable(false);
        etDate.setOnClickListener(v -> showDatePicker(etDate));
        layout.addView(etDate);

        TextView etTime = new TextView(this);
        etTime.setHint("计划时间，如 09:30");
        etTime.setTextSize(16);
        etTime.setPadding(0, padding / 2, 0, padding / 2);
        etTime.setOnClickListener(v -> showTimePicker(etTime));
        layout.addView(etTime);

        EditText etNote = new EditText(this);
        etNote.setHint("备注");
        layout.addView(etNote);

        CheckBox cbPending = new CheckBox(this);
        cbPending.setText("设为待定");
        layout.addView(cbPending);

        if (editingPlan != null) {
            etTitle.setText(editingPlan.getTitle());
            etLocation.setText(editingPlan.getLocation());
            etDate.setText(editingPlan.getTargetDate());
            etTime.setText(editingPlan.getTimeText());
            etNote.setText(editingPlan.getNote());
            cbPending.setChecked(editingPlan.isPending());
        }

        new AlertDialog.Builder(this)
                .setTitle(editingPlan == null ? "添加计划" : "编辑计划")
                .setView(layout)
                .setPositiveButton("保存", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(this, "请填写" + getRequiredName(type), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    PrePlan plan = editingPlan == null ? new PrePlan() : editingPlan;
                    plan.setTripId(tripId);
                    plan.setType(type);
                    plan.setTitle(title);
                    plan.setLocation(etLocation.getText().toString().trim());
                    plan.setTargetDate(etDate.getText().toString().trim());
                    plan.setTimeText(normalizeTimeText(etTime.getText().toString()));
                    plan.setNote(etNote.getText().toString().trim());
                    plan.setStatus(cbPending.isChecked() ? PrePlan.STATUS_PENDING : PrePlan.STATUS_NORMAL);
                    plan.setCreatedAt("2026-06-09");
                    if (editingPlan == null) {
                        plan.setIsCompleted(0);
                        dbHelper.addPrePlan(plan);
                    } else {
                        dbHelper.updatePrePlan(plan);
                    }
                    loadPlans();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.CHINA, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            target.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(TextView target) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            target.setText(String.format(Locale.CHINA, "%02d:%02d", hourOfDay, minute));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private String normalizeTimeText(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        return trimmed.matches("\\d{2}:\\d{2}") ? trimmed : "";
    }

    private String getTitleHint(String type) {
        switch (type) {
            case "想去的地方":
                return "地点名称";
            case "想吃的美食":
                return "美食名称";
            case "想买的东西":
                return "物品名称";
            case "想做的事情":
                return "事情名称";
            default:
                return "标题";
        }
    }

    private String getLocationHint(String type) {
        switch (type) {
            case "想去的地方":
                return "具体位置（选填）";
            case "想吃的美食":
                return "推荐店铺/地点（选填）";
            case "想买的东西":
                return "购买地点（选填）";
            case "想做的事情":
                return "发生地点（选填）";
            default:
                return "相关地点（选填）";
        }
    }

    private String getRequiredName(String type) {
        switch (type) {
            case "想去的地方":
                return "地点名称";
            case "想吃的美食":
                return "美食名称";
            case "想买的东西":
                return "物品名称";
            case "想做的事情":
                return "事情名称";
            default:
                return "标题";
        }
    }

    @Override
    public void onToggle(PrePlan plan, boolean completed) {
        if (completed) {
            dbHelper.completePrePlanAndSyncCheckIn(plan);
        } else {
            dbHelper.markPrePlanCompleted(plan.getId(), false);
        }
        loadPlans();
    }

    @Override
    public void onEdit(PrePlan plan) {
        showPlanDialog(plan, plan.getType());
    }

    @Override
    public void onDelete(PrePlan plan) {
        new AlertDialog.Builder(this)
                .setTitle("删除计划")
                .setMessage("确定删除“" + plan.getTitle() + "”吗？")
                .setPositiveButton("确认删除", (dialog, which) -> {
                    dbHelper.deletePrePlan(plan.getId());
                    loadPlans();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
