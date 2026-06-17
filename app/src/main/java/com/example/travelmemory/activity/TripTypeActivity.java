package com.example.travelmemory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.RegretItem;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TravelConstants;

import java.util.List;

public class TripTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_type);

        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.getCurrentUserId() <= 0) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        findViewById(R.id.btn_trip_type_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_simple_record).setOnClickListener(v -> openAddTrip(TravelConstants.TripMode.SIMPLE));
        findViewById(R.id.btn_planned_record).setOnClickListener(v -> openAddTrip(TravelConstants.TripMode.PLANNED));
        findViewById(R.id.btn_view_type_regrets).setOnClickListener(v -> showRegrets(sessionManager.getCurrentUserId()));
    }

    private void openAddTrip(String mode) {
        Intent intent = new Intent(this, AddTripActivity.class);
        intent.putExtra(TravelConstants.EXTRA_TRIP_MODE, mode);
        startActivity(intent);
    }

    private void showRegrets(int userId) {
        List<RegretItem> regrets = new DatabaseHelper(this).getRegretsForUser(userId);
        if (regrets.isEmpty()) {
            Toast.makeText(this, "还没有遗憾回顾，完成旅行整理后可以生成", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder message = new StringBuilder();
        for (RegretItem item : regrets) {
            message.append("【").append(item.getType()).append("】")
                    .append(item.getTitle()).append("\n")
                    .append(item.getReason()).append("\n\n");
        }
        new AlertDialog.Builder(this)
                .setTitle("历史遗憾")
                .setMessage(message.toString().trim())
                .setPositiveButton("知道了", null)
                .show();
    }
}
