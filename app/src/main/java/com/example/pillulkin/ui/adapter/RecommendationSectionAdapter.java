package com.example.pillulkin.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pillulkin.R;
import com.example.pillulkin.data.remote.model.RecommendationItem;

import java.util.ArrayList;
import java.util.List;

public class RecommendationSectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<RecommendationItem> items = new ArrayList<>();
    private final OnMedicineClickListener listener;

    public interface OnMedicineClickListener {
        void onMedicineClick(com.example.pillulkin.data.remote.model.ReferenceMedicineResponse medicine);
    }

    public RecommendationSectionAdapter(OnMedicineClickListener listener) {
        this.listener = listener;
    }

    public void submitItems(List<RecommendationItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == RecommendationItem.TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_recommendation_header, parent, false);
            return new HeaderViewHolder(view);
        }
        View view = inflater.inflate(R.layout.item_recommendation, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RecommendationItem item = items.get(position);
        if (item.getType() == RecommendationItem.TYPE_HEADER) {
            ((HeaderViewHolder) holder).bind(item.getHeaderText());
        } else {
            ((MedicineViewHolder) holder).bind(item.getMedicine(), item.isFromCabinet());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSectionTitle);
        }

        void bind(String text) {
            tvTitle.setText(text);
        }
    }

    class MedicineViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMedicineName;
        private final TextView tvDosage;
        private final TextView tvReason;

        MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvReason = itemView.findViewById(R.id.tvReason);
        }

        void bind(com.example.pillulkin.data.remote.model.ReferenceMedicineResponse medicine, boolean fromCabinet) {
            tvMedicineName.setText(medicine.getName());
            String dosageText = medicine.getDosage();
            if (medicine.getForm() != null && !medicine.getForm().isEmpty()) {
                dosageText += " \u2022 " + medicine.getForm();
            }
            tvDosage.setText(dosageText);

            if (fromCabinet) {
                tvReason.setVisibility(View.VISIBLE);
                tvReason.setText(R.string.recommendation_in_cabinet);
            } else {
                tvReason.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMedicineClick(medicine);
                }
            });
        }
    }
}
