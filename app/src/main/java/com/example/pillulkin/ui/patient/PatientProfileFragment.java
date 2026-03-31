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

import com.example.pillulkin.R;
import com.example.pillulkin.data.local.entity.PatientProfileEntity;
import com.example.pillulkin.databinding.FragmentPatientProfileBinding;

public class PatientProfileFragment extends Fragment {
    private FragmentPatientProfileBinding binding;
    private PatientProfileViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentPatientProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PatientProfileViewModel.class);

        setupToolbar();
        setupSaveButton();
        loadProfile();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            String ageStr = binding.etAge.getText().toString().trim();
            int age = 0;
            if (!ageStr.isEmpty()) {
                try {
                    age = Integer.parseInt(ageStr);
                } catch (NumberFormatException e) {
                    binding.etAge.setError(getString(R.string.validation_invalid_age));
                    return;
                }
            }

            String allergies = binding.etAllergies.getText().toString().trim();
            String contraindications = binding.etContraindications.getText().toString().trim();
            String notes = binding.etNotes.getText().toString().trim();

            PatientProfileEntity profile = new PatientProfileEntity(name, age, allergies, contraindications, notes);
            viewModel.saveProfile(profile);
            Toast.makeText(requireContext(), R.string.success_saved, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).popBackStack();
        });
    }

    private void loadProfile() {
        viewModel.getProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                binding.etName.setText(profile.getName());
                binding.etAge.setText(String.valueOf(profile.getAge()));
                binding.etAllergies.setText(profile.getAllergies());
                binding.etContraindications.setText(profile.getContraindications());
                binding.etNotes.setText(profile.getNotes());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
