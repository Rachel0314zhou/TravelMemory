package com.example.travelmemory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;
import com.example.travelmemory.model.PackingItem;

import java.util.List;

public class PackingListAdapter extends RecyclerView.Adapter<PackingListAdapter.PackingViewHolder> {

    public static final int MODE_SELECT = 1;
    public static final int MODE_PACK = 2;

    public interface Listener {
        void onItemStateChanged(PackingItem item, boolean checked);
    }

    private final Context context;
    private final List<PackingItem> items;
    private final Listener listener;
    private final int mode;

    public PackingListAdapter(Context context, List<PackingItem> items, Listener listener, int mode) {
        this.context = context;
        this.items = items;
        this.listener = listener;
        this.mode = mode;
    }

    @NonNull
    @Override
    public PackingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_packing_item, parent, false);
        return new PackingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackingViewHolder holder, int position) {
        PackingItem item = items.get(position);
        boolean checked = mode == MODE_SELECT ? item.getIsSelected() == 1 : item.getIsPacked() == 1;

        holder.tvName.setText(item.getItemName());
        holder.tvCategory.setText(item.getCategory());
        holder.cbChecked.setText(mode == MODE_SELECT ? "需要携带" : "已打包");
        holder.cbChecked.setOnCheckedChangeListener(null);
        holder.cbChecked.setChecked(checked);
        holder.cbChecked.setOnCheckedChangeListener((buttonView, isChecked) -> listener.onItemStateChanged(item, isChecked));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class PackingViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbChecked;
        TextView tvName;
        TextView tvCategory;

        PackingViewHolder(@NonNull View itemView) {
            super(itemView);
            cbChecked = itemView.findViewById(R.id.cb_packing_checked);
            tvName = itemView.findViewById(R.id.tv_packing_name);
            tvCategory = itemView.findViewById(R.id.tv_packing_category);
        }
    }
}
