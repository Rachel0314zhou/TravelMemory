package com.example.travelmemory.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.PackingItem;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TravelConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PackingListActivity extends AppCompatActivity {

    private static final String[] CATEGORIES = {"洗漱类", "衣物类", "证件类", "数码类", "出行类", "药品类", "其他类"};

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private int tripId = -1;
    private int currentUserId = -1;

    private LinearLayout layoutContent;
    private TextView tvEmpty;
    private TextView tvProgress;
    private Button btnFilterUnpacked;

    private boolean onlyUnpacked = false;
    private final Set<String> collapsedGroups = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packing_list);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);

        if (currentUserId <= 0 || tripId <= 0 || !dbHelper.isTripOwnedByUser(tripId, currentUserId)) {
            Toast.makeText(this, "请先选择自己的旅行", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        layoutContent = findViewById(R.id.layout_packing_content);
        tvEmpty = findViewById(R.id.tv_packing_empty);
        tvProgress = findViewById(R.id.tv_packing_progress);
        btnFilterUnpacked = findViewById(R.id.btn_filter_unpacked);

        findViewById(R.id.btn_packing_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_packing).setOnClickListener(v -> showAddItemDialog());
        btnFilterUnpacked.setOnClickListener(v -> {
            onlyUnpacked = !onlyUnpacked;
            renderPackingList();
        });

        dbHelper.ensurePackingSelectionPool(tripId);
        dbHelper.normalizePackingCategoriesForTrip(tripId);
        renderPackingList();
    }

    private void renderPackingList() {
        List<PackingItem> items = dbHelper.getPackingItemsForTrip(tripId);
        layoutContent.removeAllViews();
        updateProgress(items);
        btnFilterUnpacked.setText(onlyUnpacked ? "查看全部" : "查看未打包");

        List<PackingItem> unpackedItems = filterByPacked(items, false);
        List<PackingItem> packedItems = filterByPacked(items, true);

        if (items.isEmpty() || (onlyUnpacked && unpackedItems.isEmpty())) {
            tvEmpty.setVisibility(View.VISIBLE);
            layoutContent.setVisibility(View.GONE);
            tvEmpty.setText(onlyUnpacked
                    ? "没有未打包物品\n已保存的物品可以在已打包区域查看"
                    : "还没有行李物品\n点击“添加物品”，把这次需要带的东西加入清单吧。");
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        layoutContent.setVisibility(View.VISIBLE);
        addStatusSection("未打包", unpackedItems, false);
        if (!onlyUnpacked) {
            addStatusSection("已打包", packedItems, true);
        }
    }

    private void updateProgress(List<PackingItem> items) {
        int packed = 0;
        for (PackingItem item : items) {
            if (item.getIsPacked() == 1) packed++;
        }
        int total = items.size();
        int rate = total == 0 ? 0 : Math.round(packed * 100f / total);
        tvProgress.setText(String.format(Locale.getDefault(), "已打包 %d / %d，打包完成率 %d%%", packed, total, rate));
    }

    private List<PackingItem> filterByPacked(List<PackingItem> items, boolean packed) {
        List<PackingItem> result = new ArrayList<>();
        for (PackingItem item : items) {
            if ((item.getIsPacked() == 1) == packed) {
                result.add(item);
            }
        }
        return result;
    }

    private void addStatusSection(String title, List<PackingItem> items, boolean packedSection) {
        TextView sectionTitle = new TextView(this);
        sectionTitle.setText(title);
        sectionTitle.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        sectionTitle.setTextSize(17);
        sectionTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        sectionTitle.setPadding(dp(16), dp(12), dp(16), dp(6));
        layoutContent.addView(sectionTitle);

        if (items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(packedSection ? "还没有已打包物品" : "没有未打包物品");
            empty.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            empty.setTextSize(13);
            empty.setPadding(dp(16), dp(4), dp(16), dp(10));
            layoutContent.addView(empty);
            return;
        }

        Map<String, List<PackingItem>> grouped = groupByCategory(items);
        for (String category : CATEGORIES) {
            List<PackingItem> categoryItems = grouped.get(category);
            if (categoryItems == null || categoryItems.isEmpty()) continue;
            addCategoryCard(title + "_" + category, category, categoryItems);
        }
    }

    private Map<String, List<PackingItem>> groupByCategory(List<PackingItem> items) {
        Map<String, List<PackingItem>> grouped = new LinkedHashMap<>();
        for (String category : CATEGORIES) {
            grouped.put(category, new ArrayList<>());
        }
        for (PackingItem item : items) {
            String category = canonicalCategory(item.getCategory());
            if (!grouped.containsKey(category)) {
                grouped.put("其他类", new ArrayList<>());
                category = "其他类";
            }
            grouped.get(category).add(item);
        }
        return grouped;
    }

    private void addCategoryCard(String key, String category, List<PackingItem> items) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.card_rounded_white));
        card.setPadding(dp(14), dp(12), dp(14), dp(12));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);

        TextView title = new TextView(this);
        title.setText(category + " · " + items.size() + " 件");
        title.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        title.setTextSize(15);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView toggle = new TextView(this);
        boolean collapsed = collapsedGroups.contains(key);
        toggle.setText(collapsed ? "展开 ▾" : "收起 ▴");
        toggle.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        toggle.setTextSize(13);
        toggle.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(toggle);

        card.addView(header);
        LinearLayout itemContainer = new LinearLayout(this);
        itemContainer.setOrientation(LinearLayout.VERTICAL);
        itemContainer.setVisibility(collapsed ? View.GONE : View.VISIBLE);
        card.addView(itemContainer);

        for (PackingItem item : items) {
            itemContainer.addView(createItemRow(item));
        }

        View.OnClickListener listener = v -> {
            if (collapsedGroups.contains(key)) {
                collapsedGroups.remove(key);
            } else {
                collapsedGroups.add(key);
            }
            renderPackingList();
        };
        header.setOnClickListener(listener);
        toggle.setOnClickListener(listener);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp(16), dp(6), dp(16), dp(8));
        layoutContent.addView(card, params);
    }

    private View createItemRow(PackingItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(10), 0, 0);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setChecked(item.getIsPacked() == 1);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.updatePackingItemPacked(item.getId(), isChecked);
            renderPackingList();
        });
        row.addView(checkBox);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(dp(8), 0, 0, 0);

        TextView name = new TextView(this);
        name.setText(item.getItemName());
        name.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        name.setTextSize(15);
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        texts.addView(name);

        TextView status = new TextView(this);
        status.setText(item.getIsPacked() == 1 ? "已打包" : "未打包");
        status.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        status.setTextSize(12);
        texts.addView(status);

        row.addView(texts, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView remove = new TextView(this);
        remove.setText("\u79fb\u9664");
        remove.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        remove.setTextSize(13);
        remove.setTypeface(null, android.graphics.Typeface.BOLD);
        remove.setPadding(dp(12), dp(8), dp(4), dp(8));
        remove.setOnClickListener(v -> confirmRemovePackingItem(item));
        row.addView(remove);
        row.setOnClickListener(v -> checkBox.setChecked(!checkBox.isChecked()));
        return row;
    }

    private void confirmRemovePackingItem(PackingItem item) {
        new AlertDialog.Builder(this)
                .setTitle("\u79fb\u9664\u7269\u54c1")
                .setMessage("\u786e\u5b9a\u5c06\u201c" + item.getItemName() + "\u201d\u4ece\u672c\u6b21\u884c\u674e\u6e05\u5355\u4e2d\u79fb\u9664\u5417\uff1f")
                .setNegativeButton("\u53d6\u6d88", null)
                .setPositiveButton("\u79fb\u9664", (dialog, which) -> {
                    if (item.getIsCustom() == 1) {
                        dbHelper.deleteCustomPackingItem(item.getId());
                    } else {
                        dbHelper.updatePackingItemSelected(item.getId(), false);
                    }
                    renderPackingList();
                })
                .show();
    }

    private void showAddItemDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(16);
        layout.setPadding(padding, padding, padding, 0);

        Spinner spinnerCategory = new Spinner(this);
        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CATEGORIES));
        layout.addView(spinnerCategory);

        EditText etName = new EditText(this);
        etName.setHint("物品名称");
        layout.addView(etName);

        new AlertDialog.Builder(this)
                .setTitle("添加物品")
                .setView(layout)
                .setPositiveButton("保存", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "请填写物品名称", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    PackingItem item = new PackingItem();
                    item.setTripId(tripId);
                    item.setCategory(spinnerCategory.getSelectedItem().toString());
                    item.setItemName(name);
                    item.setIsSelected(1);
                    item.setIsPacked(0);
                    item.setIsCustom(1);
                    item.setCreatedAt("2026-06-09");
                    dbHelper.addPackingItem(item);
                    renderPackingList();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private String canonicalCategory(String category) {
        if ("证件财务".equals(category)) return "证件类";
        if ("衣物鞋帽".equals(category)) return "衣物类";
        if ("洗漱护肤".equals(category)) return "洗漱类";
        if ("电子设备".equals(category)) return "数码类";
        if ("药品健康".equals(category)) return "药品类";
        if ("旅行用品".equals(category)) return "出行类";
        if ("其他".equals(category)) return "其他类";
        return category == null || category.trim().isEmpty() ? "其他类" : category;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
