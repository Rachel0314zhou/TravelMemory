package com.example.travelmemory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.User;

public class SplashActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LinearLayout nicknameSection;
    private EditText etNickname;
    private Button btnEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        dbHelper = new DatabaseHelper(this);

        nicknameSection = findViewById(R.id.nickname_section);
        etNickname = findViewById(R.id.et_nickname);
        btnEnter = findViewById(R.id.btn_enter);

        checkUserNickname();
    }

    private void checkUserNickname() {
        User user = dbHelper.getUser();
        if (user == null || user.getNickname() == null || user.getNickname().trim().isEmpty()) {
            // First time entry, prompt nickname creation
            nicknameSection.setVisibility(View.VISIBLE);
            btnEnter.setOnClickListener(v -> {
                String nickname = etNickname.getText().toString().trim();
                if (nickname.isEmpty()) {
                    Toast.makeText(this, R.string.no_nickname_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                // Save nickname
                dbHelper.saveNickname(nickname);
                Toast.makeText(this, "欢迎来到旅迹本，" + nickname, Toast.LENGTH_SHORT).show();
                goToMain();
            });
        } else {
            // Nickname exists, transition to MainActivity with a 2-second delay
            nicknameSection.setVisibility(View.GONE);
            new Handler().postDelayed(this::goToMain, 2000);
        }
    }

    private void goToMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Clear from backstack
    }
}
