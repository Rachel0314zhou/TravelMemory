package com.example.travelmemory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.model.RegretItem;

import java.util.List;

public class RegretAdapter extends RecyclerView.Adapter<RegretAdapter.RegretViewHolder> {

    private final Context context;
    private final List<RegretItem> regrets;

    public RegretAdapter(Context context, List<RegretItem> regrets) {
        this.context = context;
        this.regrets = regrets;
    }

    @NonNull
    @Override
    public RegretViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_regret, parent, false);
        return new RegretViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegretViewHolder holder, int position) {
        RegretItem item = regrets.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvType.setText(item.getType());
        holder.tvReason.setText(item.getReason());
    }

    @Override
    public int getItemCount() {
        return regrets == null ? 0 : regrets.size();
    }

    static class RegretViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvType;
        TextView tvReason;

        RegretViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_regret_title);
            tvType = itemView.findViewById(R.id.tv_regret_type);
            tvReason = itemView.findViewById(R.id.tv_regret_reason);
        }
    }
}
