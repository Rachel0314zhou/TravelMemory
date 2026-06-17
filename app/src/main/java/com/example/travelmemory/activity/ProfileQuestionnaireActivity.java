package com.example.travelmemory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.User;
import com.example.travelmemory.utils.SessionManager;

public class ProfileQuestionnaireActivity extends AppCompatActivity {

    private static final int MAX_PREFERENCE_COUNT = 3;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int currentUserId;

    private EditText etNickname;
    private EditText etAge;
    private EditText etPreferenceOther;
    private RadioGroup rgGender;
    private CheckBox cbPreferenceOther;
    private CheckBox[] preferenceBoxes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_questionnaire);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        if (currentUserId <= 0) {
            goToLogin();
            return;
        }

        bindViews();
        loadExistingProfile();
        findViewById(R.id.btn_questionnaire_close).setOnClickListener(v -> finish());
        findViewById(R.id.btn_questionnaire_save).setOnClickListener(v -> saveProfile());
        findViewById(R.id.btn_questionnaire_skip).setOnClickListener(v -> goHome());
    }

    private void bindViews() {
        etNickname = findViewById(R.id.et_profile_nickname);
        etAge = findViewById(R.id.et_profile_age);
        etAge.setInputType(InputType.TYPE_CLASS_NUMBER);
        rgGender = findViewById(R.id.rg_profile_gender);
        etPreferenceOther = findViewById(R.id.et_preference_other);
        cbPreferenceOther = findViewById(R.id.cb_preference_other);
        preferenceBoxes = new CheckBox[]{
                findViewById(R.id.cb_preference_nature),
                findViewById(R.id.cb_preference_city),
                findViewById(R.id.cb_preference_food),
                findViewById(R.id.cb_preference_history),
                findViewById(R.id.cb_preference_vacation),
                findViewById(R.id.cb_preference_fast),
                findViewById(R.id.cb_preference_family),
                cbPreferenceOther
        };
        setupPreferenceLimit();
    }

    private void loadExistingProfile() {
        User user = dbHelper.getCurrentUser(currentUserId);
        if (user == null) {
            goToLogin();
            return;
        }
        etNickname.setText(user.getNickname() == null ? "" : user.getNickname());
        if (user.getAge() > 0) {
            etAge.setText(String.valueOf(user.getAge()));
        }
        checkGender(user.getGender());
        checkPreference(user.getTravelPreference());
    }

    private void saveProfile() {
        String nickname = etNickname.getText().toString().trim();
        int age = parseAge(etAge.getText().toString().trim());
        String gender = selectedText(rgGender);
        String preference = selectedPreferenceText();

        if (nickname.isEmpty()) {
            nickname = "旅行者";
        }
        if (age < 0 || age > 120) {
            Toast.makeText(this, "请输入合理的年龄", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cbPreferenceOther.isChecked() && etPreferenceOther.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "请输入其他旅行偏好", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHelper.updateUserProfile(currentUserId, nickname, age, gender, preference, true);
        Toast.makeText(this, "我的旅行画像已保存", Toast.LENGTH_SHORT).show();
        goHome();
    }

    private int parseAge(String text) {
        if (text.isEmpty()) return 0;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String selectedText(RadioGroup group) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return "";
        TextView view = findViewById(id);
        return view == null ? "" : view.getText().toString();
    }

    private void checkGender(String gender) {
        if ("女".equals(gender)) {
            rgGender.check(R.id.rb_gender_female);
        } else if ("男".equals(gender)) {
            rgGender.check(R.id.rb_gender_male);
        } else if ("其他".equals(gender)) {
            rgGender.check(R.id.rb_gender_other);
        }
    }

    private void setupPreferenceLimit() {
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (isChecked && countSelectedPreferences() > MAX_PREFERENCE_COUNT) {
                buttonView.setChecked(false);
                Toast.makeText(this, "最多选择 3 个旅行偏好", Toast.LENGTH_SHORT).show();
                return;
            }
            etPreferenceOther.setVisibility(cbPreferenceOther.isChecked() ? View.VISIBLE : View.GONE);
        };
        for (CheckBox box : preferenceBoxes) {
            box.setOnCheckedChangeListener(listener);
        }
    }

    private int countSelectedPreferences() {
        int count = 0;
        for (CheckBox box : preferenceBoxes) {
            if (box.isChecked()) count++;
        }
        return count;
    }

    private String selectedPreferenceText() {
        java.util.List<String> selectedPreferences = new java.util.ArrayList<>();
        for (CheckBox box : preferenceBoxes) {
            if (!box.isChecked()) continue;
            if (box == cbPreferenceOther) {
                String custom = etPreferenceOther.getText().toString().trim();
                if (!custom.isEmpty()) selectedPreferences.add(custom);
            } else {
                selectedPreferences.add(box.getText().toString());
            }
        }
        return android.text.TextUtils.join("、", selectedPreferences);
    }

    private void checkPreference(String preference) {
        if (preference == null || preference.trim().isEmpty()) return;
        for (String item : preference.split("、")) {
            checkPreferenceItem(item.trim());
        }
        etPreferenceOther.setVisibility(cbPreferenceOther.isChecked() ? View.VISIBLE : View.GONE);
    }

    private void checkPreferenceItem(String preference) {
        if ("自然风光".equals(preference)) setPreferenceChecked(R.id.cb_preference_nature);
        else if ("城市漫游".equals(preference)) setPreferenceChecked(R.id.cb_preference_city);
        else if ("美食探店".equals(preference)) setPreferenceChecked(R.id.cb_preference_food);
        else if ("人文历史".equals(preference)) setPreferenceChecked(R.id.cb_preference_history);
        else if ("轻松度假".equals(preference)) setPreferenceChecked(R.id.cb_preference_vacation);
        else if ("特种兵旅行".equals(preference)) setPreferenceChecked(R.id.cb_preference_fast);
        else if ("亲子/家庭".equals(preference)) setPreferenceChecked(R.id.cb_preference_family);
        else if (!preference.isEmpty()) {
            cbPreferenceOther.setChecked(true);
            etPreferenceOther.setText(preference);
        }
    }

    private void setPreferenceChecked(int id) {
        CheckBox box = findViewById(id);
        if (box != null) box.setChecked(true);
    }

    private void goHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
