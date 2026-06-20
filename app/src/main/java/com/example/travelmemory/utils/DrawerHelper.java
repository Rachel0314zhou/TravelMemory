package com.example.travelmemory.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.R;
import com.example.travelmemory.activity.LoginActivity;
import com.example.travelmemory.activity.MainActivity;
import com.example.travelmemory.activity.SelectTripActivity;
import com.example.travelmemory.activity.SettingsActivity;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.User;

public class DrawerHelper {
    private final Activity activity;
    private final DatabaseHelper dbHelper;
    private final SessionManager sessionManager;
    private final View drawerScrim;
    private final LinearLayout sideDrawer;
    private boolean drawerOpen = false;

    public DrawerHelper(Activity activity) {
        this.activity = activity;
        this.dbHelper = new DatabaseHelper(activity);
        this.sessionManager = new SessionManager(activity);
        this.drawerScrim = activity.findViewById(R.id.drawer_scrim);
        this.sideDrawer = activity.findViewById(R.id.layout_side_drawer);
    }

    public void setup() {
        if (drawerScrim == null || sideDrawer == null) return;
        refreshUser();
        drawerScrim.setOnClickListener(v -> close());
        bindMenu(R.id.menu_my_trips, () -> openActivity(MainActivity.class));
        bindMenu(R.id.menu_pre_trip, () -> openSelectTrip(TravelConstants.MODE_PRE_TRIP));
        bindMenu(R.id.menu_during_trip, () -> openSelectTrip(TravelConstants.MODE_DURING_TRIP));
        bindMenu(R.id.menu_post_trip, () -> openSelectTrip(TravelConstants.MODE_POST_TRIP));
        bindMenu(R.id.menu_memory, () -> openSelectTrip(TravelConstants.MODE_MEMORY));
        bindMenu(R.id.menu_settings, () -> openActivity(SettingsActivity.class));
        bindMenu(R.id.menu_logout, this::confirmLogout);
    }

    public void open() {
        if (drawerScrim == null || sideDrawer == null) return;
        drawerOpen = true;
        drawerScrim.setVisibility(View.VISIBLE);
        sideDrawer.animate().translationX(0f).setDuration(220).start();
    }

    public void close() {
        if (!drawerOpen || sideDrawer == null || drawerScrim == null) return;
        drawerOpen = false;
        sideDrawer.animate().translationX(-sideDrawer.getWidth()).setDuration(200).withEndAction(() -> {
            if (!drawerOpen) drawerScrim.setVisibility(View.GONE);
        }).start();
    }

    public boolean isOpen() {
        return drawerOpen;
    }

    public void refreshUser() {
        int userId = sessionManager.getCurrentUserId();
        User user = userId <= 0 ? null : dbHelper.getCurrentUser(userId);
        TextView nicknameView = activity.findViewById(R.id.tv_drawer_nickname);
        TextView usernameView = activity.findViewById(R.id.tv_drawer_username);
        ImageView avatarView = activity.findViewById(R.id.iv_drawer_avatar);
        if (user == null) {
            if (nicknameView != null) nicknameView.setText("旅行者");
            if (usernameView != null) usernameView.setText("账号：未登录");
            return;
        }
        String nickname = user.getNickname() == null || user.getNickname().isEmpty() ? "旅行者" : user.getNickname();
        String username = user.getUsername() == null || user.getUsername().isEmpty()
                ? sessionManager.getCurrentUsername() : user.getUsername();
        if (nicknameView != null) nicknameView.setText(nickname);
        if (usernameView != null) usernameView.setText("账号：" + username);
        if (avatarView != null) {
            ImageUtils.displayLocalImage(avatarView, user.getAvatarPath(), R.drawable.travel_placeholder);
        }
    }

    private void bindMenu(int id, Runnable action) {
        View view = activity.findViewById(id);
        if (view != null) view.setOnClickListener(v -> action.run());
    }

    private void openSelectTrip(String mode) {
        close();
        Intent intent = new Intent(activity, SelectTripActivity.class);
        intent.putExtra(TravelConstants.EXTRA_MODE, mode);
        activity.startActivity(intent);
    }

    private void openActivity(Class<?> target) {
        close();
        if (activity.getClass().equals(target)) return;
        Intent intent = new Intent(activity, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }

    private void confirmLogout() {
        close();
        new AlertDialog.Builder(activity)
                .setTitle("退出登录")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("退出登录", (dialog, which) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intent);
                    activity.finish();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
