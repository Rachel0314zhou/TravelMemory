package com.example.travelmemory.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.R;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.DateUtils;
import com.example.travelmemory.utils.ImageUtils;
import com.example.travelmemory.utils.SessionManager;
import com.example.travelmemory.utils.TripNavigator;
import com.example.travelmemory.utils.TripStateManager;
import com.example.travelmemory.utils.TravelConstants;

import java.util.Locale;

public class TripDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TripStateManager tripStateManager;
    private int tripId = -1;
    private int currentUserId = -1;
    private String mode = TravelConstants.MODE_OVERVIEW;
    private Trip currentTrip;

    private ImageView ivDetailCover;
    private TextView tvDetailName;
    private TextView tvDetailDestination;
    private TextView tvDetailDates;
    private TextView tvDetailDaysBubble;
    private TextView tvDetailStatus;
    private TextView tvModeTitle;
    private TextView tvModeDescription;
    private LinearLayout layoutModeContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        dbHelper = new DatabaseHelper(this);
        tripStateManager = new TripStateManager(dbHelper);
        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);
        mode = getIntent().getStringExtra(TravelConstants.EXTRA_MODE);
        if (mode == null || mode.isEmpty()) mode = TravelConstants.MODE_OVERVIEW;

        if (currentUserId <= 0 || tripId <= 0 || !dbHelper.isTripOwnedByUser(tripId, currentUserId)) {
            Toast.makeText(this, "请先选择自己的旅行", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupHeaderActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTripDetails();
    }

    private void bindViews() {
        ivDetailCover = findViewById(R.id.iv_detail_cover);
        tvDetailName = findViewById(R.id.tv_detail_name);
        tvDetailDestination = findViewById(R.id.tv_detail_destination);
        tvDetailDates = findViewById(R.id.tv_detail_dates);
        tvDetailDaysBubble = findViewById(R.id.tv_detail_days_bubble);
        tvDetailStatus = findViewById(R.id.tv_detail_status);
        tvModeTitle = findViewById(R.id.tv_mode_title);
        tvModeDescription = findViewById(R.id.tv_mode_description);
        layoutModeContent = findViewById(R.id.layout_mode_content);
    }

    private void setupHeaderActions() {
        findViewById(R.id.btn_detail_back).setOnClickListener(v -> finish());
        ImageButton btnEdit = findViewById(R.id.btn_detail_edit);
        ImageButton btnDelete = findViewById(R.id.btn_detail_delete);
        btnEdit.setOnClickListener(v -> openWithTrip(AddTripActivity.class));
        btnDelete.setVisibility(View.VISIBLE);
        btnDelete.setOnClickListener(v -> confirmDeleteTrip());
    }

    private void loadTripDetails() {
        currentTrip = dbHelper.getTripForUser(tripId, currentUserId);
        if (currentTrip == null) {
            Toast.makeText(this, "无权查看这段旅行", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvDetailName.setText(currentTrip.getName());
        tvDetailDestination.setText("目的地：" + currentTrip.getDestination());
        tvDetailDates.setText(currentTrip.getStartDate() + " ~ " + currentTrip.getEndDate());
        long days = DateUtils.getDaysBetween(currentTrip.getStartDate(), currentTrip.getEndDate());
        tvDetailDaysBubble.setText(days + " 天");
        tvDetailStatus.setText("当前阶段：" + TravelConstants.formatTripStatusText(currentTrip.getStatus()));
        ImageUtils.displayLocalImage(ivDetailCover, currentTrip.getCoverPath(), R.drawable.travel_placeholder);

        renderMode();
    }

    private void renderMode() {
        layoutModeContent.removeAllViews();
        switch (mode) {
            case TravelConstants.MODE_PRE_TRIP:
                showPreTripMode();
                break;
            case TravelConstants.MODE_PACKING:
                showPackingMode();
                break;
            case TravelConstants.MODE_DURING_TRIP:
                showDuringTripMode();
                break;
            case TravelConstants.MODE_POST_TRIP:
                showPostTripMode();
                break;
            case TravelConstants.MODE_MEMORY:
                showMemoryMode();
                break;
            case TravelConstants.MODE_EXPENSE:
                showExpenseMode();
                break;
            default:
                showOverviewMode();
                break;
        }
    }

    private void showOverviewMode() {
        tvModeTitle.setText("旅行总览");
        tvModeDescription.setText(overViewHint());
        addSectionTitle("阶段入口");
        addActionCard("行前准备", "整理计划和行李，让出发更从容。", () -> switchMode(TravelConstants.MODE_PRE_TRIP));
        addActionCard("旅行记录", "记录日记、照片、打卡和花费。", () -> switchMode(TravelConstants.MODE_DURING_TRIP));
        addActionCard("旅行整理", "补充记录，回顾遗憾，生成回忆。", () -> switchMode(TravelConstants.MODE_POST_TRIP));
        addSectionTitle("数据概览");
        addStatGrid();
        addSecondaryAction("查看回忆卡片", () -> switchMode(TravelConstants.MODE_MEMORY));
    }

    private void showPreTripMode() {
        tvModeTitle.setText("行前准备");
        tvModeDescription.setText("整理计划和行李，让出发更从容。");
        addActionCard("添加行前计划", "写下想去的地方、想吃的美食和待办事项。", () -> openWithTrip(PreTripPlanActivity.class));
        addActionCard("查看行李清单", "确认必带物品和未打包提醒。", () -> openWithTrip(PackingListActivity.class));
        addPrimaryAction("开始旅行记录", () -> {
            tripStateManager.startTrip(tripId);
            mode = TravelConstants.MODE_DURING_TRIP;
            loadTripDetails();
        });
    }

    private void showPackingMode() {
        int total = dbHelper.countPackingItemsForTrip(tripId);
        int packed = dbHelper.countCheckedPackingItemsForTrip(tripId);
        tvModeTitle.setText("行李清单");
        tvModeDescription.setText("已打包 " + packed + " / " + total + "，出发前再检查一遍。");
        addStatCard("打包进度", packed + " / " + total);
        addActionCard("打开行李清单", "查看分类、添加物品并勾选打包状态。", () -> openWithTrip(PackingListActivity.class));
    }

    private void showDuringTripMode() {
        tvModeTitle.setText("旅行记录");
        tvModeDescription.setText("记录路上的日记、照片、打卡和花费。");
        addActionCard("写旅行日记", "记录今天发生的故事和心情。", () -> openWithTrip(DayRecordActivity.class));
        addActionCard("添加旅行照片", "保存路上的画面。", () -> openWithTrip(PhotoActivity.class));
        addActionCard("景点/美食打卡", "标记值得记住的地点。", () -> openWithTrip(SpotsActivity.class));
        addActionCard("记录花费", "记下交通、住宿、餐饮等支出。", () -> openWithTrip(ExpenseActivity.class));
        addSecondaryAction("结束旅行", this::confirmEndTrip);
    }

    private void showPostTripMode() {
        tvModeTitle.setText("旅行整理");
        tvModeDescription.setText("补充记录，回顾遗憾，生成属于这次旅行的回忆卡片。");
        addSectionTitle("补充记录");
        addActionCard("补写旅行日记", "把漏掉的故事补回来。", () -> openWithTrip(DayRecordActivity.class));
        addActionCard("补充旅行照片", "整理这趟旅行的照片。", () -> openWithTrip(PhotoActivity.class));
        addActionCard("补记旅行花费", "完善本次旅行的账单。", () -> openWithTrip(ExpenseActivity.class));
        addSectionTitle("回顾计划");
        addActionCard("遗憾回顾", "看看这次没来得及完成的计划。", () -> openWithTrip(PlanCompareActivity.class));
        addSectionTitle("旅行成果");
        addActionCard("旅行回顾", "上下滑动浏览照片、文字和心情标签。", () -> openWithTrip(TripRecapActivity.class));
    }

    private void showMemoryMode() {
        tvModeTitle.setText("回忆卡片");
        tvModeDescription.setText("把这次旅行整理成一张值得收藏的回忆卡片。");
        addActionCard("查看旅行回顾", "打开沉浸式滑动回顾。", () -> openWithTrip(TripRecapActivity.class));
        addActionCard("生成/重新生成回忆", "根据日记、照片、打卡和花费更新回忆。", () -> openWithTrip(TripRecapActivity.class));
        addActionCard("查看全部回忆", "浏览所有旅行的回忆卡片。", () -> startActivity(new Intent(this, MemoryListActivity.class)));
    }

    private void showExpenseMode() {
        tvModeTitle.setText("花费统计");
        tvModeDescription.setText("本次旅行总支出：" + money(dbHelper.getTotalExpenseForTrip(tripId)));
        addStatCard("总支出", money(dbHelper.getTotalExpenseForTrip(tripId)));
        addStatCard("餐饮", money(dbHelper.getCategoryExpenseAmount(tripId, "餐饮")));
        addStatCard("交通", money(dbHelper.getCategoryExpenseAmount(tripId, "交通")));
        addStatCard("住宿", money(dbHelper.getCategoryExpenseAmount(tripId, "住宿")));
        addActionCard("记录花费", "新增一笔旅行支出。", () -> openWithTrip(ExpenseActivity.class));
        addActionCard("查看支出明细", "查看本次旅行的账单列表。", () -> openWithTrip(ExpenseActivity.class));
        addActionCard("查看支出统计", "进入轻量数据面板。", () -> startActivity(new Intent(this, StatisticsActivity.class)));
    }

    private void switchMode(String nextMode) {
        mode = nextMode;
        renderMode();
    }

    private void confirmEndTrip() {
        new AlertDialog.Builder(this)
                .setTitle("结束旅行")
                .setMessage("确定结束当前旅行并进入旅行整理阶段吗？\n\n进入整理后，你仍然可以补写日记、补充照片和记录花费。")
                .setNegativeButton("取消", null)
                .setPositiveButton("结束旅行", (dialog, which) -> {
                    tripStateManager.finishTrip(tripId);
                    mode = TravelConstants.MODE_POST_TRIP;
                    loadTripDetails();
                })
                .show();
    }

    private void confirmDeleteTrip() {
        new AlertDialog.Builder(this)
                .setTitle("\u5220\u9664\u65c5\u884c")
                .setMessage("\u786e\u5b9a\u5220\u9664\u8fd9\u8d9f\u65c5\u884c\u5417\uff1f\u76f8\u5173\u7684\u65e5\u8bb0\u3001\u7167\u7247\u8bb0\u5f55\u3001\u6253\u5361\u3001\u82b1\u8d39\u3001\u8ba1\u5212\u548c\u884c\u674e\u6e05\u5355\u4e5f\u4f1a\u4e00\u5e76\u79fb\u9664\u3002")
                .setNegativeButton("\u53d6\u6d88", null)
                .setPositiveButton("\u5220\u9664", (dialog, which) -> {
                    if (dbHelper.deleteTripForUser(tripId, currentUserId)) {
                        Toast.makeText(this, "\u65c5\u884c\u5df2\u5220\u9664", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "\u65e0\u6743\u5220\u9664\u8fd9\u8d9f\u65c5\u884c", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void openWithTrip(Class<?> target) {
        TripNavigator.openFeature(this, target, tripId);
    }

    private void addSectionTitle(String title) {
        TextView view = new TextView(this);
        view.setText(title);
        view.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        view.setTextSize(16);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(4, dp(18), 4, dp(8));
        layoutModeContent.addView(view, params);
    }

    private void addActionCard(String title, String subtitle, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(14), dp(18), dp(14));
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.card_rounded_white));
        card.setOnClickListener(v -> action.run());

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        titleView.setTextSize(16);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(titleView);

        TextView subtitleView = new TextView(this);
        subtitleView.setText(subtitle);
        subtitleView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        subtitleView.setTextSize(13);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        subtitleParams.setMargins(0, dp(6), 0, 0);
        card.addView(subtitleView, subtitleParams);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(8), 0, dp(6));
        layoutModeContent.addView(card, params);
    }

    private void addSecondaryAction(String title, Runnable action) {
        TextView button = new TextView(this);
        button.setText(title);
        button.setGravity(android.view.Gravity.CENTER);
        button.setTextColor(ContextCompat.getColor(this, R.color.primary));
        button.setTextSize(15);
        button.setTypeface(null, android.graphics.Typeface.BOLD);
        button.setBackground(ContextCompat.getDrawable(this, R.drawable.card_rounded_primary_light));
        button.setPadding(dp(16), dp(14), dp(16), dp(14));
        button.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(10), 0, 0);
        layoutModeContent.addView(button, params);
    }

    private void addPrimaryAction(String title, Runnable action) {
        TextView button = new TextView(this);
        button.setText(title);
        button.setGravity(android.view.Gravity.CENTER);
        button.setTextColor(ContextCompat.getColor(this, R.color.white));
        button.setTextSize(16);
        button.setTypeface(null, android.graphics.Typeface.BOLD);
        button.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_primary));
        button.setPadding(dp(16), dp(15), dp(16), dp(15));
        button.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52));
        params.setMargins(0, dp(12), 0, 0);
        layoutModeContent.addView(button, params);
    }

    private void addStatGrid() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(createSmallStat("日记", String.valueOf(dbHelper.getDayRecordsForTrip(tripId).size())), statParams(0));
        row.addView(createSmallStat("照片", String.valueOf(dbHelper.countPhotosForTrip(tripId))), statParams(dp(8)));
        row.addView(createSmallStat("打卡", String.valueOf(dbHelper.countSpotsForTrip(tripId))), statParams(dp(8)));
        row.addView(createSmallStat("支出", money(dbHelper.getTotalExpenseForTrip(tripId))), statParams(dp(8)));
        layoutModeContent.addView(row);
    }

    private void addStatCard(String label, String value) {
        layoutModeContent.addView(createStatCard(label, value), fullCardParams());
    }

    private LinearLayout createSmallStat(String label, String value) {
        LinearLayout card = createStatCard(label, value);
        card.setGravity(android.view.Gravity.CENTER);
        return card;
    }

    private LinearLayout createStatCard(String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackground(ContextCompat.getDrawable(this, R.drawable.card_rounded_white));

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        labelView.setTextSize(12);
        card.addView(labelView);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextColor(ContextCompat.getColor(this, R.color.primary));
        valueView.setTextSize(17);
        valueView.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(valueView);
        return card;
    }

    private LinearLayout.LayoutParams statParams(int marginStart) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(marginStart, 0, 0, 0);
        return params;
    }

    private LinearLayout.LayoutParams fullCardParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(8), 0, dp(6));
        return params;
    }

    private String overViewHint() {
        if (tripStateManager.isReviewing(currentTrip)) {
            return "这趟旅行已进入整理阶段，可以补充记录并生成回忆卡片。";
        }
        if (TravelConstants.isPlannedTrip(currentTrip.getMode())) {
            return "这是一趟规划中的旅行，可以先完成行前准备。";
        }
        return "这是一趟补录旅行，可以直接补写日记、照片和回忆。";
    }

    private String money(double amount) {
        return String.format(Locale.getDefault(), "¥ %.2f", amount);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
