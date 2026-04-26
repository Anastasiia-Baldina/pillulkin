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
import com.example.pillulkin.data.remote.model.PatientSymptomResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SymptomsAdapter extends ListAdapter<PatientSymptomResponse, SymptomsAdapter.SymptomViewHolder> {
    private final OnSymptomDeleteListener deleteListener;
    private final boolean showDeleteButton;

    public interface OnSymptomDeleteListener {
        void onDelete(PatientSymptomResponse symptom);
    }

    public SymptomsAdapter(OnSymptomDeleteListener deleteListener) {
        this(deleteListener, true);
    }

    public SymptomsAdapter(OnSymptomDeleteListener deleteListener, boolean showDeleteButton) {
        super(DIFF_CALLBACK);
        this.deleteListener = deleteListener;
        this.showDeleteButton = showDeleteButton;
    }

    private static final DiffUtil.ItemCallback<PatientSymptomResponse> DIFF_CALLBACK = new DiffUtil.ItemCallback<PatientSymptomResponse>() {
        @Override
        public boolean areItemsTheSame(@NonNull PatientSymptomResponse oldItem, @NonNull PatientSymptomResponse newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull PatientSymptomResponse oldItem, @NonNull PatientSymptomResponse newItem) {
            return oldItem.getSymptom().equals(newItem.getSymptom());
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
        holder.bind(getItem(position));
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

        void bind(PatientSymptomResponse symptom) {
            tvDescription.setText(symptom.getSymptom());
            if (symptom.getTimestamp() != null && !symptom.getTimestamp().isEmpty()) {
                tvTimestamp.setText(formatTimestamp(symptom.getTimestamp()));
                tvTimestamp.setVisibility(View.VISIBLE);
            } else {
                tvTimestamp.setVisibility(View.GONE);
            }
            if (showDeleteButton && deleteListener != null) {
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> deleteListener.onDelete(symptom));
            } else {
                btnDelete.setVisibility(View.GONE);
            }
        }

        private String formatTimestamp(String timestamp) {
            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = input.parse(timestamp);
                SimpleDateFormat output = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                return output.format(date);
            } catch (Exception e) {
                if (timestamp.length() > 19) {
                    return timestamp.substring(0, 19).replace("T", " ");
                }
                return timestamp;
            }
        }
    }
}
