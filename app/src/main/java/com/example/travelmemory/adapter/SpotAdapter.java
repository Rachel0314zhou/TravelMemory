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
        holder.tvNote.setText(spot.getNote().isEmpty() ? "暂无备注" : spot.getNote());

        // Update star icon representation
        if (spot.getIsFavorite() == 1) {
            holder.btnStar.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.btnStar.setImageResource(android.R.drawable.btn_star_big_off);
        }

        // Setup actions
        holder.btnStar.setOnClickListener(v -> {
            if (listener != null) {
                int nextState = spot.getIsFavorite() == 1 ? 0 : 1;
                listener.onToggleFavorite(spot, holder.getAdapterPosition(), nextState);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(spot, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return spots.size();
    }

    public static class SpotViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvName, tvNote;
        ImageButton btnStar;
        ImageButton btnDelete;

        public SpotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_spot_type_tag);
            tvName = itemView.findViewById(R.id.tv_spot_name);
            tvNote = itemView.findViewById(R.id.tv_spot_note);
            btnStar = itemView.findViewById(R.id.btn_spot_toggle_favorite);
            btnDelete = itemView.findViewById(R.id.btn_delete_spot);
        }
    }
}
