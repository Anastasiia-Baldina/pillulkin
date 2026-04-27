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
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.databinding.FragmentMedicineListBinding;
import com.example.pillulkin.ui.adapter.MedicineAdapter;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MedicineListFragment extends Fragment {
    private FragmentMedicineListBinding binding;
    private MedicineListViewModel viewModel;
    private MedicineAdapter adapter;
    private boolean sortByExpiration = false;
    private List<PatientMedicineResponse> allMedicines = new ArrayList<>();

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
        setupSort();
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
            } else if (id == R.id.action_notifications) {
                Navigation.findNavController(requireView()).navigate(R.id.action_medicineList_to_notifications);
                return true;
            } else if (id == R.id.action_logout) {
                showLogoutDialog();
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
                filterMedicines(s.toString().trim());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filterMedicines(String query) {
        if (query.isEmpty()) {
            applySortAndSubmit(allMedicines);
            updateEmptyState(allMedicines.isEmpty());
            return;
        }
        String lower = query.toLowerCase();
        List<PatientMedicineResponse> filtered = new ArrayList<>();
        for (PatientMedicineResponse m : allMedicines) {
            if (m.getMedicineName() != null && m.getMedicineName().toLowerCase().contains(lower)) {
                filtered.add(m);
            }
        }
        applySortAndSubmit(filtered);
        updateEmptyState(filtered.isEmpty());
    }

    private void setupSort() {
        binding.btnSort.setOnClickListener(v -> {
            sortByExpiration = !sortByExpiration;
            if (sortByExpiration) {
                Toast.makeText(requireContext(), R.string.sort_by_date, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), R.string.sort_by_name, Toast.LENGTH_SHORT).show();
            }
            List<PatientMedicineResponse> current = viewModel.getMedicines().getValue();
            if (current != null) {
                applySortAndSubmit(current);
            }
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
                allMedicines = new ArrayList<>(medicines);
                String query = binding.etSearch.getText().toString().trim();
                if (query.isEmpty()) {
                    applySortAndSubmit(medicines);
                    updateEmptyState(medicines.isEmpty());
                } else {
                    filterMedicines(query);
                }
            }
        });
    }

    private void applySortAndSubmit(List<PatientMedicineResponse> medicines) {
        List<PatientMedicineResponse> sorted = new ArrayList<>(medicines);
        if (sortByExpiration) {
            Collections.sort(sorted, (a, b) -> {
                LocalDate da = parseDate(a.getExpirationDate());
                LocalDate db = parseDate(b.getExpirationDate());
                if (da == null && db == null) return a.getMedicineName().compareToIgnoreCase(b.getMedicineName());
                if (da == null) return 1;
                if (db == null) return -1;
                return da.compareTo(db);
            });
        } else {
            Collections.sort(sorted, Comparator.comparing(m -> m.getMedicineName().toLowerCase()));
        }
        adapter.submitList(sorted);
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        String[] formats = {"yyyy-MM-dd", "dd.MM.yyyy", "yyyy-MM-dd'T'HH:mm:ss"};
        for (String fmt : formats) {
            try {
                return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern(fmt));
            } catch (DateTimeParseException ignored) {
            }
        }
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvMedicines.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showLogoutDialog() {
        String[] options = {getString(R.string.logout_switch_profile), getString(R.string.logout_sign_out)};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_dialog_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Navigation.findNavController(requireView()).popBackStack(R.id.nav_main, false);
                    } else {
                        NetworkModule.getInstance(requireContext().getApplicationContext()).clearPatientSession();
                        Navigation.findNavController(requireView()).popBackStack(R.id.nav_main, false);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
