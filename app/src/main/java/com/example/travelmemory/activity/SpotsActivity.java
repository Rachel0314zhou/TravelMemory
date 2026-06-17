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
import com.example.travelmemory.utils.TravelConstants;

import java.util.List;

public class SpotsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int tripId = -1;
    private EditText etSpotName, etSpotNote;
    private Spinner spSpotType;
    private CheckBox cbSpotFavorite;
    private RecyclerView rvSpots;
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
        emptyState = findViewById(R.id.spots_empty_view);
        findViewById(R.id.btn_spot_back).setOnClickListener(v -> finish());

        rvSpots.setLayoutManager(new LinearLayoutManager(this));
        spSpotType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spotTypes));
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
}
