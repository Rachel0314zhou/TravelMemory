package com.example.travelmemory.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.activity.DayRecordActivity;
import com.example.travelmemory.model.DayRecord;
import com.example.travelmemory.utils.TripNavigator;

import java.util.List;

public class DayRecordAdapter extends RecyclerView.Adapter<DayRecordAdapter.DayRecordViewHolder> {

    private final Context context;
    private final List<DayRecord> records;

    public DayRecordAdapter(Context context, List<DayRecord> records) {
        this.context = context;
        this.records = records;
    }

    @NonNull
    @Override
    public DayRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_day_record, parent, false);
        return new DayRecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayRecordViewHolder holder, int position) {
        DayRecord record = records.get(position);
        holder.tvDate.setText(record.getDate());
        holder.tvMood.setText(record.getMood());
        holder.tvNote.setText(record.getNote());

        int col = ContextCompat.getColor(context, moodColor(record.getMood()));
        holder.tvMood.setBackgroundTintList(ColorStateList.valueOf(col));
        holder.timelineDot.setBackgroundTintList(ColorStateList.valueOf(col));

        holder.itemView.setOnClickListener(v ->
                TripNavigator.openWithTripAndDate(context, DayRecordActivity.class, record.getTripId(), record.getDate()));
    }

    private int moodColor(String mood) {
        if ("平静".equals(mood)) return R.color.mood_calm;
        if ("疲惫".equals(mood)) return R.color.mood_tired;
        if ("兴奋".equals(mood)) return R.color.mood_excited;
        if ("感动".equals(mood)) return R.color.mood_touched;
        if ("遗憾".equals(mood)) return R.color.mood_regret;
        return R.color.mood_happy;
    }

    @Override
    public int getItemCount() {
        return records == null ? 0 : records.size();
    }

    public static class DayRecordViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvMood, tvNote;
        View timelineDot;

        public DayRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_record_date);
            tvMood = itemView.findViewById(R.id.tv_record_mood);
            tvNote = itemView.findViewById(R.id.tv_record_note);
            timelineDot = itemView.findViewById(R.id.timeline_dot);
        }
    }
}
