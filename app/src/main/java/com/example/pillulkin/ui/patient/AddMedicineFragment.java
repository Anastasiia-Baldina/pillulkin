package com.example.pillulkin.ui.patient;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.pillulkin.R;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;
import com.example.pillulkin.databinding.FragmentAddMedicineBinding;
import com.example.pillulkin.ui.adapter.ReferenceMedicineAdapter;
import com.example.pillulkin.ui.CustomMedicineDialogHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Calendar;

public class AddMedicineFragment extends Fragment {
    protected FragmentAddMedicineBinding binding;
    protected AddEditMedicineViewModel viewModel;
    private ReferenceMedicineAdapter searchAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentAddMedicineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddEditMedicineViewModel.class);

        setupToolbar();
        setupChips();
        setupSearch();
        observeData();
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
                if (NetworkModule.getInstance(requireContext().getApplicationContext()).isLocalMode()) {
                    Toast.makeText(requireContext(), R.string.code_auth_required, Toast.LENGTH_SHORT).show();
                } else {
                    Navigation.findNavController(requireView()).navigate(R.id.action_medicineList_to_generateCode);
                }
                return true;
            } else if (id == R.id.action_notifications) {
                Navigation.findNavController(requireView()).navigate(R.id.action_medicineList_to_notifications);
                return true;
            } else if (id == R.id.action_prescriptions) {
                Navigation.findNavController(requireView()).navigate(R.id.action_medicineList_to_prescriptions);
                return true;
            } else if (id == R.id.action_logout) {
                showLogoutDialog();
                return true;
            }
            return false;
        });
    }

    private void setupChips() {
        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipSymptoms)) {
                viewModel.setSymptomMode(true);
                binding.searchLayout.setHint(getString(R.string.search_type_diagnosis_hint));
                searchAdapter.setCustomButtonVisible(false);
            } else {
                viewModel.setSymptomMode(false);
                binding.searchLayout.setHint(getString(R.string.search_medicine_hint));
                searchAdapter.setCustomButtonVisible(true);
            }
        });
    }

    private void setupSearch() {
        searchAdapter = new ReferenceMedicineAdapter(
                medicine -> showAddDialog(medicine),
                () -> showCustomMedicineDialog()
        );
        binding.rvSearchResults.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        binding.rvSearchResults.setAdapter(searchAdapter);

        binding.btnSearch.setOnClickListener(v -> performSearch());

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });
    }

    private void performSearch() {
        String query = binding.etSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            viewModel.searchMedicines(query);
        }
    }

    private void showAddDialog(ReferenceMedicineResponse medicine) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_medicine, null);
        TextView tvName = dialogView.findViewById(R.id.tvMedicineNameDialog);
        EditText etExpiration = dialogView.findViewById(R.id.etExpirationDate);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);

        tvName.setText(medicine.getName());

        etExpiration.setFocusable(false);
        etExpiration.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                etExpiration.setText(date);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_medicine_title)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String expDate = etExpiration.getText().toString().trim();
                    String quantity = etQuantity.getText().toString().trim();
                    String medName = medicine.getName() != null ? medicine.getName() : "";
                    String medDosage = medicine.getDosage() != null ? medicine.getDosage() : "";
                    String medForm = medicine.getForm() != null ? medicine.getForm() : "";
                    String medActiveSubstance = medicine.getActiveSubstance() != null ? medicine.getActiveSubstance() : "";
                    viewModel.addMedicine(medicine.getId(), expDate.isEmpty() ? null : expDate, quantity.isEmpty() ? null : quantity,
                            medName, medDosage, medForm, medActiveSubstance);
                    Toast.makeText(requireContext(), R.string.success_saved, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showCustomMedicineDialog() {
        CustomMedicineDialogHelper.show(requireContext(), (name, dosage, form, activeSubstance, expirationDate, quantity) -> {
            viewModel.addCustomMedicine(name, dosage, form, activeSubstance, null, null, expirationDate, quantity);
            Toast.makeText(requireContext(), R.string.success_saved, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).popBackStack();
        });
    }

    private void observeData() {
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), results -> {
            if (results != null) {
                searchAdapter.submitList(results);
            }
        });
    }

    private void showLogoutDialog() {
        String[] options = {getString(R.string.logout_switch_profile), getString(R.string.logout_sign_out)};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_dialog_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Navigation.findNavController(requireView()).popBackStack(R.id.roleSelectionFragment, false);
                    } else {
                        NetworkModule.getInstance(requireContext().getApplicationContext()).clearPatientSession();
                        Navigation.findNavController(requireView()).popBackStack(R.id.roleSelectionFragment, false);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}