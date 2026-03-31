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
import com.example.pillulkin.domain.model.RecommendationItem;

public class RecommendationAdapter extends ListAdapter<RecommendationItem, RecommendationAdapter.RecommendationViewHolder> {

    public RecommendationAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<RecommendationItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<RecommendationItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull RecommendationItem oldItem, @NonNull RecommendationItem newItem) {
            return oldItem.getMedicineId() == newItem.getMedicineId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull RecommendationItem oldItem, @NonNull RecommendationItem newItem) {
            return oldItem.getMedicineName().equals(newItem.getMedicineName()) &&
                   oldItem.getDosage().equals(newItem.getDosage()) &&
                   oldItem.getRecommendationReason().equals(newItem.getRecommendationReason());
        }
    };

    @NonNull
    @Override
    public RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation, parent, false);
        return new RecommendationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendationViewHolder holder, int position) {
        RecommendationItem item = getItem(position);
        holder.bind(item);
    }

    static class RecommendationViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMedicineName;
        private final TextView tvExpired;
        private final TextView tvDosage;
        private final TextView tvExpiration;
        private final TextView tvReason;
        private final TextView tvWarning;

        RecommendationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvExpired = itemView.findViewById(R.id.tvExpired);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvExpiration = itemView.findViewById(R.id.tvExpiration);
            tvReason = itemView.findViewById(R.id.tvReason);
            tvWarning = itemView.findViewById(R.id.tvWarning);
        }

        void bind(RecommendationItem item) {
            tvMedicineName.setText(item.getMedicineName());
            tvDosage.setText(item.getDosage());

            String expirationText = itemView.getContext().getString(R.string.medicine_expiration) + ": " + item.getExpirationDate();
            tvExpiration.setText(expirationText);

            String reasonText = String.format(itemView.getContext().getString(R.string.recommendation_reason), item.getRecommendationReason());
            tvReason.setText(reasonText);

            if (item.isExpired()) {
                tvExpired.setVisibility(View.VISIBLE);
            } else {
                tvExpired.setVisibility(View.GONE);
            }

            if (item.getWarning() != null && !item.getWarning().isEmpty()) {
                tvWarning.setVisibility(View.VISIBLE);
                String warningText = String.format(itemView.getContext().getString(R.string.recommendation_warning), item.getWarning());
                tvWarning.setText(warningText);
            } else {
                tvWarning.setVisibility(View.GONE);
            }
        }
    }
}
