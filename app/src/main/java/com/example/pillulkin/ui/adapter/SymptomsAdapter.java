package com.example.pillulkin.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pillulkin.R;
import com.example.pillulkin.data.local.entity.SymptomEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SymptomsAdapter extends ListAdapter<SymptomEntity, SymptomsAdapter.SymptomViewHolder> {
    private final OnSymptomDeleteListener deleteListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public interface OnSymptomDeleteListener {
        void onDelete(SymptomEntity symptom);
    }

    public SymptomsAdapter(OnSymptomDeleteListener deleteListener) {
        super(DIFF_CALLBACK);
        this.deleteListener = deleteListener;
    }

    private static final DiffUtil.ItemCallback<SymptomEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<SymptomEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull SymptomEntity oldItem, @NonNull SymptomEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull SymptomEntity oldItem, @NonNull SymptomEntity newItem) {
            return oldItem.getDescription().equals(newItem.getDescription()) &&
                   oldItem.getTimestamp() == newItem.getTimestamp();
        }
    };

    @NonNull
    @Override
    public SymptomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_symptom, parent, false);
        return new SymptomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SymptomViewHolder holder, int position) {
        SymptomEntity symptom = getItem(position);
        holder.bind(symptom);
    }

    class SymptomViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDescription;
        private final TextView tvTimestamp;
        private final ImageButton btnDelete;

        SymptomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(SymptomEntity symptom) {
            tvDescription.setText(symptom.getDescription());
            tvTimestamp.setText(dateFormat.format(new Date(symptom.getTimestamp())));
            btnDelete.setOnClickListener(v -> deleteListener.onDelete(symptom));
        }
    }
}
