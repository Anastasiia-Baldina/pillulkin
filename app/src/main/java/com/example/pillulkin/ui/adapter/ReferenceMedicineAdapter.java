package com.example.pillulkin.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pillulkin.R;
import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;

import java.util.ArrayList;
import java.util.List;

public class ReferenceMedicineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ADD_CUSTOM = 0;
    private static final int TYPE_MEDICINE = 1;

    private final OnMedicineClickListener listener;
    private final OnCustomClickListener customListener;
    private boolean customButtonVisible = true;
    private List<ReferenceMedicineResponse> items = new ArrayList<>();

    public interface OnMedicineClickListener {
        void onMedicineClick(ReferenceMedicineResponse medicine);
    }

    public interface OnCustomClickListener {
        void onCustomClick();
    }

    public ReferenceMedicineAdapter(OnMedicineClickListener listener, OnCustomClickListener customListener) {
        this.listener = listener;
        this.customListener = customListener;
    }

    public ReferenceMedicineAdapter(OnMedicineClickListener listener) {
        this(listener, null);
    }

    public void submitList(List<ReferenceMedicineResponse> list) {
        if (list == null) list = new ArrayList<>();
        this.items = list;
        notifyDataSetChanged();
    }

    public void setCustomButtonVisible(boolean visible) {
        if (visible && customListener == null) return;
        this.customButtonVisible = visible;
        notifyDataSetChanged();
    }

    private boolean hasCustomButton() {
        return customListener != null && customButtonVisible;
    }

    @Override
    public int getItemViewType(int position) {
        if (hasCustomButton() && position == 0) {
            return TYPE_ADD_CUSTOM;
        }
        return TYPE_MEDICINE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD_CUSTOM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_custom, parent, false);
            return new AddCustomViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AddCustomViewHolder) {
            holder.itemView.setOnClickListener(v -> {
                if (customListener != null) {
                    customListener.onCustomClick();
                }
            });
        } else {
            int listPosition = hasCustomButton() ? position - 1 : position;
            ((ViewHolder) holder).bind(items.get(listPosition));
        }
    }

    @Override
    public int getItemCount() {
        int count = items.size();
        if (hasCustomButton()) {
            return count + 1;
        }
        return count;
    }

    static class AddCustomViewHolder extends RecyclerView.ViewHolder {
        AddCustomViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMedicineName;
        private final TextView tvDosage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
        }

        void bind(ReferenceMedicineResponse medicine) {
            tvMedicineName.setText(medicine.getName());
            String dosageText = medicine.getDosage() != null ? medicine.getDosage() : "";
            if (medicine.getForm() != null && !medicine.getForm().isEmpty()) {
                dosageText += " \u2022 " + medicine.getForm();
            }
            tvDosage.setText(dosageText);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMedicineClick(medicine);
                }
            });
        }
    }
}