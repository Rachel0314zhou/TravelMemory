package com.example.travelmemory.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.SpotAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.Spot;

import java.util.List;

public class SpotsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int tripId = -1;

    private EditText etSpotName, etSpotNote;
    private Spinner spSpotType;
    private CheckBox cbSpotFavorite;
    private Button btnAddSpot;

    private RecyclerView rvSpots;
    private TextView emptyState;

    private final String[] spotTypes = {"景点", "美食餐厅", "博物馆", "公园风景", "特色街道", "商场购物", "其他选项"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spots);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getIntExtra("trip_id", -1);

        if (tripId == -1) {
            Toast.makeText(this, "未找到目标旅途，打卡失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // View mapping
        etSpotName = findViewById(R.id.et_spot_name);
        etSpotNote = findViewById(R.id.et_spot_note);
        spSpotType = findViewById(R.id.sp_spot_type);
        cbSpotFavorite = findViewById(R.id.cb_spot_favorite);
        btnAddSpot = findViewById(R.id.btn_add_spot);

        rvSpots = findViewById(R.id.rv_spots);
        emptyState = findViewById(R.id.spots_empty_view);

        findViewById(R.id.btn_spot_back).setOnClickListener(v -> finish());

        rvSpots.setLayoutManager(new LinearLayoutManager(this));

        // Spinners binding
        ArrayAdapter<String> adapterType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spotTypes);
        spSpotType.setAdapter(adapterType);

        // Click save button action
        btnAddSpot.setOnClickListener(v -> addSpotToTrip());

        loadSpotsList();
    }

    private void loadSpotsList() {
        List<Spot> spots = dbHelper.getSpotsForTrip(tripId);
        if (spots.isEmpty()) {
            rvSpots.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvSpots.setVisibility(View.VISIBLE);

            SpotAdapter adapter = new SpotAdapter(this, spots, new SpotAdapter.OnSpotActionListener() {
                @Override
                public void onDelete(Spot spot, int position) {
                    new AlertDialog.Builder(SpotsActivity.this)
                        .setTitle("确认删除打卡")
                        .setMessage("确定要删除对打卡点“" + spot.getName() + "”的打卡印记吗？")
                        .setPositiveButton("删除", (dialog, which) -> {
                            dbHelper.deleteSpot(spot.getId());
                            loadSpotsList(); // Reload
                            Toast.makeText(SpotsActivity.this, "景点已移除", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("取消", null)
                        .show();
                }

                @Override
                public void onToggleFavorite(Spot spot, int position, int nextState) {
                    // Quick SQLite update
                    dbHelper.updateSpotFavoriteStatus(spot.getId(), nextState);
                    loadSpotsList(); // Reload
                    if (nextState == 1) {
                        Toast.makeText(SpotsActivity.this, "已设为心动打卡点！❤️", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SpotsActivity.this, "已取消设为最爱", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            rvSpots.setAdapter(adapter);
        }
    }

    private void addSpotToTrip() {
        String name = etSpotName.getText().toString().trim();
        String note = etSpotNote.getText().toString().trim();
        String type = spSpotType.getSelectedItem().toString();
        int isFav = cbSpotFavorite.isChecked() ? 1 : 0;

        if (name.isEmpty()) {
            Toast.makeText(this, "打卡地名可不能为空哦", Toast.LENGTH_SHORT).show();
            return;
        }

        Spot spot = new Spot();
        spot.setTripId(tripId);
        spot.setName(name);
        spot.setType(type);
        spot.setIsFavorite(isFav);
        spot.setNote(note);
        spot.setCreatedAt("2026-06-07");

        long val = dbHelper.addSpot(spot);
        if (val != -1) {
            Toast.makeText(this, "景点打卡登记成功！🎯", Toast.LENGTH_SHORT).show();
            // Clear entry logs
            etSpotName.setText("");
            etSpotNote.setText("");
            cbSpotFavorite.setChecked(false);
            spSpotType.setSelection(0);
            loadSpotsList(); // refresh
        } else {
            Toast.makeText(this, "打卡登记失败，请核查输入", Toast.LENGTH_SHORT).show();
        }
    }
}
