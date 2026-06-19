package com.example.travelmemory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.model.PrePlan;

import java.util.List;

public class PrePlanAdapter extends RecyclerView.Adapter<PrePlanAdapter.PrePlanViewHolder> {

    public interface Listener {
        void onToggle(PrePlan plan, boolean completed);
        void onEdit(PrePlan plan);
        void onDelete(PrePlan plan);
    }

    private final Context context;
    private final List<PrePlan> plans;
    private final Listener listener;

    public PrePlanAdapter(Context context, List<PrePlan> plans, Listener listener) {
        this.context = context;
        this.plans = plans;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PrePlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pre_plan, parent, false);
        return new PrePlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrePlanViewHolder holder, int position) {
        PrePlan plan = plans.get(position);
        boolean completed = plan.getIsCompleted() == 1;
        holder.cbCompleted.setOnCheckedChangeListener(null);
        holder.cbCompleted.setChecked(completed);
        holder.tvTitle.setText(plan.getTitle());
        holder.tvStatus.setText(completed ? "已完成" : "未完成");
        holder.tvType.setText(plan.getType());
        holder.tvLocation.setText(isBlank(plan.getLocation()) ? "地点：未填写" : "地点：" + plan.getLocation());
        holder.tvDate.setText(isBlank(plan.getTargetDate()) ? "计划日期：未设置" : "计划日期：" + plan.getTargetDate());
        holder.tvNote.setText(isBlank(plan.getNote()) ? "备注：暂无" : "备注：" + plan.getNote());
        holder.btnToggle.setText(completed ? "设为未完成" : "完成");
        holder.cbCompleted.setText("");
        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onToggle(plan, isChecked));
        holder.tvTime.setText(isBlank(plan.getTimeText()) ? "时间：未设置" : "时间：" + plan.getTimeText());
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(plan));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(plan));
    }

    @Override
    public int getItemCount() {
        return plans == null ? 0 : plans.size();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static class PrePlanViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvStatus;
        TextView tvType;
        TextView tvLocation;
        TextView tvNote;
        TextView tvDate;
        TextView tvTime;
        CheckBox cbCompleted;
        Button btnToggle;
        Button btnEdit;
        Button btnDelete;

        PrePlanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_pre_plan_title);
            tvStatus = itemView.findViewById(R.id.tv_pre_plan_status);
            tvType = itemView.findViewById(R.id.tv_pre_plan_type);
            tvLocation = itemView.findViewById(R.id.tv_pre_plan_location);
            tvNote = itemView.findViewById(R.id.tv_pre_plan_note);
            tvDate = itemView.findViewById(R.id.tv_pre_plan_date);
            tvTime = itemView.findViewById(R.id.tv_pre_plan_time);
            cbCompleted = itemView.findViewById(R.id.cb_pre_plan_completed);
            btnToggle = cbCompleted;
            btnEdit = itemView.findViewById(R.id.btn_pre_plan_edit);
            btnDelete = itemView.findViewById(R.id.btn_pre_plan_delete);
        }
    }
}
