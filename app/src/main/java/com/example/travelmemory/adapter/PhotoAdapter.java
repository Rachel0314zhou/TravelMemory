package com.example.travelmemory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.model.Photo;
import com.example.travelmemory.utils.ImageUtils;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    public interface OnPhotoDeleteListener {
        void onDelete(Photo photo, int position);
    }

    public interface OnPhotoClickListener {
        void onClick(Photo photo);
    }

    private final Context context;
    private final List<Photo> photos;
    private final OnPhotoDeleteListener deleteListener;
    private final OnPhotoClickListener clickListener;

    public PhotoAdapter(Context context, List<Photo> photos, OnPhotoDeleteListener deleteListener, OnPhotoClickListener clickListener) {
        this.context = context;
        this.photos = photos;
        this.deleteListener = deleteListener;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photos.get(position);
        ImageUtils.displayLocalImage(holder.ivThumbnail, photo.getImagePath(), R.drawable.travel_placeholder);
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDelete(photo, holder.getAdapterPosition());
        });
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(photo);
        });
    }

    @Override
    public int getItemCount() {
        return photos == null ? 0 : photos.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        ImageButton btnDelete;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            btnDelete = itemView.findViewById(R.id.btn_delete_photo);
        }
    }
}
