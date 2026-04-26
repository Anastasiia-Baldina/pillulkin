package com.example.pillulkin.ui.patient;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.pillulkin.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiagnosisDialogFragment extends DialogFragment {

    public static final List<String> STUB_SYMPTOMS = Arrays.asList(
            "Повышенная температура",
            "Насморк",
            "Кашель",
            "Головная боль",
            "Боль в горле",
            "Чихание",
            "Слабость",
            "Боль в мышцах"
    );

    public interface OnDiagnosisConfirmedListener {
        void onDiagnosisConfirmed(List<Integer> binaryList);
    }

    private static final String ARG_SYMPTOMS = "symptoms";
    private List<String> symptoms;
    private OnDiagnosisConfirmedListener listener;
    private final List<CheckBox> checkboxes = new ArrayList<>();

    public static DiagnosisDialogFragment newInstance(List<String> symptoms, OnDiagnosisConfirmedListener listener) {
        DiagnosisDialogFragment fragment = new DiagnosisDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_SYMPTOMS, new ArrayList<>(symptoms));
        fragment.setArguments(args);
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            symptoms = getArguments().getStringArrayList(ARG_SYMPTOMS);
        }
        if (symptoms == null) {
            symptoms = STUB_SYMPTOMS;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_diagnosis, null);

        LinearLayout container = view.findViewById(R.id.checkboxContainer);
        checkboxes.clear();

        for (String symptom : symptoms) {
            CheckBox cb = new CheckBox(requireContext());
            cb.setText(symptom);
            cb.setTextSize(16);
            cb.setPadding(0, 8, 0, 8);
            container.addView(cb);
            checkboxes.add(cb);
        }

        view.findViewById(R.id.btnConfirmDiagnosis).setOnClickListener(v -> {
            List<Integer> binaryList = new ArrayList<>();
            for (CheckBox cb : checkboxes) {
                binaryList.add(cb.isChecked() ? 1 : 0);
            }
            if (listener != null) {
                listener.onDiagnosisConfirmed(binaryList);
            }
            dismiss();
        });

        return new MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .setCancelable(true)
                .create();
    }
}
