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
import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.PackingItem;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.DateUtils;
import com.example.travelmemory.utils.ImageUtils;
import com.example.travelmemory.utils.TripNavigator;
import com.example.travelmemory.utils.TravelConstants;

import java.util.ArrayList;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private final Context context;
    private final List<Trip> tripList;
    private final DatabaseHelper dbHelper;
    private final OnTripClickListener listener;

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
    }

    public TripAdapter(Context context, List<Trip> tripList) {
        this(context, tripList, null);
    }

    public TripAdapter(Context context, List<Trip> tripList, OnTripClickListener listener) {
        this.context = context;
        this.tripList = tripList;
        this.dbHelper = new DatabaseHelper(context);
        this.listener = listener;
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
        holder.tvDestination.setText("目的地：" + trip.getDestination());
        holder.tvDates.setText(trip.getStartDate() + " ~ " + trip.getEndDate());
        long days = DateUtils.getDaysBetween(trip.getStartDate(), trip.getEndDate());
        holder.tvDays.setText(days + " 天");
        holder.tvStatus.setText("当前阶段：" + TravelConstants.formatTripStatusText(trip.getStatus()));
        bindPackingHint(holder, trip);
        bindPlanHint(holder, trip);
        ImageUtils.displayLocalImage(holder.ivTripCover, trip.getCoverPath(), R.drawable.travel_placeholder);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTripClick(trip);
                return;
            }
            TripNavigator.openTripDetail(context, trip.getId(), TravelConstants.MODE_OVERVIEW);
        });
    }

    @Override
    public int getItemCount() {
        return tripList == null ? 0 : tripList.size();
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTripCover;
        TextView tvName, tvDestination, tvDates, tvDays, tvStatus, tvPackingHint, tvPlanHint;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTripCover = itemView.findViewById(R.id.iv_trip_cover);
            tvName = itemView.findViewById(R.id.tv_trip_name);
            tvDestination = itemView.findViewById(R.id.tv_trip_destination);
            tvDates = itemView.findViewById(R.id.tv_trip_dates);
            tvDays = itemView.findViewById(R.id.tv_trip_days);
            tvStatus = itemView.findViewById(R.id.tv_trip_status);
            tvPackingHint = itemView.findViewById(R.id.tv_trip_packing_hint);
            tvPlanHint = itemView.findViewById(R.id.tv_trip_plan_hint);
        }
    }

    private void bindPackingHint(TripViewHolder holder, Trip trip) {
        if (!TravelConstants.isPlannedTrip(trip.getMode())) {
            holder.tvPackingHint.setVisibility(View.GONE);
            return;
        }
        List<PackingItem> items = dbHelper.getPackingItemsForTrip(trip.getId());
        List<String> unpacked = new ArrayList<>();
        for (PackingItem item : items) {
            if (item.getIsPacked() != 1) unpacked.add(item.getItemName());
        }
        holder.tvPackingHint.setVisibility(View.VISIBLE);
        if (unpacked.isEmpty() && !items.isEmpty()) {
            holder.tvPackingHint.setText("行李已准备完成");
        } else if (unpacked.isEmpty()) {
            holder.tvPackingHint.setText("还没有行李清单");
        } else {
            holder.tvPackingHint.setText("未打包：" + preview(unpacked) + "等 " + unpacked.size() + " 件");
        }
    }

    private void bindPlanHint(TripViewHolder holder, Trip trip) {
        if (!TravelConstants.isPlannedTrip(trip.getMode())) {
            holder.tvPlanHint.setVisibility(View.GONE);
            return;
        }
        int unfinished = dbHelper.countPrePlansForTrip(trip.getId()) - dbHelper.countCompletedPrePlansForTrip(trip.getId());
        if (unfinished > 0) {
            holder.tvPlanHint.setVisibility(View.VISIBLE);
            holder.tvPlanHint.setText("还有 " + unfinished + " 个计划未完成");
        } else {
            holder.tvPlanHint.setVisibility(View.GONE);
        }
    }

    private String preview(List<String> values) {
        StringBuilder builder = new StringBuilder();
        int limit = Math.min(2, values.size());
        for (int i = 0; i < limit; i++) {
            if (i > 0) builder.append("、");
            builder.append(values.get(i));
        }
        return builder.toString();
    }
}
