package com.example.travelmemory.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.R;

public class FeatureHubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_hub);

        findViewById(R.id.btn_feature_back).setOnClickListener(v -> finish());
        bindDeveloping(R.id.feature_achievement);
        bindDeveloping(R.id.feature_mood_report);
        bindDeveloping(R.id.feature_year_report);
        bindDeveloping(R.id.feature_packing);
        bindDeveloping(R.id.feature_inspiration);
        bindDeveloping(R.id.feature_export);
    }

    private void bindDeveloping(int viewId) {
        TextView view = findViewById(viewId);
        if (view == null) return;
        view.setOnClickListener(v -> Toast.makeText(this,
                view.getText() + "\u529f\u80fd\u5f00\u53d1\u4e2d", Toast.LENGTH_SHORT).show());
    }
}
