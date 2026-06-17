package com.example.travelmemory.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.adapter.ExpenseAdapter;
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.Expense;
import com.example.travelmemory.utils.TravelConstants;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ExpenseActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int tripId = -1;
    private String selectedExpenseDate = "";
    private EditText etAmount, etNote;
    private Spinner spCategory;
    private TextView tvExpenseDate, tvTotalSum, emptyState;
    private TextView tvFood, tvTraffic, tvHotel, tvTicket, tvShopping, tvOther;
    private RecyclerView rvExpenses;
    private final String[] categories = {"餐饮", "交通", "住宿", "门票", "购物", "其他"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getIntExtra(TravelConstants.EXTRA_TRIP_ID, -1);
        if (tripId == -1) {
            Toast.makeText(this, "未定位到目标旅行，账本无法加载", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etAmount = findViewById(R.id.et_expense_amount);
        etNote = findViewById(R.id.et_expense_note);
        spCategory = findViewById(R.id.sp_expense_category);
        tvExpenseDate = findViewById(R.id.tv_expense_date);
        tvTotalSum = findViewById(R.id.tv_expense_total_sum);
        Button btnSubmitAdd = findViewById(R.id.btn_add_expense_submit);
        rvExpenses = findViewById(R.id.rv_expenses);
        emptyState = findViewById(R.id.expense_empty_view);
        tvFood = findViewById(R.id.tv_summary_food);
        tvTraffic = findViewById(R.id.tv_summary_traffic);
        tvHotel = findViewById(R.id.tv_summary_hotel);
        tvTicket = findViewById(R.id.tv_summary_ticket);
        tvShopping = findViewById(R.id.tv_summary_shopping);
        tvOther = findViewById(R.id.tv_summary_other);
        findViewById(R.id.btn_expense_back).setOnClickListener(v -> finish());

        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        spCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));

        Calendar cal = Calendar.getInstance();
        selectedExpenseDate = String.format(Locale.getDefault(), "%d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        tvExpenseDate.setText(selectedExpenseDate);
        tvExpenseDate.setOnClickListener(v -> showDatePicker());
        btnSubmitAdd.setOnClickListener(v -> addNewExpenseItem());
        loadExpensesDetails();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            selectedExpenseDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day);
            tvExpenseDate.setText(selectedExpenseDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void loadExpensesDetails() {
        List<Expense> list = dbHelper.getExpensesForTrip(tripId);
        double sum = 0.0, food = 0.0, traffic = 0.0, hotel = 0.0, ticket = 0.0, shopping = 0.0, other = 0.0;

        for (Expense exp : list) {
            double amt = exp.getAmount();
            sum += amt;
            switch (exp.getCategory() == null ? "其他" : exp.getCategory()) {
                case "餐饮": food += amt; break;
                case "交通": traffic += amt; break;
                case "住宿": hotel += amt; break;
                case "门票": ticket += amt; break;
                case "购物": shopping += amt; break;
                default: other += amt; break;
            }
        }

        tvTotalSum.setText(String.format("¥ %.2f", sum));
        tvFood.setText("餐饮: ¥" + String.format("%.1f", food));
        tvTraffic.setText("交通: ¥" + String.format("%.1f", traffic));
        tvHotel.setText("住宿: ¥" + String.format("%.1f", hotel));
        tvTicket.setText("门票: ¥" + String.format("%.1f", ticket));
        tvShopping.setText("购物: ¥" + String.format("%.1f", shopping));
        tvOther.setText("其他: ¥" + String.format("%.1f", other));

        if (list.isEmpty()) {
            rvExpenses.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvExpenses.setVisibility(View.VISIBLE);
            rvExpenses.setAdapter(new ExpenseAdapter(this, list, (expense, position) -> new AlertDialog.Builder(this)
                    .setTitle("确认删除账目")
                    .setMessage("确定要删除这笔 ¥" + String.format("%.2f", expense.getAmount()) + " 的消费记录吗？")
                    .setPositiveButton("删除", (dialog, id) -> {
                        dbHelper.deleteExpense(expense.getId());
                        loadExpensesDetails();
                        Toast.makeText(this, "账目删除成功！", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show()));
        }
    }

    private void addNewExpenseItem() {
        String amountStr = etAmount.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "请输入消费金额", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0.0) {
                Toast.makeText(this, "消费金额必须大于 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入合法的数字金额", Toast.LENGTH_SHORT).show();
            return;
        }

        if (note.isEmpty()) note = category;
        Expense expense = new Expense();
        expense.setTripId(tripId);
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setNote(note);
        expense.setDate(selectedExpenseDate);

        if (dbHelper.addExpense(expense) != -1) {
            Toast.makeText(this, "记账登记成功！", Toast.LENGTH_SHORT).show();
            etAmount.setText("");
            etNote.setText("");
            spCategory.setSelection(0);
            loadExpensesDetails();
        } else {
            Toast.makeText(this, "记账失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
}
