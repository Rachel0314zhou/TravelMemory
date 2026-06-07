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

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private final Context context;
    private final List<Trip> tripList;

    public TripAdapter(Context context, List<Trip> tripList) {
        this.context = context;
        this.tripList = tripList;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = tripList.get(position);

        holder.tvName.setText(trip.getName());
        holder.tvDestination.setText("📍 " + trip.getDestination());
        holder.tvDates.setText(trip.getStartDate() + " ~ " + trip.getEndDate());

        // Days counts
        long days = DateUtils.getDaysBetween(trip.getStartDate(), trip.getEndDate());
        holder.tvDays.setText(days + " 天");

        // Cover Loading
        ImageUtils.displayLocalImage(holder.ivTripCover, trip.getCoverPath(), R.drawable.travel_placeholder);

        // Click navigation to Detail activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TripDetailActivity.class);
            intent.putExtra("trip_id", trip.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return tripList.size();
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTripCover;
        TextView tvName, tvDestination, tvDates, tvDays;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTripCover = itemView.findViewById(R.id.iv_trip_cover);
            tvName = itemView.findViewById(R.id.tv_trip_name);
            tvDestination = itemView.findViewById(R.id.tv_trip_destination);
            tvDates = itemView.findViewById(R.id.tv_trip_dates);
            tvDays = itemView.findViewById(R.id.tv_trip_days);
        }
    }
}
