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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pillulkin.R;
import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;
import com.example.pillulkin.databinding.FragmentDoctorSearchBinding;
import com.example.pillulkin.ui.adapter.ReferenceMedicineAdapter;

import java.util.Arrays;
import java.util.List;

public class DoctorSearchFragment extends Fragment {
    private FragmentDoctorSearchBinding binding;
    private DoctorSearchViewModel viewModel;
    private ReferenceMedicineAdapter adapter;
    private boolean isSymptomMode = true;

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
        setupRecyclerView();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            try {
                Navigation.findNavController(v).popBackStack();
            } catch (Exception e) {
                if (getActivity() != null) getActivity().onBackPressed();
            }
        });
    }

    private void setupChips() {
        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipDiagnosis)) {
                isSymptomMode = true;
                binding.etQuery.setHint(R.string.search_type_diagnosis);
            } else if (checkedIds.contains(R.id.chipMedicine)) {
                isSymptomMode = false;
                binding.etQuery.setHint(R.string.search_type_medicine);
            }
        });
    }

    private void setupSearchButton() {
        binding.btnSearch.setOnClickListener(v -> {
            if (binding.etQuery == null) return;
            String query = binding.etQuery.getText() != null ? binding.etQuery.getText().toString().trim() : "";
            if (!query.isEmpty()) {
                if (isSymptomMode) {
                    List<String> symptoms = Arrays.asList(query.split("[,;]\\s*"));
                    viewModel.searchBySymptoms(symptoms);
                } else {
                    viewModel.searchByMedicineName(query);
                }
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ReferenceMedicineAdapter(null);
        if (binding.rvResults != null) {
            binding.rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.rvResults.setAdapter(adapter);
        }
    }

    private void observeData() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), results -> {
            if (results != null && adapter != null) {
                adapter.submitList(results);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
