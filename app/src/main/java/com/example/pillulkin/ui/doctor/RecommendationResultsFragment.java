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
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.data.remote.model.RecommendationItem;
import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;
import com.example.pillulkin.databinding.FragmentRecommendationResultsBinding;
import com.example.pillulkin.ui.adapter.RecommendationSectionAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecommendationResultsFragment extends Fragment {
    private FragmentRecommendationResultsBinding binding;
    private DoctorSearchViewModel viewModel;
    private DoctorCodeEntryViewModel sharedViewModel;
    private RecommendationSectionAdapter adapter;

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
            sharedViewModel = new ViewModelProvider(requireActivity()).get(DoctorCodeEntryViewModel.class);
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

        adapter = new RecommendationSectionAdapter(null);
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

                List<PatientMedicineResponse> patientMeds = sharedViewModel.getMedicines().getValue();
                List<RecommendationItem> items = buildSectionedResults(results, patientMeds);
                if (adapter != null) {
                    adapter.submitItems(items);
                }
            } else {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.rvResults.setVisibility(View.GONE);
            }
        });
    }

    private List<RecommendationItem> buildSectionedResults(
            List<ReferenceMedicineResponse> results,
            List<PatientMedicineResponse> patientMeds) {

        Set<Long> patientMedicineIds = new HashSet<>();
        Set<String> patientMedicineNames = new HashSet<>();
        if (patientMeds != null) {
            for (PatientMedicineResponse pm : patientMeds) {
                if (pm.getMedicineId() != null) patientMedicineIds.add(pm.getMedicineId());
                if (pm.getMedicineName() != null)
                    patientMedicineNames.add(pm.getMedicineName().toLowerCase());
            }
        }

        List<RecommendationItem> cabinetList = new ArrayList<>();
        List<RecommendationItem> otherList = new ArrayList<>();

        for (ReferenceMedicineResponse med : results) {
            boolean inCabinet = false;
            if (med.getId() != null && patientMedicineIds.contains(med.getId())) {
                inCabinet = true;
            }
            if (!inCabinet && med.getName() != null) {
                String nameLower = med.getName().toLowerCase();
                for (String patientName : patientMedicineNames) {
                    if (nameLower.contains(patientName) || patientName.contains(nameLower)) {
                        inCabinet = true;
                        break;
                    }
                }
            }
            if (inCabinet) {
                cabinetList.add(RecommendationItem.medicine(med, true));
            } else {
                otherList.add(RecommendationItem.medicine(med, false));
            }
        }

        List<RecommendationItem> items = new ArrayList<>();
        if (!cabinetList.isEmpty()) {
            items.add(RecommendationItem.header(getString(R.string.recommendation_cabinet_header)));
            items.addAll(cabinetList);
        }
        if (!otherList.isEmpty()) {
            items.add(RecommendationItem.header(getString(R.string.recommendation_other_header)));
            items.addAll(otherList);
        }
        return items;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
    }
}
