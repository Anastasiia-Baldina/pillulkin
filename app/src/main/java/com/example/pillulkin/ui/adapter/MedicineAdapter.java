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
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;

public class MedicineAdapter extends ListAdapter<PatientMedicineResponse, MedicineAdapter.MedicineViewHolder> {
    private final OnMedicineClickListener listener;

    public interface OnMedicineClickListener {
        void onMedicineClick(PatientMedicineResponse medicine);
    }

    public MedicineAdapter(OnMedicineClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<PatientMedicineResponse> DIFF_CALLBACK = new DiffUtil.ItemCallback<PatientMedicineResponse>() {
        @Override
        public boolean areItemsTheSame(@NonNull PatientMedicineResponse oldItem, @NonNull PatientMedicineResponse newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull PatientMedicineResponse oldItem, @NonNull PatientMedicineResponse newItem) {
            return oldItem.getMedicineName().equals(newItem.getMedicineName()) &&
                   oldItem.getDosage().equals(newItem.getDosage());
        }
    };

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class MedicineViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvDosage;
        private final TextView tvExpiration;
        private final TextView tvStatus;

        MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedicineName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvExpiration = itemView.findViewById(R.id.tvExpiration);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void bind(PatientMedicineResponse medicine) {
            tvName.setText(medicine.getMedicineName());
            String dosageText = medicine.getDosage();
            if (medicine.getForm() != null && !medicine.getForm().isEmpty()) {
                dosageText += " \u2022 " + medicine.getForm();
            }
            tvDosage.setText(dosageText);

            if (medicine.getExpirationDate() != null && !medicine.getExpirationDate().isEmpty()) {
                tvExpiration.setText(itemView.getContext().getString(R.string.medicine_expiration) + ": " + medicine.getExpirationDate());
                tvExpiration.setVisibility(View.VISIBLE);
            } else {
                tvExpiration.setVisibility(View.GONE);
            }

            if (medicine.getQuantity() != null && !medicine.getQuantity().isEmpty()) {
                tvStatus.setText(medicine.getQuantity());
                tvStatus.setVisibility(View.VISIBLE);
            } else {
                tvStatus.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onMedicineClick(medicine));
        }
    }
}
