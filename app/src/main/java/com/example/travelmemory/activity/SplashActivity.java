package com.example.travelmemory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.R;
import com.example.travelmemory.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(this::goNext, 1000);
    }

    private void goNext() {
        SessionManager sessionManager = new SessionManager(this);
        Class<?> target = sessionManager.isLoggedIn() ? MainActivity.class : LoginActivity.class;
        startActivity(new Intent(SplashActivity.this, target));
        finish();
    }
}
