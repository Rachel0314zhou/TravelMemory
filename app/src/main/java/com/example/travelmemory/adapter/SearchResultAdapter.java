package com.example.travelmemory.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.activity.TripDetailActivity;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.DateUtils;
import com.example.travelmemory.utils.ImageUtils;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {

    private final Context context;
    private final List<Trip> results;

    public SearchResultAdapter(Context context, List<Trip> results) {
        this.context = context;
        this.results = results;
    }

    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        return new SearchResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        Trip trip = results.get(position);

        holder.tvName.setText(trip.getName());
        long days = DateUtils.getDaysBetween(trip.getStartDate(), trip.getEndDate());
        holder.tvDestDays.setText("📍 " + trip.getDestination() + " · " + days + " 天");
        holder.tvDates.setText(trip.getStartDate() + " ~ " + trip.getEndDate());

        ImageUtils.displayLocalImage(holder.ivThumb, trip.getCoverPath(), R.drawable.travel_placeholder);

        // Click navigates to Trip details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TripDetailActivity.class);
            intent.putExtra("trip_id", trip.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class SearchResultViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvName, tvDestDays, tvDates;

        public SearchResultViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_search_thumb);
            tvName = itemView.findViewById(R.id.tv_search_name);
            tvDestDays = itemView.findViewById(R.id.tv_search_dest_days);
            tvDates = itemView.findViewById(R.id.tv_search_dates);
        }
    }
}
