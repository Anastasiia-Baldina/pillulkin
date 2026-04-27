package com.example.pillulkin.ui.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pillulkin.R;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.PatientSymptomResponse;
import com.example.pillulkin.databinding.FragmentSymptomsBinding;
import com.example.pillulkin.ui.adapter.SymptomsAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class SymptomsFragment extends Fragment {
    private FragmentSymptomsBinding binding;
    private SymptomsViewModel viewModel;
    private SymptomsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentSymptomsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SymptomsViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupAddButton();
        setupDiagnoseButton();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_profile) {
                Navigation.findNavController(requireView()).navigate(R.id.action_medicineList_to_profile);
                return true;
            } else if (id == R.id.action_generate_code) {
                Navigation.findNavController(requireView()).navigate(R.id.action_medicineList_to_generateCode);
                return true;
            } else if (id == R.id.action_notifications) {
                Navigation.findNavController(requireView()).navigate(R.id.action_medicineList_to_notifications);
                return true;
            } else if (id == R.id.action_logout) {
                showLogoutDialog();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new SymptomsAdapter(
                symptom -> {
                    viewModel.deleteSymptom(symptom.getId());
                    Toast.makeText(requireContext(), R.string.success_deleted, Toast.LENGTH_SHORT).show();
                },
                symptom -> {
                    viewModel.renewSymptom(symptom.getId());
                    Toast.makeText(requireContext(), R.string.symptom_renew, Toast.LENGTH_SHORT).show();
                }
        );

        binding.rvSymptoms.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSymptoms.setAdapter(adapter);
    }

    private void setupAddButton() {
        binding.btnAdd.setOnClickListener(v -> {
            String symptom = binding.etSymptom.getText() != null ? 
                binding.etSymptom.getText().toString().trim() : "";
            
            if (symptom.isEmpty()) {
                binding.etSymptom.setError(getString(R.string.validation_required));
                return;
            }

            viewModel.addSymptom(symptom);
            binding.etSymptom.setText("");
            binding.etSymptom.setError(null);
            Toast.makeText(requireContext(), R.string.symptom_added, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupDiagnoseButton() {
        binding.btnDiagnose.setOnClickListener(v -> {
            List<PatientSymptomResponse> symptoms = viewModel.getSymptoms().getValue();
            if (symptoms == null || symptoms.isEmpty()) {
                Toast.makeText(requireContext(), R.string.diagnose_error_no_symptoms, Toast.LENGTH_LONG).show();
                return;
            }

            boolean hasActual = false;
            for (PatientSymptomResponse s : symptoms) {
                if (!SymptomsAdapter.isSymptomOutdated(s.getTimestamp())) {
                    hasActual = true;
                    break;
                }
            }
            if (!hasActual) {
                Toast.makeText(requireContext(), R.string.diagnose_error_all_outdated, Toast.LENGTH_LONG).show();
                return;
            }

            DiagnosisDialogFragment dialog = DiagnosisDialogFragment.newInstance(
                    DiagnosisDialogFragment.STUB_SYMPTOMS,
                    binaryList -> viewModel.diagnose(binaryList)
            );
            dialog.show(getChildFragmentManager(), "diagnosis_dialog");
        });
    }

    private void observeData() {
        viewModel.getSymptoms().observe(getViewLifecycleOwner(), symptoms -> {
            adapter.submitList(symptoms);
            updateEmptyState(symptoms == null || symptoms.isEmpty());
        });

        viewModel.getDiagnosisResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                binding.tvDiagnosisResult.setText(
                        getString(R.string.diagnose_result_label) + " " + result);
                binding.tvDiagnosisResult.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvSymptoms.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showLogoutDialog() {
        String[] options = {getString(R.string.logout_switch_profile), getString(R.string.logout_sign_out)};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_dialog_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Navigation.findNavController(requireView()).popBackStack(R.id.nav_main, false);
                    } else {
                        NetworkModule.getInstance(requireContext().getApplicationContext()).clearPatientSession();
                        Navigation.findNavController(requireView()).popBackStack(R.id.nav_main, false);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadSymptoms();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
