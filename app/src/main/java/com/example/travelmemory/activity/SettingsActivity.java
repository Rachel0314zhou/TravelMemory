package com.example.travelmemory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.SettingsAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.User;
import com.example.travelmemory.utils.BottomNavHelper;
import com.example.travelmemory.utils.DrawerHelper;
import com.example.travelmemory.utils.ImageUtils;
import com.example.travelmemory.utils.SessionManager;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_AVATAR_ALBUM = 301;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int currentUserId;
    private User currentUser;

    private ImageView ivAvatar;
    private TextView tvNickname;
    private TextView tvUsername;
    private TextView tvAge;
    private TextView tvGender;
    private TextView tvPreference;
    private RecyclerView rvSettingButtons;
    private DrawerHelper drawerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        drawerHelper = new DrawerHelper(this);
        currentUserId = sessionManager.getCurrentUserId();
        if (currentUserId <= 0) {
            goToLogin();
            return;
        }

        ivAvatar = findViewById(R.id.iv_settings_avatar);
        tvNickname = findViewById(R.id.tv_settings_nickname);
        tvUsername = findViewById(R.id.tv_settings_username);
        tvAge = findViewById(R.id.tv_settings_age);
        tvGender = findViewById(R.id.tv_settings_gender);
        tvPreference = findViewById(R.id.tv_settings_preference);
        rvSettingButtons = findViewById(R.id.rv_setting_buttons);

        drawerHelper.setup();
        findViewById(R.id.btn_settings_back).setOnClickListener(v -> drawerHelper.open());
        findViewById(R.id.iv_settings_avatar).setOnClickListener(v -> chooseAvatarFromAlbum());
        findViewById(R.id.btn_settings_avatar).setOnClickListener(v -> chooseAvatarFromAlbum());
        findViewById(R.id.row_logout).setOnClickListener(v -> confirmLogout());
        setupSettingButtons();
        BottomNavHelper.setup(this, BottomNavHelper.TAB_PROFILE);

        loadUser();
    }

    private void setupSettingButtons() {
        rvSettingButtons.setLayoutManager(new LinearLayoutManager(this));
        rvSettingButtons.setNestedScrollingEnabled(false);
        rvSettingButtons.setAdapter(new SettingsAdapter(action -> {
            if (SettingsAdapter.ACTION_EDIT_PROFILE.equals(action)) {
                startActivity(new Intent(this, ProfileQuestionnaireActivity.class));
            } else if (SettingsAdapter.ACTION_EDIT_NICKNAME.equals(action)) {
                showProfileDialog();
            } else if (SettingsAdapter.ACTION_CHANGE_PASSWORD.equals(action)) {
                showPasswordDialog();
            }
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUser();
    }

    private void loadUser() {
        currentUser = dbHelper.getCurrentUser(currentUserId);
        if (currentUser == null) {
            goToLogin();
            return;
        }
        String nickname = currentUser.getNickname() == null || currentUser.getNickname().isEmpty()
                ? "\u65c5\u884c\u8005" : currentUser.getNickname();
        String username = currentUser.getUsername() == null ? "" : currentUser.getUsername();

        tvNickname.setText(nickname);
        tvUsername.setText("\u8d26\u53f7\uff1a" + username);
        tvAge.setText(currentUser.getAge() > 0 ? "\u5e74\u9f84\uff1a" + currentUser.getAge() : "\u5e74\u9f84\uff1a\u8fd8\u6ca1\u6709\u586b\u5199");
        tvGender.setText(isBlank(currentUser.getGender()) ? "\u6027\u522b\uff1a\u8fd8\u6ca1\u6709\u586b\u5199" : "\u6027\u522b\uff1a" + currentUser.getGender());
        tvPreference.setText(isBlank(currentUser.getTravelPreference()) ? "\u65c5\u884c\u504f\u597d\uff1a\u8fd8\u6ca1\u6709\u586b\u5199" : "\u65c5\u884c\u504f\u597d\uff1a" + currentUser.getTravelPreference());
        ImageUtils.displayLocalImage(ivAvatar, currentUser.getAvatarPath(), R.drawable.travel_placeholder);
        drawerHelper.refreshUser();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void chooseAvatarFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_AVATAR_ALBUM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        if (requestCode != REQUEST_AVATAR_ALBUM || data == null || data.getData() == null) {
            Toast.makeText(this, "\u672a\u83b7\u53d6\u5230\u5934\u50cf\u56fe\u7247", Toast.LENGTH_SHORT).show();
            return;
        }

        String avatarPath = ImageUtils.copyAvatarToAppDirectory(this, data.getData());
        if (avatarPath.isEmpty()) {
            Toast.makeText(this, "\u5934\u50cf\u4fdd\u5b58\u5931\u8d25", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHelper.updateAvatarPath(currentUserId, avatarPath);
        Toast.makeText(this, "\u5934\u50cf\u5df2\u66f4\u65b0", Toast.LENGTH_SHORT).show();
        loadUser();
    }

    private void showProfileDialog() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint("请输入新的旅行昵称");
        input.setText(currentUser == null ? "" : currentUser.getNickname());
        int padding = (int) (18 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding / 2, padding, padding / 2);

        new AlertDialog.Builder(this)
                .setTitle("个人资料")
                .setMessage("头像可点击上方图片从相册选择；昵称会显示在首页和侧边栏。")
                .setView(input)
                .setPositiveButton("保存昵称", (dialog, which) -> saveNickname(input))
                .setNegativeButton("取消", null)
                .show();
    }

    private void saveNickname(EditText input) {
        String nickname = input.getText().toString().trim();
        if (nickname.isEmpty()) {
            Toast.makeText(this, "\u6635\u79f0\u4e0d\u80fd\u4e3a\u7a7a", Toast.LENGTH_SHORT).show();
            return;
        }
        int rows = dbHelper.saveNicknameForUser(currentUserId, nickname);
        if (rows <= 0) {
            Toast.makeText(this, "\u6635\u79f0\u4fdd\u5b58\u5931\u8d25\uff0c\u8bf7\u91cd\u8bd5", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "\u6635\u79f0\u5df2\u66f4\u65b0", Toast.LENGTH_SHORT).show();
        loadUser();
        setResult(RESULT_OK);
    }

    private void showPasswordDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        int padding = (int) (18 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding / 2, padding, 0);

        EditText oldPassword = passwordInput("当前密码");
        EditText newPassword = passwordInput("新密码（至少 6 位）");
        EditText confirmPassword = passwordInput("确认新密码");
        layout.addView(oldPassword);
        layout.addView(newPassword);
        layout.addView(confirmPassword);

        new AlertDialog.Builder(this)
                .setTitle("修改密码")
                .setView(layout)
                .setPositiveButton("确认修改", (dialog, which) -> changePassword(oldPassword, newPassword, confirmPassword))
                .setNegativeButton("取消", null)
                .show();
    }

    private EditText passwordInput(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setSingleLine(true);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        return input;
    }

    private void changePassword(EditText oldInput, EditText newInput, EditText confirmInput) {
        String oldPassword = oldInput.getText().toString().trim();
        String newPassword = newInput.getText().toString().trim();
        String confirmPassword = confirmInput.getText().toString().trim();

        if (!dbHelper.checkPassword(currentUserId, oldPassword)) {
            Toast.makeText(this, "\u5f53\u524d\u5bc6\u7801\u9519\u8bef", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPassword.length() < 6) {
            Toast.makeText(this, "\u65b0\u5bc6\u7801\u957f\u5ea6\u4e0d\u80fd\u5c11\u4e8e 6 \u4f4d", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "\u4e24\u6b21\u65b0\u5bc6\u7801\u4e0d\u4e00\u81f4", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHelper.updatePassword(currentUserId, newPassword);
        Toast.makeText(this, "\u5bc6\u7801\u4fee\u6539\u6210\u529f\uff0c\u8bf7\u4f7f\u7528\u65b0\u5bc6\u7801\u767b\u5f55", Toast.LENGTH_LONG).show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("\u9000\u51fa\u767b\u5f55")
                .setMessage("\u786e\u5b9a\u8981\u9000\u51fa\u5f53\u524d\u8d26\u53f7\u5417\uff1f")
                .setPositiveButton("\u9000\u51fa\u767b\u5f55", (dialog, which) -> {
                    sessionManager.logout();
                    goToLogin();
                })
                .setNegativeButton("\u53d6\u6d88", null)
                .show();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
