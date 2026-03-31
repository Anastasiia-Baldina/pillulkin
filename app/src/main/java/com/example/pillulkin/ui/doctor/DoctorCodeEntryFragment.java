package com.example.pillulkin.ui.doctor;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.pillulkin.R;
import com.example.pillulkin.databinding.FragmentDoctorCodeEntryBinding;

public class DoctorCodeEntryFragment extends Fragment {
    private FragmentDoctorCodeEntryBinding binding;
    private DoctorCodeEntryViewModel viewModel;
    private boolean isNavigating = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentDoctorCodeEntryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DoctorCodeEntryViewModel.class);

        setupToolbar();
        setupSubmitButton();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupSubmitButton() {
        binding.btnSubmit.setOnClickListener(v -> {
            if (isNavigating) {
                return;
            }
            if (binding == null || binding.etCode == null) {
                return;
            }
            String code = binding.etCode.getText() != null ? binding.etCode.getText().toString().trim() : "";
            if (code.isEmpty()) {
                binding.codeLayout.setError(getString(R.string.validation_required));
                return;
            }
            binding.codeLayout.setError(null);
            viewModel.validateCode(code);
        });
    }

    private void observeData() {
        viewModel.getValidationResult().observe(getViewLifecycleOwner(), result -> {
            if (binding == null || result == null) {
                return;
            }
            if (result.isValid()) {
                binding.codeLayout.setError(null);
                navigateToMedicineList();
            } else {
                binding.codeLayout.setError(result.getMessage());
            }
        });
    }

    private void navigateToMedicineList() {
        if (isNavigating) {
            return;
        }
        isNavigating = true;
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                if (getView() != null && isAdded()) {
                    NavController navController = Navigation.findNavController(getView());
                    navController.navigate(R.id.action_codeEntry_to_medicineList);
                }
            } catch (Exception e) {
                isNavigating = false;
                if (binding != null) {
                    binding.codeLayout.setError(getString(R.string.error_generic));
                }
            }
        }, 100);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        isNavigating = false;
    }
}
