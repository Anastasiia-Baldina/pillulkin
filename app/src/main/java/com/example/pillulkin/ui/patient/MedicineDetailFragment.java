package com.example.pillulkin.ui.patient;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.pillulkin.R;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.databinding.FragmentMedicineDetailBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MedicineDetailFragment extends Fragment {
    private FragmentMedicineDetailBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentMedicineDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        loadMedicineDetails();
        setupButtons();
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

    private void loadMedicineDetails() {
        if (getArguments() == null) return;

        String name = getArguments().getString("medicineName", "");
        String dosage = getArguments().getString("dosage", "");
        String form = getArguments().getString("form", "");
        String expirationDate = getArguments().getString("expirationDate");
        String quantity = getArguments().getString("quantity");

        binding.tvName.setText(name);
        binding.tvDosage.setText(getString(R.string.medicine_dosage) + ": " + dosage);
        binding.tvForm.setText(getString(R.string.medicine_form) + ": " + form);

        if (expirationDate != null && !expirationDate.isEmpty()) {
            binding.tvExpiration.setText(getString(R.string.medicine_expiration) + ": " + expirationDate);
            binding.tvExpiration.setVisibility(View.VISIBLE);
        } else {
            binding.tvExpiration.setVisibility(View.GONE);
        }

        if (quantity != null && !quantity.isEmpty()) {
            binding.tvQuantity.setText(getString(R.string.medicine_quantity) + ": " + quantity);
            binding.tvQuantity.setVisibility(View.VISIBLE);
        } else {
            binding.tvQuantity.setVisibility(View.GONE);
        }

        binding.tvStatus.setVisibility(View.GONE);
        binding.tvComment.setVisibility(View.GONE);
    }

    private void setupButtons() {
        binding.btnEdit.setVisibility(View.VISIBLE);
        binding.btnEdit.setOnClickListener(v -> showEditDialog());

        if (getArguments() != null) {
            long recordId = getArguments().getLong("recordId", -1);
            binding.btnDelete.setOnClickListener(v -> {
                AddEditMedicineViewModel viewModel = new ViewModelProvider(this).get(AddEditMedicineViewModel.class);
                viewModel.deleteMedicine(recordId);
                Toast.makeText(requireContext(), R.string.success_deleted, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(v).popBackStack();
            });
        }
    }

    private void showEditDialog() {
        if (getArguments() == null) return;

        String currentExpiration = getArguments().getString("expirationDate", "");
        String currentQuantity = getArguments().getString("quantity", "");

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_medicine, null);
        EditText etDate = dialogView.findViewById(R.id.etExpirationDate);
        EditText etQty = dialogView.findViewById(R.id.etQuantity);

        if (currentExpiration != null && !currentExpiration.isEmpty()) {
            etDate.setText(currentExpiration);
        }
        if (currentQuantity != null && !currentQuantity.isEmpty()) {
            etQty.setText(currentQuantity);
        }

        etDate.setFocusable(false);
        etDate.setOnClickListener(v -> {
            LocalDate initial = LocalDate.now();
            if (currentExpiration != null && !currentExpiration.isEmpty()) {
                try {
                    initial = LocalDate.parse(currentExpiration, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e) {
                    try {
                        initial = LocalDate.parse(currentExpiration);
                    } catch (Exception ignored) {
                    }
                }
            }
            DatePickerDialog dpd = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        String selected = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        etDate.setText(selected);
                    },
                    initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth());
            dpd.show();
        });

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.edit_medicine_title)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String newDate = etDate.getText() != null ? etDate.getText().toString().trim() : "";
                    String newQty = etQty.getText() != null ? etQty.getText().toString().trim() : "";

                    long recordId = getArguments().getLong("recordId", -1);
                    AddEditMedicineViewModel viewModel = new ViewModelProvider(this).get(AddEditMedicineViewModel.class);
                    viewModel.updateMedicine(recordId, newDate, newQty);

                    Toast.makeText(requireContext(), R.string.success_saved, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
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
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
