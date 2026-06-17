package com.example.travelmemory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.model.DayRecord;
import com.example.travelmemory.model.Photo;
import com.example.travelmemory.utils.ImageUtils;

import java.util.List;

public class TripRecapAdapter extends RecyclerView.Adapter<TripRecapAdapter.RecapViewHolder> {

    private final Context context;
    private final List<DayRecord> dayRecords;
    private final List<Photo> photos;
    private final int[] pagePhotoIndexes;

    public TripRecapAdapter(Context context, List<DayRecord> dayRecords, List<Photo> photos) {
        this.context = context;
        this.dayRecords = dayRecords;
        this.photos = photos;
        this.pagePhotoIndexes = new int[getPageCount(dayRecords, photos)];
        int photoCount = photos == null ? 0 : photos.size();
        for (int i = 0; i < pagePhotoIndexes.length; i++) {
            pagePhotoIndexes[i] = photoCount == 0 ? 0 : i % photoCount;
        }
    }

    @NonNull
    @Override
    public RecapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recap_page, parent, false);
        return new RecapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecapViewHolder holder, int position) {
        DayRecord record = hasDayRecords() ? dayRecords.get(position) : null;
        bindPhoto(holder, position);

        holder.tvMemory.setText(memoryText(record));
        holder.tvMood.setText(moodText(record));
        holder.tvPageIndex.setText((position + 1) + " / " + getItemCount());
    }

    @Override
    public int getItemCount() {
        return pagePhotoIndexes.length;
    }

    private boolean hasDayRecords() {
        return dayRecords != null && !dayRecords.isEmpty();
    }

    private void bindPhoto(RecapViewHolder holder, int position) {
        Photo photo = photoFor(position);
        if (photo == null) {
            holder.ivPhoto.setImageResource(R.drawable.travel_placeholder);
            holder.layoutPhotoControls.setVisibility(View.GONE);
            return;
        }

        holder.layoutPhotoControls.setVisibility(View.VISIBLE);
        ImageUtils.displayLocalImage(holder.ivPhoto, photo.getImagePath(), R.drawable.travel_placeholder);
        holder.tvPhotoIndex.setText((pagePhotoIndexes[position] + 1) + " / " + photos.size());
        boolean hasMultiplePhotos = photos.size() > 1;
        holder.btnPrevPhoto.setEnabled(hasMultiplePhotos);
        holder.btnNextPhoto.setEnabled(hasMultiplePhotos);
        holder.btnPrevPhoto.setAlpha(hasMultiplePhotos ? 1f : 0.45f);
        holder.btnNextPhoto.setAlpha(hasMultiplePhotos ? 1f : 0.45f);
        holder.btnPrevPhoto.setOnClickListener(v -> shiftPhoto(holder, position, -1));
        holder.btnNextPhoto.setOnClickListener(v -> shiftPhoto(holder, position, 1));
    }

    private void shiftPhoto(RecapViewHolder holder, int position, int delta) {
        if (photos == null || photos.size() <= 1) return;
        int nextIndex = pagePhotoIndexes[position] + delta;
        if (nextIndex < 0) nextIndex = photos.size() - 1;
        if (nextIndex >= photos.size()) nextIndex = 0;
        pagePhotoIndexes[position] = nextIndex;
        bindPhoto(holder, position);
    }

    private Photo photoFor(int position) {
        if (photos == null || photos.isEmpty()) return null;
        return photos.get(pagePhotoIndexes[position] % photos.size());
    }

    private int getPageCount(List<DayRecord> dayRecords, List<Photo> photos) {
        if (dayRecords != null && !dayRecords.isEmpty()) return dayRecords.size();
        if (photos != null && !photos.isEmpty()) return photos.size();
        return 1;
    }

    private String memoryText(DayRecord record) {
        if (record == null || isBlank(record.getNote())) {
            return "这趟旅行还没有文字回忆，先从照片开始回顾。";
        }
        return record.getNote();
    }

    private String moodText(DayRecord record) {
        if (record == null || isBlank(record.getMood())) {
            return "心情 · 待补充";
        }
        return "心情 · " + record.getMood();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static class RecapViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;
        LinearLayout layoutPhotoControls;
        TextView btnPrevPhoto;
        TextView btnNextPhoto;
        TextView tvPhotoIndex;
        TextView tvMemory;
        TextView tvMood;
        TextView tvPageIndex;

        RecapViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_recap_photo);
            layoutPhotoControls = itemView.findViewById(R.id.layout_recap_photo_controls);
            btnPrevPhoto = itemView.findViewById(R.id.btn_recap_prev_photo);
            btnNextPhoto = itemView.findViewById(R.id.btn_recap_next_photo);
            tvPhotoIndex = itemView.findViewById(R.id.tv_recap_photo_index);
            tvMemory = itemView.findViewById(R.id.tv_recap_memory);
            tvMood = itemView.findViewById(R.id.tv_recap_mood);
            tvPageIndex = itemView.findViewById(R.id.tv_recap_page_index);
        }
    }
}
