package com.example.travelmemory.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.model.Expense;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface OnExpenseDeleteListener {
        void onDelete(Expense expense, int position);
    }

    private final Context context;
    private final List<Expense> expenses;
    private final OnExpenseDeleteListener listener;

    public ExpenseAdapter(Context context, List<Expense> expenses, OnExpenseDeleteListener listener) {
        this.context = context;
        this.expenses = expenses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.tvCategory.setText(expense.getCategory());
        String note = expense.getNote();
        holder.tvNote.setText(note == null || note.isEmpty() ? "未填写备注" : note);
        holder.tvDate.setText(expense.getDate());
        holder.tvAmount.setText(String.format("¥%.2f", expense.getAmount()));
        holder.tvCategory.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, colorForCategory(expense.getCategory()))));
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(expense, holder.getAdapterPosition());
        });
    }

    private int colorForCategory(String category) {
        if ("餐饮".equals(category)) return R.color.cat_food;
        if ("交通".equals(category)) return R.color.cat_traffic;
        if ("住宿".equals(category)) return R.color.cat_hotel;
        if ("门票".equals(category)) return R.color.cat_ticket;
        if ("购物".equals(category)) return R.color.cat_shopping;
        return R.color.cat_other;
    }

    @Override
    public int getItemCount() {
        return expenses == null ? 0 : expenses.size();
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvNote, tvDate, tvAmount;
        ImageButton btnDelete;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_expense_category_tag);
            tvNote = itemView.findViewById(R.id.tv_expense_note);
            tvDate = itemView.findViewById(R.id.tv_expense_date);
            tvAmount = itemView.findViewById(R.id.tv_expense_amount);
            btnDelete = itemView.findViewById(R.id.btn_delete_expense);
        }
    }
}
