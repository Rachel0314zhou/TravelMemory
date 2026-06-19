package com.example.travelmemory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.model.Spot;

import java.util.List;

public class SpotAdapter extends RecyclerView.Adapter<SpotAdapter.SpotViewHolder> {

    public interface OnSpotActionListener {
        void onDelete(Spot spot, int position);
        void onToggleFavorite(Spot spot, int position, int nextState);
    }

    private final Context context;
    private final List<Spot> spots;
    private final OnSpotActionListener listener;

    public SpotAdapter(Context context, List<Spot> spots, OnSpotActionListener listener) {
        this.context = context;
        this.spots = spots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SpotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_spot, parent, false);
        return new SpotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpotViewHolder holder, int position) {
        Spot spot = spots.get(position);
        holder.tvType.setText(spot.getType());
        holder.tvName.setText(spot.getName());
        String note = spot.getNote();
        holder.tvNote.setText(note == null || note.isEmpty() ? "暂无备注" : note);
        holder.tvSource.setText(spot.getSourcePlanId() > 0 ? "来自行前计划" : "计划外新增");
        holder.btnStar.setImageResource(spot.getIsFavorite() == 1 ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        holder.btnStar.setOnClickListener(v -> {
            if (listener != null) listener.onToggleFavorite(spot, holder.getAdapterPosition(), spot.getIsFavorite() == 1 ? 0 : 1);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(spot, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return spots == null ? 0 : spots.size();
    }

    public static class SpotViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvName, tvNote, tvSource;
        ImageButton btnStar, btnDelete;

        public SpotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_spot_type_tag);
            tvName = itemView.findViewById(R.id.tv_spot_name);
            tvNote = itemView.findViewById(R.id.tv_spot_note);
            tvSource = itemView.findViewById(R.id.tv_spot_source);
            btnStar = itemView.findViewById(R.id.btn_spot_toggle_favorite);
            btnDelete = itemView.findViewById(R.id.btn_delete_spot);
        }
    }
}
