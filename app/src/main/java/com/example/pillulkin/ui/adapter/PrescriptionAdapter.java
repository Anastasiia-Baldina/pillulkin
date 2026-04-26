package com.example.pillulkin.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pillulkin.R;
import com.example.pillulkin.data.remote.model.PrescriptionResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionAdapter extends RecyclerView.Adapter<PrescriptionAdapter.ViewHolder> {

    private final List<PrescriptionResponse> items = new ArrayList<>();
    private final OnMoveToCabinetListener listener;

    public interface OnMoveToCabinetListener {
        void onMoveToCabinet(PrescriptionResponse prescription);
    }

    public PrescriptionAdapter(OnMoveToCabinetListener listener) {
        this.listener = listener;
    }

    public void submitList(List<PrescriptionResponse> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prescription, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMedicineName;
        private final TextView tvDosage;
        private final TextView tvInstructions;
        private final TextView tvDate;
        private final MaterialButton btnMove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvInstructions = itemView.findViewById(R.id.tvInstructions);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnMove = itemView.findViewById(R.id.btnMoveToCabinet);
        }

        void bind(PrescriptionResponse p) {
            tvMedicineName.setText(p.getMedicineName());
            String dosageText = p.getDosage();
            if (p.getForm() != null && !p.getForm().isEmpty()) {
                dosageText += " \u2022 " + p.getForm();
            }
            tvDosage.setText(dosageText);

            if (p.getDosageInstructions() != null && !p.getDosageInstructions().isEmpty()) {
                tvInstructions.setText(p.getDosageInstructions());
                tvInstructions.setVisibility(View.VISIBLE);
            } else {
                tvInstructions.setVisibility(View.GONE);
            }

            if (p.getPrescribedAt() != null) {
                tvDate.setText(itemView.getContext().getString(R.string.prescription_date_label)
                        + " " + formatDate(p.getPrescribedAt()));
                tvDate.setVisibility(View.VISIBLE);
            } else {
                tvDate.setVisibility(View.GONE);
            }

            if ("MOVED".equals(p.getStatus())) {
                btnMove.setVisibility(View.GONE);
            } else {
                btnMove.setVisibility(View.VISIBLE);
                btnMove.setOnClickListener(v -> {
                    if (listener != null) listener.onMoveToCabinet(p);
                });
            }
        }

        private String formatDate(String isoDate) {
            try {
                if (isoDate.contains("T")) {
                    String[] parts = isoDate.split("T");
                    return parts[0];
                }
                return isoDate;
            } catch (Exception e) {
                return isoDate;
            }
        }
    }
}
