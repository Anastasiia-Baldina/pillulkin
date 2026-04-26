package com.example.pillulkin.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pillulkin.R;
import com.example.pillulkin.data.remote.model.PatientSymptomResponse;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SymptomsAdapter extends ListAdapter<PatientSymptomResponse, SymptomsAdapter.SymptomViewHolder> {
    private final OnSymptomDeleteListener deleteListener;
    private final OnSymptomRenewListener renewListener;
    private final boolean showDeleteButton;

    public interface OnSymptomDeleteListener {
        void onDelete(PatientSymptomResponse symptom);
    }

    public interface OnSymptomRenewListener {
        void onRenew(PatientSymptomResponse symptom);
    }

    public SymptomsAdapter(OnSymptomDeleteListener deleteListener) {
        this(deleteListener, null, true);
    }

    public SymptomsAdapter(OnSymptomDeleteListener deleteListener, OnSymptomRenewListener renewListener) {
        this(deleteListener, renewListener, true);
    }

    public SymptomsAdapter(OnSymptomDeleteListener deleteListener, boolean showDeleteButton) {
        this(deleteListener, null, showDeleteButton);
    }

    public SymptomsAdapter(OnSymptomDeleteListener deleteListener, OnSymptomRenewListener renewListener, boolean showDeleteButton) {
        super(DIFF_CALLBACK);
        this.deleteListener = deleteListener;
        this.renewListener = renewListener;
        this.showDeleteButton = showDeleteButton;
    }

    private static final DiffUtil.ItemCallback<PatientSymptomResponse> DIFF_CALLBACK = new DiffUtil.ItemCallback<PatientSymptomResponse>() {
        @Override
        public boolean areItemsTheSame(@NonNull PatientSymptomResponse oldItem, @NonNull PatientSymptomResponse newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull PatientSymptomResponse oldItem, @NonNull PatientSymptomResponse newItem) {
            return oldItem.getSymptom().equals(newItem.getSymptom()) &&
                   equalsOrBothNull(oldItem.getTimestamp(), newItem.getTimestamp());
        }

        private boolean equalsOrBothNull(String a, String b) {
            if (a == null && b == null) return true;
            if (a == null || b == null) return false;
            return a.equals(b);
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

    public static boolean isSymptomOutdated(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return false;
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date symptomDate = input.parse(timestamp);
            if (symptomDate == null) return false;
            long diffMs = System.currentTimeMillis() - symptomDate.getTime();
            long diffDays = TimeUnit.MILLISECONDS.toDays(diffMs);
            return diffDays > 7;
        } catch (Exception e) {
            return false;
        }
    }

    class SymptomViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView tvDescription;
        private final TextView tvTimestamp;
        private final ImageButton btnDelete;
        private final com.google.android.material.button.MaterialButton btnRenewSymptom;

        SymptomViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnRenewSymptom = itemView.findViewById(R.id.btnRenewSymptom);
        }

        void bind(PatientSymptomResponse symptom) {
            tvDescription.setText(symptom.getSymptom());

            if (symptom.getTimestamp() != null && !symptom.getTimestamp().isEmpty()) {
                tvTimestamp.setText(formatTimestamp(symptom.getTimestamp()));
                tvTimestamp.setVisibility(View.VISIBLE);
            } else {
                tvTimestamp.setVisibility(View.GONE);
            }

            boolean outdated = isSymptomOutdated(symptom.getTimestamp());
            int defaultColor = ContextCompat.getColor(itemView.getContext(), R.color.card_background);
            int outdatedColor = ContextCompat.getColor(itemView.getContext(), R.color.symptom_outdated);
            int outdatedTextColor = ContextCompat.getColor(itemView.getContext(), R.color.symptom_outdated_text);
            int normalTextColor = ContextCompat.getColor(itemView.getContext(), R.color.on_background);

            if (outdated) {
                cardView.setCardBackgroundColor(outdatedColor);
                tvDescription.setTextColor(outdatedTextColor);
                tvTimestamp.setText(formatTimestamp(symptom.getTimestamp()) + " \u2022 " +
                        itemView.getContext().getString(R.string.symptom_outdated_label));
                if (renewListener != null) {
                    btnRenewSymptom.setVisibility(View.VISIBLE);
                    btnRenewSymptom.setOnClickListener(v -> renewListener.onRenew(symptom));
                } else {
                    btnRenewSymptom.setVisibility(View.GONE);
                }
            } else {
                cardView.setCardBackgroundColor(defaultColor);
                tvDescription.setTextColor(normalTextColor);
                btnRenewSymptom.setVisibility(View.GONE);
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
