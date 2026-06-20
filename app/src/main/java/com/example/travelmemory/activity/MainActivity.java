package com.example.travelmemory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.TripAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.model.User;
import com.example.travelmemory.utils.BottomNavHelper;
import com.example.travelmemory.utils.ImageUtils;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TripStateManager;
import com.example.travelmemory.utils.TravelConstants;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private TripStateManager tripStateManager;
    private User currentUser;

    private TextView tvGreeting;
    private TextView tvHomeSubtitle;
    private TextView tvTravelStatusBanner;
    private TextView tvFootprintLine1;
    private TextView tvFootprintLine2;
    private RecyclerView rvTrips;
    private LinearLayout emptyStateLayout;
    private ImageView ivHomeAvatar;
    private ImageView ivDrawerAvatar;
    private TextView tvDrawerNickname;
    private TextView tvDrawerUsername;
    private View drawerScrim;
    private LinearLayout sideDrawer;
    private boolean drawerOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        tripStateManager = new TripStateManager(dbHelper);

        tvGreeting = findViewById(R.id.tv_greeting);
        tvHomeSubtitle = findViewById(R.id.tv_home_subtitle);
        tvTravelStatusBanner = findViewById(R.id.tv_travel_status_banner);
        tvFootprintLine1 = findViewById(R.id.tv_home_footprint_line1);
        tvFootprintLine2 = findViewById(R.id.tv_home_footprint_line2);
        rvTrips = findViewById(R.id.rv_trips);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        ivHomeAvatar = findViewById(R.id.iv_home_avatar);
        ivDrawerAvatar = findViewById(R.id.iv_drawer_avatar);
        tvDrawerNickname = findViewById(R.id.tv_drawer_nickname);
        tvDrawerUsername = findViewById(R.id.tv_drawer_username);
        drawerScrim = findViewById(R.id.drawer_scrim);
        sideDrawer = findViewById(R.id.layout_side_drawer);

        rvTrips.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.btn_add_trip).setOnClickListener(v -> startActivity(new Intent(this, TripTypeActivity.class)));
        findViewById(R.id.btn_delete_trip).setOnClickListener(v -> openSelectTrip(TravelConstants.MODE_DELETE_TRIP));
        findViewById(R.id.layout_search_box).setOnClickListener(v -> openActivity(TripSearchActivity.class));
        findViewById(R.id.btn_menu_header).setOnClickListener(v -> openDrawer());
        ivHomeAvatar.setOnClickListener(v -> openDrawer());
        drawerScrim.setOnClickListener(v -> closeDrawer());
        setupDrawerMenu();
        setupBackNavigation();
        BottomNavHelper.setup(this, BottomNavHelper.TAB_HOME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserDataAndTrips();
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerOpen) {
                    closeDrawer();
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void loadUserDataAndTrips() {
        int userId = sessionManager.getCurrentUserId();
        if (userId <= 0) {
            goToLogin();
            return;
        }

        currentUser = dbHelper.getCurrentUser(userId);
        if (currentUser == null) {
            goToLogin();
            return;
        }
        String nickname = currentUser.getNickname() == null || currentUser.getNickname().isEmpty()
                ? "旅行者" : currentUser.getNickname();
        String username = currentUser.getUsername() == null || currentUser.getUsername().isEmpty()
                ? sessionManager.getCurrentUsername() : currentUser.getUsername();

        tvGreeting.setText("下一站，准备去哪？");
        tvHomeSubtitle.setText(nickname + "，开始记录下一次旅行");
        tvDrawerNickname.setText(nickname);
        tvDrawerUsername.setText("账号：" + username);
        ImageUtils.displayLocalImage(ivHomeAvatar, currentUser.getAvatarPath(), R.drawable.travel_placeholder);
        ImageUtils.displayLocalImage(ivDrawerAvatar, currentUser.getAvatarPath(), R.drawable.travel_placeholder);

        List<Trip> tripList = dbHelper.getTripsForUser(currentUser.getId());
        updateTravelStatusBanner(tripList);
        updateHomeCards(tripList);
        if (tripList.isEmpty()) {
            rvTrips.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            rvTrips.setVisibility(View.VISIBLE);
            rvTrips.setAdapter(new TripAdapter(this, tripList));
        }
    }

    private void updateTravelStatusBanner(List<Trip> tripList) {
        boolean hasInProgressTrip = false;
        for (Trip trip : tripList) {
            if (tripStateManager.isActive(trip)) {
                hasInProgressTrip = true;
                break;
            }
        }

        if (hasInProgressTrip) {
            if (tvTravelStatusBanner.getVisibility() != View.VISIBLE) {
                tvTravelStatusBanner.setAlpha(0f);
                tvTravelStatusBanner.setVisibility(View.VISIBLE);
                tvTravelStatusBanner.animate().alpha(1f).setDuration(180).start();
            }
        } else {
            tvTravelStatusBanner.setVisibility(View.GONE);
            tvTravelStatusBanner.setAlpha(1f);
        }
    }

    private void updateHomeCards(List<Trip> tripList) {
        Trip recentTrip = tripList.isEmpty() ? null : tripList.get(0);
        int dayCount = 0;
        for (Trip trip : tripList) {
            dayCount += dbHelper.getDayRecordsForTrip(trip.getId()).size();
        }
        String recentText = recentTrip == null ? "最近更新：暂无" : "最近更新：" + recentTrip.getName();
        tvFootprintLine1.setText("已经记录 " + tripList.size() + " 趟旅行 · " + dayCount + " 篇日记");
        tvFootprintLine2.setText("照片 " + dbHelper.getTotalPhotoCount(currentUser.getId())
                + " 张  打卡 " + dbHelper.getTotalSpotCount(currentUser.getId())
                + " 个  花费 " + money(dbHelper.getTotalExpense(currentUser.getId()))
                + "\n" + recentText);
    }

    private String money(double amount) {
        return String.format(Locale.getDefault(), "¥%.2f", amount);
    }

    private void setupDrawerMenu() {
        findViewById(R.id.menu_my_trips).setOnClickListener(v -> closeDrawer());
        findViewById(R.id.menu_pre_trip).setOnClickListener(v -> openSelectTrip(TravelConstants.MODE_PRE_TRIP));
        findViewById(R.id.menu_during_trip).setOnClickListener(v -> openSelectTrip(TravelConstants.MODE_DURING_TRIP));
        findViewById(R.id.menu_post_trip).setOnClickListener(v -> openSelectTrip(TravelConstants.MODE_POST_TRIP));
        findViewById(R.id.menu_memory).setOnClickListener(v -> openSelectTrip(TravelConstants.MODE_MEMORY));
        findViewById(R.id.menu_settings).setOnClickListener(v -> openActivity(SettingsActivity.class));
        findViewById(R.id.menu_logout).setOnClickListener(v -> confirmLogout());
    }

    private void openSelectTrip(String mode) {
        closeDrawer();
        Intent intent = new Intent(this, SelectTripActivity.class);
        intent.putExtra(TravelConstants.EXTRA_MODE, mode);
        startActivity(intent);
    }

    private void openActivity(Class<?> target) {
        closeDrawer();
        Intent intent = new Intent(this, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void confirmLogout() {
        closeDrawer();
        new AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    sessionManager.logout();
                    goToLogin();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openDrawer() {
        drawerOpen = true;
        drawerScrim.setVisibility(View.VISIBLE);
        sideDrawer.animate().translationX(0f).setDuration(220).start();
    }

    private void closeDrawer() {
        if (!drawerOpen) return;
        drawerOpen = false;
        sideDrawer.animate().translationX(-sideDrawer.getWidth()).setDuration(200).withEndAction(() -> {
            if (!drawerOpen) drawerScrim.setVisibility(View.GONE);
        }).start();
    }
}
