package com.example.travelmemory.adapter;

import android.content.Context;
import android.content.Intent;
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

        // Select timeline dot color and mood badge tint depending on mood
        int moodColorRes = R.color.mood_happy;
        String m = record.getMood();
        if (m != null) {
            if (m.contains("平静") || m.contains("😐")) {
                moodColorRes = R.color.mood_calm;
            } else if (m.contains("疲惫") || m.contains("😴")) {
                moodColorRes = R.color.mood_tired;
            } else if (m.contains("兴奋") || m.contains("🤩")) {
                moodColorRes = R.color.mood_excited;
            } else if (m.contains("感动") || m.contains("🥹")) {
                moodColorRes = R.color.mood_touched;
            } else if (m.contains("遗憾") || m.contains("😔")) {
                moodColorRes = R.color.mood_regret;
            }
        }

        int col = ContextCompat.getColor(context, moodColorRes);
        holder.tvMood.setBackgroundTintList(ColorStateList.valueOf(col));
        holder.timelineDot.setBackgroundTintList(ColorStateList.valueOf(col));

        // Click to edit day record
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DayRecordActivity.class);
            intent.putExtra("trip_id", record.getTripId());
            intent.putExtra("record_date", record.getDate());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
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
