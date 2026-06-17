package com.example.travelmemory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.activity.MemoryCardActivity;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.DateUtils;
import com.example.travelmemory.utils.ImageUtils;
import com.example.travelmemory.utils.TripNavigator;

import java.util.List;

public class MemoryTripAdapter extends RecyclerView.Adapter<MemoryTripAdapter.MemoryTripViewHolder> {

    private final Context context;
    private final List<Trip> trips;

    public MemoryTripAdapter(Context context, List<Trip> trips) {
        this.context = context;
        this.trips = trips;
    }

    @NonNull
    @Override
    public MemoryTripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);
        return new MemoryTripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoryTripViewHolder holder, int position) {
        Trip trip = trips.get(position);
        holder.tvName.setText(trip.getName());
        holder.tvDestination.setText("\u76ee\u7684\u5730\uff1a" + trip.getDestination());
        holder.tvDates.setText(trip.getStartDate() + " ~ " + trip.getEndDate());
        long days = DateUtils.getDaysBetween(trip.getStartDate(), trip.getEndDate());
        holder.tvDays.setText(days + " \u5929");
        ImageUtils.displayLocalImage(holder.ivCover, trip.getCoverPath(), R.drawable.travel_placeholder);
        holder.itemView.setOnClickListener(v ->
                TripNavigator.openFeature(context, MemoryCardActivity.class, trip.getId()));
    }

    @Override
    public int getItemCount() {
        return trips == null ? 0 : trips.size();
    }

    static class MemoryTripViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvName;
        TextView tvDestination;
        TextView tvDates;
        TextView tvDays;

        MemoryTripViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_trip_cover);
            tvName = itemView.findViewById(R.id.tv_trip_name);
            tvDestination = itemView.findViewById(R.id.tv_trip_destination);
            tvDates = itemView.findViewById(R.id.tv_trip_dates);
            tvDays = itemView.findViewById(R.id.tv_trip_days);
        }
    }
}
