package com.example.travelmemory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.utils.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private EditText etUsername;
    private EditText etPassword;
    private EditText etNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        etUsername = findViewById(R.id.et_register_username);
        etPassword = findViewById(R.id.et_register_password);
        etNickname = findViewById(R.id.et_register_nickname);
        Button btnRegister = findViewById(R.id.btn_register);
        Button btnBackLogin = findViewById(R.id.btn_back_login);

        btnRegister.setOnClickListener(v -> register());
        btnBackLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
            Toast.makeText(this, "\u8bf7\u5b8c\u6574\u586b\u5199\u7528\u6237\u540d\u3001\u5bc6\u7801\u548c\u6635\u79f0", Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.length() < 3) {
            Toast.makeText(this, "\u7528\u6237\u540d\u81f3\u5c11 3 \u4e2a\u5b57\u7b26", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "\u5bc6\u7801\u81f3\u5c11 6 \u4f4d", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dbHelper.isUsernameExists(username)) {
            Toast.makeText(this, "\u7528\u6237\u540d\u5df2\u5b58\u5728\uff0c\u8bf7\u6362\u4e00\u4e2a", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = dbHelper.registerUser(username, password, nickname);
        if (result != -1) {
            sessionManager.saveLogin((int) result, username);
            dbHelper.ensureSeedDataForUser((int) result);
            Toast.makeText(this, "\u6ce8\u518c\u6210\u529f\uff0c\u5148\u5b8c\u5584\u65c5\u884c\u6863\u6848", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ProfileQuestionnaireActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "\u6ce8\u518c\u5931\u8d25\uff0c\u8bf7\u91cd\u8bd5", Toast.LENGTH_SHORT).show();
        }
    }
}
