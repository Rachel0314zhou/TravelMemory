package com.example.travelmemory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.SearchResultAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.SearchResult;
import com.example.travelmemory.utils.BottomNavHelper;
import com.example.travelmemory.utils.DrawerHelper;
import com.example.travelmemory.utils.SessionManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private static final String MOOD_ALL = "\u5168\u90e8";

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int currentUserId = -1;
    private EditText etSearchInput;
    private RecyclerView rvSearchResults;
    private TextView emptyState;
    private DrawerHelper drawerHelper;
    private final Map<String, TextView> moodFilterViews = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        if (currentUserId <= 0) {
            goToLogin();
            return;
        }
        etSearchInput = findViewById(R.id.et_search_input);
        drawerHelper = new DrawerHelper(this);
        drawerHelper.setup();
        Button btnSearchSubmit = findViewById(R.id.btn_search_submit);
        rvSearchResults = findViewById(R.id.rv_search_results);
        emptyState = findViewById(R.id.search_empty_view);
        findViewById(R.id.btn_search_back).setOnClickListener(v -> drawerHelper.open());
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        BottomNavHelper.setup(this, BottomNavHelper.TAB_SEARCH);

        moodFilterViews.put(MOOD_ALL, findViewById(R.id.filter_all));
        moodFilterViews.put("\u5f00\u5fc3", findViewById(R.id.filter_happy));
        moodFilterViews.put("\u5e73\u9759", findViewById(R.id.filter_calm));
        moodFilterViews.put("\u75b2\u60eb", findViewById(R.id.filter_tired));
        moodFilterViews.put("\u5174\u594b", findViewById(R.id.filter_excited));
        moodFilterViews.put("\u611f\u52a8", findViewById(R.id.filter_touched));
        moodFilterViews.put("\u9057\u61be", findViewById(R.id.filter_regret));
        setupMoodFilters();

        btnSearchSubmit.setOnClickListener(v -> executeTextSearch());
        displayResults(dbHelper.getTripSearchResultsForUser(currentUserId));
    }

    private void setupMoodFilters() {
        for (Map.Entry<String, TextView> entry : moodFilterViews.entrySet()) {
            String mood = entry.getKey();
            entry.getValue().setOnClickListener(v -> {
                highlightMoodFilter(mood);
                if (MOOD_ALL.equals(mood)) {
                    executeTextSearch();
                } else {
                    displayResults(dbHelper.moodSearchResultsForUser(currentUserId, mood));
                }
            });
        }
    }

    private void highlightMoodFilter(String activeMood) {
        for (Map.Entry<String, TextView> entry : moodFilterViews.entrySet()) {
            TextView view = entry.getValue();
            if (entry.getKey().equals(activeMood)) {
                view.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_primary));
                view.setTextColor(ContextCompat.getColor(this, R.color.white));
            } else {
                view.setBackground(ContextCompat.getDrawable(this, R.drawable.card_rounded_white));
                view.setTextColor(ContextCompat.getColor(this, R.color.black));
            }
        }
    }

    private void executeTextSearch() {
        String keyword = etSearchInput.getText().toString().trim();
        List<SearchResult> list = keyword.isEmpty() ? dbHelper.getTripSearchResultsForUser(currentUserId) : dbHelper.searchResultsForUser(currentUserId, keyword);
        displayResults(list);
    }

    private void displayResults(List<SearchResult> list) {
        if (list == null || list.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
            rvSearchResults.setAdapter(new SearchResultAdapter(this, list));
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
