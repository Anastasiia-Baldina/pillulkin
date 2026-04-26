package com.example.pillulkin.ui.doctor;

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
import com.example.pillulkin.databinding.FragmentDoctorSymptomsBinding;
import com.example.pillulkin.ui.adapter.SymptomsAdapter;

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
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_doctor_medicine) {
                Navigation.findNavController(requireView()).popBackStack();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new SymptomsAdapter(null, false);

        binding.rvSymptoms.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSymptoms.setAdapter(adapter);
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
    }
}
