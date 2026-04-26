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
import com.example.pillulkin.data.remote.model.PrescriptionResponse;
import com.example.pillulkin.databinding.FragmentPrescriptionsBinding;
import com.example.pillulkin.ui.adapter.PrescriptionAdapter;

import java.util.List;

public class PrescriptionsFragment extends Fragment {

    private FragmentPrescriptionsBinding binding;
    private PrescriptionsViewModel viewModel;
    private PrescriptionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPrescriptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PrescriptionsViewModel.class);

        setupToolbar();
        setupRecyclerView();
        observeData();

        viewModel.loadPrescriptions();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            try { Navigation.findNavController(v).popBackStack(); }
            catch (Exception e) { if (getActivity() != null) getActivity().onBackPressed(); }
        });
    }

    private void setupRecyclerView() {
        adapter = new PrescriptionAdapter(prescription -> {
            viewModel.moveToCabinet(prescription);
            Toast.makeText(requireContext(), R.string.prescription_moved, Toast.LENGTH_SHORT).show();
        });
        binding.rvPrescriptions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPrescriptions.setAdapter(adapter);
    }

    private void observeData() {
        viewModel.getPrescriptions().observe(getViewLifecycleOwner(), list -> {
            if (list != null && !list.isEmpty()) {
                adapter.submitList(list);
                binding.rvPrescriptions.setVisibility(View.VISIBLE);
                binding.emptyState.setVisibility(View.GONE);
            } else {
                binding.rvPrescriptions.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.loadPrescriptions();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
