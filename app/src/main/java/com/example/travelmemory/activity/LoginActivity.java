package com.example.travelmemory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.User;
import com.example.travelmemory.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private EditText etUsername;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        etUsername = findViewById(R.id.et_login_username);
        etPassword = findViewById(R.id.et_login_password);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnGoRegister = findViewById(R.id.btn_go_register);

        btnLogin.setOnClickListener(v -> login());
        btnGoRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "\u8bf7\u8f93\u5165\u7528\u6237\u540d\u548c\u5bc6\u7801", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = dbHelper.login(username, password);
        if (user == null) {
            Toast.makeText(this, "\u7528\u6237\u540d\u6216\u5bc6\u7801\u9519\u8bef", Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.saveLogin(user.getId(), user.getUsername());
        Toast.makeText(this, "\u6b22\u8fce\u56de\u6765\uff0c" + user.getNickname(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
