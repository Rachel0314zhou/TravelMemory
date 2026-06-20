package com.example.travelmemory.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.SearchResultAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.SearchResult;
import com.example.travelmemory.utils.SessionManager;

import java.util.List;

public class TripSearchActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int currentUserId;
    private EditText etKeyword;
    private RecyclerView rvResults;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_search);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        if (currentUserId <= 0) {
            goToLogin();
            return;
        }

        etKeyword = findViewById(R.id.et_trip_search_keyword);
        rvResults = findViewById(R.id.rv_trip_search_results);
        tvEmpty = findViewById(R.id.tv_trip_search_empty);
        rvResults.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btn_trip_search_back).setOnClickListener(v -> finish());
        etKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadResults(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        loadResults("");
        etKeyword.requestFocus();
        etKeyword.postDelayed(() -> {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (manager != null) {
                manager.showSoftInput(etKeyword, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 180);
    }

    private void loadResults(String keyword) {
        String query = keyword == null ? "" : keyword.trim();
        List<SearchResult> results = query.isEmpty()
                ? dbHelper.getTripSearchResultsForUser(currentUserId)
                : dbHelper.searchResultsForUser(currentUserId, query);
        rvResults.setAdapter(new SearchResultAdapter(this, results));
        boolean empty = results == null || results.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvResults.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
