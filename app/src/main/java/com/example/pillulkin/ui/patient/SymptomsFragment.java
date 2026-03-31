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
import com.example.pillulkin.databinding.FragmentSymptomsBinding;
import com.example.pillulkin.ui.adapter.SymptomsAdapter;

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
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new SymptomsAdapter(symptom -> {
            viewModel.deleteSymptom(symptom);
            Toast.makeText(requireContext(), R.string.success_deleted, Toast.LENGTH_SHORT).show();
        });

        binding.rvSymptoms.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSymptoms.setAdapter(adapter);
    }

    private void setupAddButton() {
        binding.btnAdd.setOnClickListener(v -> {
            String description = binding.etSymptom.getText() != null ? 
                binding.etSymptom.getText().toString().trim() : "";
            
            if (description.isEmpty()) {
                binding.etSymptom.setError(getString(R.string.validation_required));
                return;
            }

            viewModel.addSymptom(description);
            binding.etSymptom.setText("");
            binding.etSymptom.setError(null);
            Toast.makeText(requireContext(), R.string.symptom_added, Toast.LENGTH_SHORT).show();
        });
    }

    private void observeData() {
        viewModel.getSymptoms().observe(getViewLifecycleOwner(), symptoms -> {
            adapter.submitList(symptoms);
            updateEmptyState(symptoms.isEmpty());
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvSymptoms.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
