package com.example.travelmemory.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.SearchResultAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.Trip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    private EditText etSearchInput;
    private Button btnSearchSubmit;
    private RecyclerView rvSearchResults;
    private TextView emptyState;

    private String selectedMoodFilter = null; // null means no mood filter

    // Mood filters map
    private final Map<String, TextView> moodFilterViews = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = new DatabaseHelper(this);

        etSearchInput = findViewById(R.id.et_search_input);
        btnSearchSubmit = findViewById(R.id.btn_search_submit);
        rvSearchResults = findViewById(R.id.rv_search_results);
        emptyState = findViewById(R.id.search_empty_view);

        findViewById(R.id.btn_search_back).setOnClickListener(v -> finish());

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));

        // Mood filters mapping
        moodFilterViews.put("全部", findViewById(R.id.filter_all));
        moodFilterViews.put("开心 😊", findViewById(R.id.filter_happy));
        moodFilterViews.put("平静 😐", findViewById(R.id.filter_calm));
        moodFilterViews.put("疲惫 😴", findViewById(R.id.filter_tired));
        moodFilterViews.put("兴奋 🤩", findViewById(R.id.filter_excited));
        moodFilterViews.put("感动 🥹", findViewById(R.id.filter_touched));
        moodFilterViews.put("遗憾 😔", findViewById(R.id.filter_regret));

        setupMoodFilters();

        // Submit button
        btnSearchSubmit.setOnClickListener(v -> executeTextSearch());

        // Default: display all trips
        displayTrips(dbHelper.getAllTrips());
    }

    private void setupMoodFilters() {
        for (Map.Entry<String, TextView> entry : moodFilterViews.entrySet()) {
            String moodStr = entry.getKey();
            TextView view = entry.getValue();
            view.setOnClickListener(v -> {
                highlightMoodFilter(moodStr);
                if (moodStr.equals("全部")) {
                    selectedMoodFilter = null;
                    executeTextSearch(); // run text keyword query
                } else {
                    selectedMoodFilter = moodStr;
                    // Run mood query directly
                    List<Trip> list = dbHelper.filterTripsByMood(moodStr);
                    displayTrips(list);
                }
            });
        }
    }

    private void highlightMoodFilter(String activeMood) {
        for (Map.Entry<String, TextView> entry : moodFilterViews.entrySet()) {
            String text = entry.getKey();
            TextView view = entry.getValue();
            if (text.equals(activeMood)) {
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
        List<Trip> list;
        if (keyword.isEmpty()) {
            list = dbHelper.getAllTrips();
        } else {
            list = dbHelper.searchTrips(keyword);
        }
        displayTrips(list);
    }

    private void displayTrips(List<Trip> list) {
        if (list.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
            SearchResultAdapter adapter = new SearchResultAdapter(this, list);
            rvSearchResults.setAdapter(adapter);
        }
    }
}
