package com.example.travelmemory.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.R;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingButtonViewHolder> {

    public static final String ACTION_EDIT_PROFILE = "edit_profile";
    public static final String ACTION_EDIT_NICKNAME = "edit_nickname";
    public static final String ACTION_CHANGE_PASSWORD = "change_password";

    public interface Listener {
        void onSettingClicked(String action);
    }

    private final Listener listener;
    private final SettingButton[] buttons = {
            new SettingButton("我的旅行画像", ACTION_EDIT_PROFILE),
            new SettingButton("修改昵称", ACTION_EDIT_NICKNAME),
            new SettingButton("修改密码", ACTION_CHANGE_PASSWORD)
    };

    public SettingsAdapter(Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SettingButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_setting_button, parent, false);
        return new SettingButtonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingButtonViewHolder holder, int position) {
        SettingButton button = buttons[position];
        holder.tvTitle.setText(button.title);
        holder.itemView.setOnClickListener(v -> listener.onSettingClicked(button.action));
    }

    @Override
    public int getItemCount() {
        return buttons.length;
    }

    static class SettingButtonViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        SettingButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_setting_button_title);
        }
    }

    private static class SettingButton {
        final String title;
        final String action;

        SettingButton(String title, String action) {
            this.title = title;
            this.action = action;
        }
    }
}
