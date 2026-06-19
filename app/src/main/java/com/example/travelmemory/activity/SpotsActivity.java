package com.example.travelmemory.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.SpotAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.PrePlan;
import com.example.travelmemory.model.Spot;
import com.example.travelmemory.utils.TravelConstants;

import java.util.List;

public class SpotsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int tripId = -1;
    private EditText etSpotName, etSpotNote;
    private Spinner spSpotType;
    private CheckBox cbSpotFavorite;
    private RecyclerView rvSpots;
    private LinearLayout layoutPlannedCheckins;
    private TextView emptyState;
    private final String[] spotTypes = {"景点", "美食", "住宿", "其他"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spots);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);
        if (tripId == -1) {
            Toast.makeText(this, "未找到目标旅行，打卡失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etSpotName = findViewById(R.id.et_spot_name);
        etSpotNote = findViewById(R.id.et_spot_note);
        spSpotType = findViewById(R.id.sp_spot_type);
        cbSpotFavorite = findViewById(R.id.cb_spot_favorite);
        Button btnAddSpot = findViewById(R.id.btn_add_spot);
        rvSpots = findViewById(R.id.rv_spots);
        layoutPlannedCheckins = findViewById(R.id.layout_planned_checkins);
        emptyState = findViewById(R.id.spots_empty_view);
        findViewById(R.id.btn_spot_back).setOnClickListener(v -> finish());

        rvSpots.setLayoutManager(new LinearLayoutManager(this));
        spSpotType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spotTypes));
        btnAddSpot.setOnClickListener(v -> addSpotToTrip());
        loadPlannedCheckins();
        loadSpotsList();
    }

    private void loadPlannedCheckins() {
        layoutPlannedCheckins.removeAllViews();
        List<PrePlan> plans = dbHelper.getPrePlansForTripSorted(tripId);
        boolean hasPlanned = false;
        for (PrePlan plan : plans) {
            if (!isSpotOrFoodPlan(plan) || dbHelper.hasSpotForPrePlan(plan.getId())) {
                continue;
            }
            hasPlanned = true;
            layoutPlannedCheckins.addView(createPlannedCheckinCard(plan));
        }
        layoutPlannedCheckins.setVisibility(hasPlanned ? View.VISIBLE : View.GONE);
    }

    private View createPlannedCheckinCard(PrePlan plan) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.card_rounded_white));

        TextView title = new TextView(this);
        title.setText(plan.getTitle());
        title.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        title.setTextSize(15);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(title);

        TextView meta = new TextView(this);
        meta.setText(planMeta(plan));
        meta.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        meta.setTextSize(12);
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        metaParams.setMargins(0, dp(6), 0, 0);
        card.addView(meta, metaParams);

        Button complete = new Button(this);
        complete.setText("完成打卡");
        complete.setOnClickListener(v -> completePlannedCheckIn(plan));
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(40));
        buttonParams.setMargins(0, dp(10), 0, 0);
        card.addView(complete, buttonParams);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dp(8));
        card.setLayoutParams(cardParams);
        return card;
    }

    private void completePlannedCheckIn(PrePlan plan) {
        Spot spot = new Spot();
        spot.setTripId(tripId);
        spot.setName(plan.getTitle());
        spot.setType(planTypeToSpotType(plan.getType()));
        spot.setNote(plan.getNote());
        spot.setIsFavorite(0);
        spot.setCreatedAt(isBlank(plan.getTargetDate()) ? "2026-06-09" : plan.getTargetDate());
        spot.setSourcePlanId(plan.getId());
        long result = dbHelper.addSpot(spot);
        if (result != -1) {
            dbHelper.markPrePlanCompleted(plan.getId(), true);
            Toast.makeText(this, "计划打卡已完成", Toast.LENGTH_SHORT).show();
            loadPlannedCheckins();
            loadSpotsList();
        } else {
            Toast.makeText(this, "计划打卡失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSpotsList() {
        List<Spot> spots = dbHelper.getSpotsForTrip(tripId);
        if (spots.isEmpty()) {
            rvSpots.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvSpots.setVisibility(View.VISIBLE);
            rvSpots.setAdapter(new SpotAdapter(this, spots, new SpotAdapter.OnSpotActionListener() {
                @Override
                public void onDelete(Spot spot, int position) {
                    new AlertDialog.Builder(SpotsActivity.this)
                            .setTitle("确认删除打卡")
                            .setMessage("确定要删除“" + spot.getName() + "”的打卡记录吗？")
                            .setPositiveButton("删除", (dialog, which) -> {
                                dbHelper.deleteSpot(spot.getId());
                                loadSpotsList();
                                Toast.makeText(SpotsActivity.this, "景点已移除", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }

                @Override
                public void onToggleFavorite(Spot spot, int position, int nextState) {
                    dbHelper.updateSpotFavoriteStatus(spot.getId(), nextState);
                    loadSpotsList();
                    Toast.makeText(SpotsActivity.this, nextState == 1 ? "已设为最喜欢" : "已取消最喜欢", Toast.LENGTH_SHORT).show();
                }
            }));
        }
    }

    private void addSpotToTrip() {
        String name = etSpotName.getText().toString().trim();
        String note = etSpotNote.getText().toString().trim();
        String type = spSpotType.getSelectedItem().toString();
        int isFav = cbSpotFavorite.isChecked() ? 1 : 0;

        if (name.isEmpty()) {
            Toast.makeText(this, "打卡地点名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        Spot spot = new Spot();
        spot.setTripId(tripId);
        spot.setName(name);
        spot.setType(type);
        spot.setIsFavorite(isFav);
        spot.setNote(note);
        spot.setCreatedAt("2026-06-07");
        spot.setSourcePlanId(0);

        long val = dbHelper.addSpot(spot);
        if (val != -1) {
            Toast.makeText(this, "打卡记录已保存", Toast.LENGTH_SHORT).show();
            etSpotName.setText("");
            etSpotNote.setText("");
            cbSpotFavorite.setChecked(false);
            spSpotType.setSelection(0);
            loadSpotsList();
        } else {
            Toast.makeText(this, "打卡登记失败，请检查输入", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isSpotOrFoodPlan(PrePlan plan) {
        String type = plan.getType();
        if (type == null) return false;
        return type.contains("地方") || type.contains("美食") || type.contains("鍦版柟") || type.contains("缇庨");
    }

    private String planTypeToSpotType(String type) {
        if (type == null) return "其他";
        if (type.contains("美食") || type.contains("缇庨")) return "美食";
        return "景点";
    }

    private String planMeta(PrePlan plan) {
        StringBuilder builder = new StringBuilder();
        if (!isBlank(plan.getTargetDate())) builder.append(plan.getTargetDate());
        if (!isBlank(plan.getTimeText())) {
            if (builder.length() > 0) builder.append("  ");
            builder.append(plan.getTimeText());
        }
        if (!isBlank(plan.getLocation())) {
            if (builder.length() > 0) builder.append(" · ");
            builder.append(plan.getLocation());
        }
        return builder.length() == 0 ? "来自行前计划" : builder.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
