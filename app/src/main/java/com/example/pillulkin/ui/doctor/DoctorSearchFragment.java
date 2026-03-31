package com.example.pillulkin.ui.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.pillulkin.R;
import com.example.pillulkin.databinding.FragmentDoctorSearchBinding;

public class DoctorSearchFragment extends Fragment {
    private FragmentDoctorSearchBinding binding;
    private DoctorSearchViewModel viewModel;
    private boolean isDiagnosisMode = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentDoctorSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DoctorSearchViewModel.class);

        setupToolbar();
        setupChips();
        setupSearchButton();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            try {
                Navigation.findNavController(v).popBackStack();
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });
    }

    private void setupChips() {
        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipDiagnosis)) {
                isDiagnosisMode = true;
                binding.etQuery.setHint(R.string.search_type_diagnosis);
            } else if (checkedIds.contains(R.id.chipMedicine)) {
                isDiagnosisMode = false;
                binding.etQuery.setHint(R.string.search_type_medicine);
            }
        });
    }

    private void setupSearchButton() {
        binding.btnSearch.setOnClickListener(v -> {
            if (binding == null || binding.etQuery == null) {
                return;
            }
            String query = binding.etQuery.getText() != null ? binding.etQuery.getText().toString().trim() : "";
            if (!query.isEmpty()) {
                if (isDiagnosisMode) {
                    viewModel.searchByDiagnosis(query);
                } else {
                    viewModel.searchByMedicineName(query);
                }
            }
        });
    }

    private void observeData() {
        viewModel.getSearchResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null && binding != null) {
                try {
                    Bundle args = new Bundle();
                    args.putString("query", result.getQuery());
                    args.putString("queryType", result.getQueryType());
                    Navigation.findNavController(requireView()).navigate(R.id.action_search_to_results, args);
                } catch (Exception e) {
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
