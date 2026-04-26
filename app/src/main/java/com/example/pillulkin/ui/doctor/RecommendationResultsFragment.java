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
import com.example.pillulkin.databinding.FragmentRecommendationResultsBinding;
import com.example.pillulkin.ui.adapter.ReferenceMedicineAdapter;

public class RecommendationResultsFragment extends Fragment {
    private FragmentRecommendationResultsBinding binding;
    private DoctorSearchViewModel viewModel;
    private ReferenceMedicineAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentRecommendationResultsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            viewModel = new ViewModelProvider(requireActivity()).get(DoctorSearchViewModel.class);
            setupToolbar();
            setupRecyclerView();
            observeData();
        } catch (Exception e) {
            if (binding != null) {
                binding.rvResults.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        }
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

    private void setupRecyclerView() {
        if (binding == null || getContext() == null) return;

        adapter = new ReferenceMedicineAdapter(null);
        binding.rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvResults.setAdapter(adapter);
    }

    private void observeData() {
        if (viewModel == null) return;

        viewModel.getSearchResults().observe(getViewLifecycleOwner(), results -> {
            if (binding == null) return;

            if (results != null && !results.isEmpty()) {
                binding.emptyState.setVisibility(View.GONE);
                binding.rvResults.setVisibility(View.VISIBLE);
                if (adapter != null) {
                    adapter.submitList(results);
                }
            } else {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.rvResults.setVisibility(View.GONE);
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
