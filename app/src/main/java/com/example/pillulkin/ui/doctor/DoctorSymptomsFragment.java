package com.example.pillulkin.ui.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pillulkin.R;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.PatientSymptomResponse;
import com.example.pillulkin.databinding.FragmentDoctorSymptomsBinding;
import com.example.pillulkin.ui.adapter.SymptomsAdapter;
import com.example.pillulkin.ui.patient.DiagnosisDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class DoctorSymptomsFragment extends Fragment {
    private FragmentDoctorSymptomsBinding binding;
    private DoctorCodeEntryViewModel viewModel;
    private SymptomsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDoctorSymptomsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(DoctorCodeEntryViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupDiagnoseButton();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_doctor_medicine) {
                requireActivity().onBackPressed();
                return true;
            } else if (id == R.id.action_doctor_prescriptions) {
                try {
                    androidx.navigation.Navigation.findNavController(requireView())
                            .navigate(R.id.action_doctorSymptoms_to_prescriptions);
                } catch (Exception ignored) {}
                return true;
            } else if (id == R.id.action_doctor_logout) {
                NetworkModule.getInstance(requireContext().getApplicationContext()).clearDoctorSession();
                androidx.navigation.Navigation.findNavController(requireView())
                        .popBackStack(R.id.roleSelectionFragment, false);
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new SymptomsAdapter(null, false);

        binding.rvSymptoms.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        binding.rvSymptoms.setAdapter(adapter);
    }

    private void setupDiagnoseButton() {
        binding.btnDiagnose.setOnClickListener(v -> {
            List<PatientSymptomResponse> symptoms = viewModel.getSymptoms().getValue();
            if (symptoms == null || symptoms.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), R.string.diagnose_error_no_symptoms, android.widget.Toast.LENGTH_LONG).show();
                return;
            }

            List<String> symptomTexts = new ArrayList<>();
            List<String> symptomTimestamps = new ArrayList<>();
            for (PatientSymptomResponse s : symptoms) {
                if (s.getSymptom() != null) {
                    symptomTexts.add(s.getSymptom());
                    symptomTimestamps.add(s.getTimestamp() != null ? s.getTimestamp() : "");
                }
            }

            DiagnosisDialogFragment dialog = DiagnosisDialogFragment.newSymptomsStep(
                    symptomTexts,
                    symptomTimestamps,
                    selectedSymptoms -> viewModel.diagnoseInitial(selectedSymptoms)
            );
            dialog.show(getChildFragmentManager(), "diagnosis_symptoms");
        });
    }

    private void observeData() {
        viewModel.getSymptoms().observe(getViewLifecycleOwner(), symptoms -> {
            if (adapter != null && binding != null) {
                adapter.submitList(symptoms);
                boolean empty = symptoms == null || symptoms.isEmpty();
                binding.rvSymptoms.setVisibility(empty ? View.GONE : View.VISIBLE);
                binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getSuggestedQuestions().observe(getViewLifecycleOwner(), questions -> {
            if (questions != null && !questions.isEmpty()) {
                DiagnosisDialogFragment dialog = DiagnosisDialogFragment.newQuestionsStep(
                        questions,
                        answers -> viewModel.requestFinalDiagnosis(answers)
                );
                dialog.show(getChildFragmentManager(), "diagnosis_questions");
            }
        });

        viewModel.getDiagnosisResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null && binding != null) {
                String disclaimer = getString(R.string.diagnosis_disclaimer);
                binding.tvDiagnosisResult.setText(
                        getString(R.string.diagnose_result_label) + " " + result + "\n\n" + disclaimer);
                binding.tvDiagnosisResult.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
    }
}