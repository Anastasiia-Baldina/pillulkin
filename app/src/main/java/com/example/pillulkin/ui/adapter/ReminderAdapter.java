package com.example.pillulkin.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pillulkin.R;
import com.example.pillulkin.data.local.entity.Reminder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    private final List<Reminder> items = new ArrayList<>();
    private final OnReminderActionListener listener;

    public interface OnReminderActionListener {
        void onToggle(Reminder reminder);
        void onDelete(Reminder reminder);
        void onClick(Reminder reminder);
    }

    public ReminderAdapter(OnReminderActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Reminder> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = items.get(position);
        holder.tvTime.setText(reminder.getTimeFormatted());
        holder.tvText.setText(reminder.getDisplayText());
        holder.switchEnabled.setOnCheckedChangeListener(null);
        holder.switchEnabled.setChecked(reminder.isEnabled());
        holder.switchEnabled.setOnCheckedChangeListener((btn, checked) -> {
            if (listener != null) listener.onToggle(reminder);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(reminder);
        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(reminder);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        TextView tvText;
        SwitchMaterial switchEnabled;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvText = itemView.findViewById(R.id.tvText);
            switchEnabled = itemView.findViewById(R.id.switchEnabled);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
