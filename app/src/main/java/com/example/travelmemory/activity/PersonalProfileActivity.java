package com.example.travelmemory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.User;
import com.example.travelmemory.utils.ImageUtils;
import com.example.travelmemory.utils.SessionManager;

public class PersonalProfileActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int currentUserId;

    private ImageView ivAvatar;
    private TextView tvNickname;
    private TextView tvUsername;
    private TextView tvAge;
    private TextView tvGender;
    private TextView tvPreference;
    private TextView tvTripCount;
    private TextView tvDiaryCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_profile);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        if (currentUserId <= 0) {
            goToLogin();
            return;
        }

        bindViews();
        findViewById(R.id.btn_profile_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileQuestionnaireActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void bindViews() {
        ivAvatar = findViewById(R.id.iv_profile_avatar);
        tvNickname = findViewById(R.id.tv_profile_nickname);
        tvUsername = findViewById(R.id.tv_profile_username);
        tvAge = findViewById(R.id.tv_profile_age);
        tvGender = findViewById(R.id.tv_profile_gender);
        tvPreference = findViewById(R.id.tv_profile_preference);
        tvTripCount = findViewById(R.id.tv_profile_trip_count);
        tvDiaryCount = findViewById(R.id.tv_profile_diary_count);
    }

    private void loadProfile() {
        User user = dbHelper.getCurrentUser(currentUserId);
        if (user == null) {
            goToLogin();
            return;
        }

        tvNickname.setText(blankToDefault(user.getNickname(), "旅行者"));
        tvUsername.setText("账号：" + blankToDefault(user.getUsername(), sessionManager.getCurrentUsername()));
        tvAge.setText(user.getAge() > 0 ? user.getAge() + " 岁" : "还没有填写年龄");
        tvGender.setText(blankToDefault(user.getGender(), "还没有填写性别"));
        tvPreference.setText(blankToDefault(user.getTravelPreference(), "还没有填写旅行偏好"));
        tvTripCount.setText(String.valueOf(dbHelper.getTotalTripCount(currentUserId)));
        tvDiaryCount.setText(String.valueOf(countDiaries()));
        ImageUtils.displayLocalImage(ivAvatar, user.getAvatarPath(), R.drawable.travel_placeholder);
    }

    private int countDiaries() {
        int count = 0;
        for (com.example.travelmemory.model.Trip trip : dbHelper.getTripsForUser(currentUserId)) {
            count += dbHelper.getDayRecordsForTrip(trip.getId()).size();
        }
        return count;
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private void goToLogin() {
        Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
