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
import com.example.travelmemory.model.Photo;
import com.example.travelmemory.model.SearchResult;
import com.example.travelmemory.utils.ImageUtils;
import com.example.travelmemory.utils.TripNavigator;
import com.example.travelmemory.utils.TravelConstants;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {

    private final Context context;
    private final List<SearchResult> results;
    private final DatabaseHelper dbHelper;

    public SearchResultAdapter(Context context, List<SearchResult> results) {
        this.context = context;
        this.results = results;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        return new SearchResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        SearchResult result = results.get(position);
        holder.tvName.setText(result.getTitle());
        holder.tvSourceType.setText(result.getSourceType());
        holder.tvDestDays.setText(result.getSubtitle());
        holder.tvDates.setText(result.getDateText());
        ImageUtils.displayLocalImage(holder.ivThumb, firstPhotoPath(result), R.drawable.travel_placeholder);
        holder.itemView.setOnClickListener(v ->
                TripNavigator.openTripDetail(context, result.getTripId(), TravelConstants.MODE_OVERVIEW));
    }

    @Override
    public int getItemCount() {
        return results == null ? 0 : results.size();
    }

    private String firstPhotoPath(SearchResult result) {
        List<Photo> photos = dbHelper.getPhotosForTrip(result.getTripId());
        if (photos == null || photos.isEmpty()) return "";
        return photos.get(0).getImagePath();
    }

    public static class SearchResultViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvName, tvSourceType, tvDestDays, tvDates;

        public SearchResultViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_search_thumb);
            tvName = itemView.findViewById(R.id.tv_search_name);
            tvSourceType = itemView.findViewById(R.id.tv_search_source_type);
            tvDestDays = itemView.findViewById(R.id.tv_search_dest_days);
            tvDates = itemView.findViewById(R.id.tv_search_dates);
        }
    }
}
