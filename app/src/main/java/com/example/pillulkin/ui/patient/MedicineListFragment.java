package com.example.pillulkin.ui.patient;

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
import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;
import com.example.pillulkin.databinding.FragmentMedicineListBinding;
import com.example.pillulkin.ui.adapter.MedicineAdapter;

public class MedicineListFragment extends Fragment {
    private FragmentMedicineListBinding binding;
    private MedicineListViewModel viewModel;
    private MedicineAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentMedicineListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MedicineListViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupFab();
        observeData();

        viewModel.loadMedicines();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadMedicines();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_symptoms) {
                Navigation.findNavController(requireView()).navigate(R.id.action_medicineList_to_symptoms);
                return true;
            } else if (id == R.id.action_profile) {
                Navigation.findNavController(requireView()).navigate(R.id.action_medicineList_to_profile);
                return true;
            } else if (id == R.id.action_generate_code) {
                Navigation.findNavController(requireView()).navigate(R.id.action_medicineList_to_generateCode);
                return true;
            } else if (id == R.id.action_logout) {
                Navigation.findNavController(requireView()).popBackStack(R.id.nav_main, false);
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new MedicineAdapter(medicine -> {
            Bundle args = new Bundle();
            args.putLong("recordId", medicine.getId());
            args.putLong("medicineId", medicine.getMedicineId());
            args.putString("medicineName", medicine.getMedicineName());
            args.putString("dosage", medicine.getDosage());
            args.putString("form", medicine.getForm());
            args.putString("expirationDate", medicine.getExpirationDate());
            args.putString("quantity", medicine.getQuantity());
            Navigation.findNavController(requireView()).navigate(R.id.action_medicineList_to_medicineDetail, args);
        });

        binding.rvMedicines.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMedicines.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 2) {
                    viewModel.searchMedicines(s.toString());
                } else if (s.length() == 0) {
                    viewModel.loadMedicines();
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_medicineList_to_addMedicine);
        });
    }

    private void observeData() {
        viewModel.getMedicines().observe(getViewLifecycleOwner(), medicines -> {
            if (medicines != null) {
                adapter.submitList(medicines);
                updateEmptyState(medicines.isEmpty());
            }
        });

        viewModel.getSearchResults().observe(getViewLifecycleOwner(), results -> {
            if (results != null && !results.isEmpty()) {
                java.util.List<PatientMedicineResponse> current = viewModel.getMedicines().getValue();
                if (current == null || current.isEmpty() || binding.etSearch.getText().length() >= 2) {
                    adapter.submitList(convertToPatientMedicines(results));
                    updateEmptyState(false);
                }
            }
        });
    }

    private java.util.List<PatientMedicineResponse> convertToPatientMedicines(java.util.List<ReferenceMedicineResponse> refs) {
        java.util.List<PatientMedicineResponse> list = new java.util.ArrayList<>();
        for (ReferenceMedicineResponse ref : refs) {
            PatientMedicineResponse pmr = new PatientMedicineResponse();
            pmr.setId(ref.getId());
            pmr.setMedicineId(ref.getId());
            pmr.setMedicineName(ref.getName());
            pmr.setDosage(ref.getDosage());
            pmr.setForm(ref.getForm());
            list.add(pmr);
        }
        return list;
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvMedicines.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
