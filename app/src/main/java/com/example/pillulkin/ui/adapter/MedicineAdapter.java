package com.example.pillulkin.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pillulkin.R;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.google.android.material.card.MaterialCardView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
                   oldItem.getDosage().equals(newItem.getDosage()) &&
                   equalsOrBothNull(oldItem.getExpirationDate(), newItem.getExpirationDate()) &&
                   equalsOrBothNull(oldItem.getQuantity(), newItem.getQuantity());
        }

        private boolean equalsOrBothNull(String a, String b) {
            if (a == null && b == null) return true;
            if (a == null || b == null) return false;
            return a.equals(b);
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
        private final MaterialCardView cardView;
        private final TextView tvName;
        private final TextView tvDosage;
        private final TextView tvExpiration;
        private final TextView tvQuantity;
        private final TextView tvStatus;

        MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvName = itemView.findViewById(R.id.tvMedicineName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvExpiration = itemView.findViewById(R.id.tvExpiration);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void bind(PatientMedicineResponse medicine) {
            tvName.setText(medicine.getMedicineName());
            String dosageText = medicine.getDosage();
            if (medicine.getForm() != null && !medicine.getForm().isEmpty()) {
                dosageText += " \u2022 " + medicine.getForm();
            }
            tvDosage.setText(dosageText);

            int defaultCardColor = ContextCompat.getColor(itemView.getContext(), R.color.card_background);
            int defaultTextColor = ContextCompat.getColor(itemView.getContext(), R.color.on_background);
            int defaultSubtextColor = ContextCompat.getColor(itemView.getContext(), R.color.hint);
            int expiredColor = ContextCompat.getColor(itemView.getContext(), R.color.expired);
            int warningColor = ContextCompat.getColor(itemView.getContext(), R.color.expiration_warning);
            int warningTextColor = ContextCompat.getColor(itemView.getContext(), R.color.warning_text);
            int expiredTextColor = ContextCompat.getColor(itemView.getContext(), R.color.expired_text);

            ExpirationStatus status = evaluateStatus(medicine.getExpirationDate());

            switch (status) {
                case EXPIRED:
                    cardView.setCardBackgroundColor(expiredColor);
                    tvName.setTextColor(expiredTextColor);
                    tvDosage.setTextColor(expiredTextColor);
                    tvExpiration.setTextColor(expiredTextColor);
                    tvQuantity.setTextColor(expiredTextColor);
                    tvStatus.setText(R.string.medicine_expired_label);
                    tvStatus.setVisibility(View.VISIBLE);
                    break;
                case EXPIRING_SOON:
                    cardView.setCardBackgroundColor(warningColor);
                    tvName.setTextColor(warningTextColor);
                    tvDosage.setTextColor(warningTextColor);
                    tvExpiration.setTextColor(warningTextColor);
                    tvQuantity.setTextColor(warningTextColor);
                    tvStatus.setText(R.string.medicine_status_expiring_soon);
                    tvStatus.setVisibility(View.VISIBLE);
                    break;
                default:
                    cardView.setCardBackgroundColor(defaultCardColor);
                    tvName.setTextColor(defaultTextColor);
                    tvDosage.setTextColor(defaultSubtextColor);
                    tvExpiration.setTextColor(defaultSubtextColor);
                    tvQuantity.setTextColor(defaultSubtextColor);
                    tvStatus.setVisibility(View.GONE);
                    break;
            }

            if (medicine.getExpirationDate() != null && !medicine.getExpirationDate().isEmpty()) {
                tvExpiration.setText(itemView.getContext().getString(R.string.medicine_expiration) + ": " + medicine.getExpirationDate());
                tvExpiration.setVisibility(View.VISIBLE);
            } else {
                tvExpiration.setVisibility(View.GONE);
            }

            if (medicine.getQuantity() != null && !medicine.getQuantity().isEmpty()) {
                tvQuantity.setText(medicine.getQuantity());
                tvQuantity.setVisibility(View.VISIBLE);
            } else {
                tvQuantity.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onMedicineClick(medicine));
        }

        private ExpirationStatus evaluateStatus(String dateStr) {
            if (dateStr == null || dateStr.isEmpty()) return ExpirationStatus.NORMAL;
            LocalDate expDate = parseExpirationDate(dateStr);
            if (expDate == null) return ExpirationStatus.NORMAL;
            LocalDate today = LocalDate.now();
            if (!expDate.isAfter(today)) return ExpirationStatus.EXPIRED;
            if (!expDate.isAfter(today.plusDays(30))) return ExpirationStatus.EXPIRING_SOON;
            return ExpirationStatus.NORMAL;
        }

        private LocalDate parseExpirationDate(String dateStr) {
            String[] formats = {"yyyy-MM-dd", "dd.MM.yyyy", "MM.yyyy", "yyyy-MM-dd'T'HH:mm:ss"};
            for (String fmt : formats) {
                try {
                    return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern(fmt));
                } catch (DateTimeParseException ignored) {
                }
            }
            try {
                return LocalDate.parse(dateStr.trim());
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }

    private enum ExpirationStatus {
        NORMAL, EXPIRING_SOON, EXPIRED
    }
}
