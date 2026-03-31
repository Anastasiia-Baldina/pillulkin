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
import com.example.pillulkin.data.local.entity.MedicineEntity;
import com.example.pillulkin.utils.DateUtils;

public class MedicineAdapter extends ListAdapter<MedicineEntity, MedicineAdapter.MedicineViewHolder> {
    private final OnMedicineClickListener listener;
    private final StatusProvider statusProvider;

    public interface OnMedicineClickListener {
        void onMedicineClick(MedicineEntity medicine);
    }

    public interface StatusProvider {
        String getStatus(MedicineEntity medicine);
    }

    public MedicineAdapter(OnMedicineClickListener listener, StatusProvider statusProvider) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.statusProvider = statusProvider;
    }

    private static final DiffUtil.ItemCallback<MedicineEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<MedicineEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull MedicineEntity oldItem, @NonNull MedicineEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull MedicineEntity oldItem, @NonNull MedicineEntity newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getDosage().equals(newItem.getDosage()) &&
                   oldItem.getExpirationDate().equals(newItem.getExpirationDate());
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
        MedicineEntity medicine = getItem(position);
        holder.bind(medicine);
    }

    class MedicineViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvStatus;
        private final TextView tvDosage;
        private final TextView tvExpiration;

        MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedicineName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvExpiration = itemView.findViewById(R.id.tvExpiration);
        }

        void bind(MedicineEntity medicine) {
            tvName.setText(medicine.getName());
            tvDosage.setText(String.format("%s • %s", medicine.getDosage(), medicine.getForm()));

            String expirationText = itemView.getContext().getString(R.string.medicine_expiration) + ": " + medicine.getExpirationDate();
            tvExpiration.setText(expirationText);

            String status = statusProvider.getStatus(medicine);
            if ("EXPIRED".equals(status)) {
                tvStatus.setText(R.string.medicine_status_expired);
                tvStatus.setBackgroundResource(R.drawable.bg_status_badge);
                tvStatus.getBackground().setTint(itemView.getContext().getColor(R.color.expired));
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.on_error));
            } else if ("EXPIRING_SOON".equals(status)) {
                tvStatus.setText(R.string.medicine_status_expiring_soon);
                tvStatus.setBackgroundResource(R.drawable.bg_status_badge);
                tvStatus.getBackground().setTint(itemView.getContext().getColor(R.color.warning));
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.on_background));
            } else {
                tvStatus.setText(R.string.medicine_status_valid);
                tvStatus.setBackgroundResource(R.drawable.bg_status_badge);
                tvStatus.getBackground().setTint(itemView.getContext().getColor(R.color.valid));
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.on_background));
            }

            itemView.setOnClickListener(v -> listener.onMedicineClick(medicine));
        }
    }
}
