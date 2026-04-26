package com.example.pillulkin.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pillulkin.R;
import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;

public class ReferenceMedicineAdapter extends ListAdapter<ReferenceMedicineResponse, ReferenceMedicineAdapter.ViewHolder> {
    private final OnMedicineClickListener listener;

    public interface OnMedicineClickListener {
        void onMedicineClick(ReferenceMedicineResponse medicine);
    }

    public ReferenceMedicineAdapter(OnMedicineClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ReferenceMedicineResponse> DIFF_CALLBACK = new DiffUtil.ItemCallback<ReferenceMedicineResponse>() {
        @Override
        public boolean areItemsTheSame(@NonNull ReferenceMedicineResponse oldItem, @NonNull ReferenceMedicineResponse newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ReferenceMedicineResponse oldItem, @NonNull ReferenceMedicineResponse newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getDosage().equals(newItem.getDosage());
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
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
            String dosageText = medicine.getDosage();
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
