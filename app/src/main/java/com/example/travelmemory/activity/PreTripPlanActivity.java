package com.example.travelmemory.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TripNavigator;
import com.example.travelmemory.utils.TripStateManager;
import com.example.travelmemory.utils.TravelConstants;

import java.util.Calendar;
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
    private TextView tvEmpty;
    private TextView tvSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_trip_plan);

        dbHelper = new DatabaseHelper(this);
        tripStateManager = new TripStateManager(dbHelper);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);

        if (currentUserId <= 0 || tripId <= 0 || !dbHelper.isTripOwnedByUser(tripId, currentUserId)) {
            Toast.makeText(this, "请先选择自己的旅行", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvPrePlans = findViewById(R.id.rv_pre_plans);
        tvEmpty = findViewById(R.id.tv_pre_plan_empty);
        tvSummary = findViewById(R.id.tv_pre_plan_summary);
        rvPrePlans.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btn_pre_plan_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_pre_plan).setOnClickListener(v -> showTypePicker());
        findViewById(R.id.btn_skip_pre_trip).setOnClickListener(v -> {
            tripStateManager.startTrip(tripId);
            TripNavigator.openTripDetail(this, tripId, TravelConstants.MODE_DURING_TRIP);
            finish();
        });
        loadPlans();
    }

    private void loadPlans() {
        List<PrePlan> plans = dbHelper.getPrePlansForTrip(tripId);
        int total = plans.size();
        int completed = dbHelper.countCompletedPrePlansForTrip(tripId);
        tvSummary.setText("已完成 " + completed + " / " + total + "，完成率 " + dbHelper.getPlanCompletionRate(tripId) + "%");
        if (plans.isEmpty()) {
            rvPrePlans.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvPrePlans.setVisibility(View.VISIBLE);
            rvPrePlans.setAdapter(new PrePlanAdapter(this, plans, this));
        }
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

        EditText etNote = new EditText(this);
        etNote.setHint("备注");
        layout.addView(etNote);

        if (editingPlan != null) {
            etTitle.setText(editingPlan.getTitle());
            etLocation.setText(editingPlan.getLocation());
            etDate.setText(editingPlan.getTargetDate());
            etNote.setText(editingPlan.getNote());
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
                    plan.setNote(etNote.getText().toString().trim());
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
    public void onToggle(PrePlan plan) {
        dbHelper.markPrePlanCompleted(plan.getId(), plan.getIsCompleted() != 1);
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
