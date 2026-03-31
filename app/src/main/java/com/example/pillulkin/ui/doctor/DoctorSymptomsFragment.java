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
import com.example.pillulkin.databinding.FragmentDoctorSymptomsBinding;
import com.example.pillulkin.ui.adapter.SymptomsAdapter;
import com.example.pillulkin.ui.patient.SymptomsViewModel;

public class DoctorSymptomsFragment extends Fragment {
    private FragmentDoctorSymptomsBinding binding;
    private SymptomsViewModel viewModel;
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

        try {
            viewModel = new ViewModelProvider(this).get(SymptomsViewModel.class);
            setupToolbar();
            setupRecyclerView();
            observeData();
        } catch (Exception e) {
            if (binding != null) {
                binding.rvSymptoms.setVisibility(View.GONE);
            }
        }
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_doctor_medicine) {
                Navigation.findNavController(requireView()).popBackStack();
                return true;
            } else if (item.getItemId() == R.id.action_doctor_symptoms) {
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        if (binding == null || getContext() == null) {
            return;
        }

        adapter = new SymptomsAdapter(symptom -> {
        });

        binding.rvSymptoms.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSymptoms.setAdapter(adapter);
    }

    private void observeData() {
        if (viewModel == null) {
            return;
        }

        viewModel.getSymptoms().observe(getViewLifecycleOwner(), symptoms -> {
            if (adapter != null && binding != null) {
                adapter.submitList(symptoms);
                if (symptoms == null || symptoms.isEmpty()) {
                    binding.rvSymptoms.setVisibility(View.GONE);
                    binding.emptyState.setVisibility(View.VISIBLE);
                } else {
                    binding.rvSymptoms.setVisibility(View.VISIBLE);
                    binding.emptyState.setVisibility(View.GONE);
                }
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
