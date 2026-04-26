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
import com.example.pillulkin.databinding.FragmentDoctorSearchBinding;
import com.example.pillulkin.ui.adapter.RecommendationSectionAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DoctorSearchFragment extends Fragment {
    private FragmentDoctorSearchBinding binding;
    private DoctorSearchViewModel viewModel;
    private DoctorCodeEntryViewModel sharedViewModel;
    private RecommendationSectionAdapter adapter;
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
        sharedViewModel = new ViewModelProvider(requireActivity()).get(DoctorCodeEntryViewModel.class);

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
        adapter = new RecommendationSectionAdapter(medicine -> {
            long patientId = sharedViewModel.getPatientData().getValue() != null
                    ? sharedViewModel.getPatientData().getValue().getPatientId() : -1;
            Bundle args = new Bundle();
            args.putLong("medicineId", medicine.getId());
            args.putString("medicineName", medicine.getName());
            args.putString("dosage", medicine.getDosage() != null ? medicine.getDosage() : "");
            args.putString("form", medicine.getForm() != null ? medicine.getForm() : "");
            args.putString("activeSubstance", medicine.getActiveSubstance() != null ? medicine.getActiveSubstance() : "");
            args.putLong("patientId", patientId);
            Navigation.findNavController(requireView()).navigate(R.id.action_search_to_medicineDetail, args);
        });
        if (binding.rvResults != null) {
            binding.rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.rvResults.setAdapter(adapter);
        }
    }

    private void observeData() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), results -> {
            if (results != null) {
                List<PatientMedicineResponse> patientMeds = sharedViewModel.getMedicines().getValue();
                List<RecommendationItem> items = buildSectionedResults(results, patientMeds);
                adapter.submitItems(items);
            }
        });
    }

    List<RecommendationItem> buildSectionedResults(
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
    }
}
