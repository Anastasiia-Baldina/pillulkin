package com.example.pillulkin.ui.patient;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.pillulkin.R;
import com.example.pillulkin.ui.adapter.SymptomsAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiagnosisDialogFragment extends DialogFragment {

    public interface OnSymptomsSelectedListener {
        void onSymptomsSelected(List<String> selectedSymptoms);
    }

    public interface OnQuestionsAnsweredListener {
        void onQuestionsAnswered(List<String> yesAnswers);
    }

    private static final String ARG_SYMPTOM_TEXTS = "symptom_texts";
    private static final String ARG_SYMPTOM_TIMESTAMPS = "symptom_timestamps";
    private static final String ARG_QUESTIONS = "questions";
    private static final String ARG_STEP = "step";

    private ArrayList<String> symptomTexts;
    private ArrayList<String> symptomTimestamps;
    private List<String> questions;
    private String step;
    private OnSymptomsSelectedListener symptomsListener;
    private OnQuestionsAnsweredListener questionsListener;

    private LinearLayout checkboxContainer;
    private CheckBox cbSelectAll;
    private final List<CheckBox> symptomCheckboxes = new ArrayList<>();
    private final List<Boolean> symptomOutdated = new ArrayList<>();
    private final List<String> symptomDates = new ArrayList<>();

    public static DiagnosisDialogFragment newSymptomsStep(
            List<String> symptomTexts,
            List<String> symptomTimestamps,
            OnSymptomsSelectedListener listener) {
        DiagnosisDialogFragment fragment = new DiagnosisDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_SYMPTOM_TEXTS, new ArrayList<>(symptomTexts));
        args.putStringArrayList(ARG_SYMPTOM_TIMESTAMPS, new ArrayList<>(symptomTimestamps));
        args.putString(ARG_STEP, "symptoms");
        fragment.setArguments(args);
        fragment.symptomsListener = listener;
        return fragment;
    }

    public static DiagnosisDialogFragment newQuestionsStep(
            List<String> questions, OnQuestionsAnsweredListener listener) {
        DiagnosisDialogFragment fragment = new DiagnosisDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_QUESTIONS, new ArrayList<>(questions));
        args.putString(ARG_STEP, "questions");
        fragment.setArguments(args);
        fragment.questionsListener = listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            symptomTexts = getArguments().getStringArrayList(ARG_SYMPTOM_TEXTS);
            symptomTimestamps = getArguments().getStringArrayList(ARG_SYMPTOM_TIMESTAMPS);
            questions = getArguments().getStringArrayList(ARG_QUESTIONS);
            step = getArguments().getString(ARG_STEP, "symptoms");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_diagnosis, null);

        TextView tvTitle = view.findViewById(R.id.tvDiagnosisTitle);
        checkboxContainer = view.findViewById(R.id.checkboxContainer);
        cbSelectAll = view.findViewById(R.id.cbSelectAll);

        if ("questions".equals(step)) {
            setupQuestionsStep(view, tvTitle);
        } else {
            setupSymptomsStep(view, tvTitle);
        }

        return new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .setCancelable(true)
                .create();
    }

    private void setupQuestionsStep(View view, TextView tvTitle) {
        tvTitle.setText(R.string.diagnose_questions_title);
        view.findViewById(R.id.rgFilter).setVisibility(View.GONE);
        cbSelectAll.setVisibility(View.GONE);

        List<CheckBox> questionCheckboxes = new ArrayList<>();
        if (questions != null) {
            for (String question : questions) {
                CheckBox cb = new CheckBox(requireContext());
                cb.setText(question);
                cb.setTextSize(16);
                cb.setPadding(0, 8, 0, 8);
                checkboxContainer.addView(cb);
                questionCheckboxes.add(cb);
            }
        }

        view.findViewById(R.id.btnConfirmDiagnosis).setOnClickListener(v -> {
            List<String> yesAnswers = new ArrayList<>();
            for (CheckBox cb : questionCheckboxes) {
                if (cb.isChecked()) {
                    yesAnswers.add(cb.getText().toString().replace(" ", "_"));
                }
            }
            if (questionsListener != null) {
                questionsListener.onQuestionsAnswered(yesAnswers);
            }
            dismiss();
        });
    }

    private void setupSymptomsStep(View view, TextView tvTitle) {
        tvTitle.setText(R.string.diagnose_title);

        symptomCheckboxes.clear();
        symptomOutdated.clear();
        symptomDates.clear();
        checkboxContainer.removeAllViews();

        if (symptomTexts == null || symptomTexts.isEmpty()) return;

        int outdatedColor = ContextCompat.getColor(requireContext(), R.color.symptom_outdated_text);
        int normalColor = ContextCompat.getColor(requireContext(), R.color.on_background);
        int hintColor = ContextCompat.getColor(requireContext(), R.color.hint);

        for (int i = 0; i < symptomTexts.size(); i++) {
            String text = symptomTexts.get(i);
            String timestamp = (symptomTimestamps != null && i < symptomTimestamps.size())
                    ? symptomTimestamps.get(i) : null;
            boolean outdated = SymptomsAdapter.isSymptomOutdated(timestamp);
            String dateStr = formatTimestamp(timestamp);

            symptomOutdated.add(outdated);
            symptomDates.add(dateStr);

            LinearLayout item = new LinearLayout(requireContext());
            item.setOrientation(LinearLayout.VERTICAL);
            item.setPadding(0, 4, 0, 4);

            CheckBox cb = new CheckBox(requireContext());
            cb.setText(text);
            cb.setTextSize(16);
            cb.setTextColor(outdated ? outdatedColor : normalColor);
            item.addView(cb);

            if (dateStr != null && !dateStr.isEmpty()) {
                TextView tvDate = new TextView(requireContext());
                tvDate.setText(dateStr + (outdated ? " \u2022 " + getString(R.string.symptom_outdated_label) : ""));
                tvDate.setTextSize(12);
                tvDate.setTextColor(hintColor);
                tvDate.setPadding(44, 0, 0, 0);
                item.addView(tvDate);
            }

            checkboxContainer.addView(item);
            symptomCheckboxes.add(cb);
        }

        RadioButton rbActualOnly = view.findViewById(R.id.rbActualOnly);
        RadioButton rbAllSymptoms = view.findViewById(R.id.rbAllSymptoms);

        rbActualOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) applyFilter(true);
        });
        rbAllSymptoms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) applyFilter(false);
        });

        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (int i = 0; i < symptomCheckboxes.size(); i++) {
                if (checkboxContainer.getChildAt(i) != null
                        && checkboxContainer.getChildAt(i).getVisibility() == View.VISIBLE) {
                    symptomCheckboxes.get(i).setChecked(isChecked);
                }
            }
        });

        applyFilter(true);

        view.findViewById(R.id.btnConfirmDiagnosis).setOnClickListener(v -> {
            List<String> selected = new ArrayList<>();
            for (int i = 0; i < symptomCheckboxes.size(); i++) {
                if (symptomCheckboxes.get(i).isChecked()
                        && checkboxContainer.getChildAt(i) != null
                        && checkboxContainer.getChildAt(i).getVisibility() == View.VISIBLE) {
                    selected.add(symptomTexts.get(i).replace(" ", "_"));
                }
            }
            if (selected.isEmpty()) return;
            if (symptomsListener != null) {
                symptomsListener.onSymptomsSelected(selected);
            }
            dismiss();
        });
    }

    private void applyFilter(boolean actualOnly) {
        cbSelectAll.setChecked(false);
        for (int i = 0; i < symptomCheckboxes.size(); i++) {
            View itemView = checkboxContainer.getChildAt(i);
            if (itemView == null) continue;

            if (actualOnly && symptomOutdated.get(i)) {
                itemView.setVisibility(View.GONE);
                symptomCheckboxes.get(i).setChecked(false);
            } else {
                itemView.setVisibility(View.VISIBLE);
            }
        }
    }

    private String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return "";
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
