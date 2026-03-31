package com.example.pillulkin.ui.patient;

import android.app.DatePickerDialog;
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

import com.example.pillulkin.R;
import com.example.pillulkin.data.local.entity.MedicineEntity;
import com.example.pillulkin.databinding.FragmentAddMedicineBinding;
import com.example.pillulkin.utils.ValidationUtils;

import java.util.Calendar;

public class AddMedicineFragment extends Fragment {
    protected FragmentAddMedicineBinding binding;
    protected AddEditMedicineViewModel viewModel;
    protected String selectedDate = "";

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
        setupDatePicker();
        setupSaveButton();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });
    }

    private void setupDatePicker() {
        binding.etExpiration.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, day) -> {
                    selectedDate = String.format("%02d.%02d.%04d", day, month + 1, year);
                    binding.etExpiration.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.getDatePicker().setMinDate(System.currentTimeMillis());
            dialog.show();
        });
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                MedicineEntity medicine = createMedicine();
                viewModel.saveMedicine(medicine);
                Toast.makeText(requireContext(), R.string.success_saved, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(v).popBackStack();
            }
        });
    }

    protected boolean validateInput() {
        boolean isValid = true;

        String name = binding.etName.getText().toString().trim();
        if (!ValidationUtils.isValidName(name)) {
            binding.etName.setError(getString(R.string.validation_required));
            isValid = false;
        }

        String dosage = binding.etDosage.getText().toString().trim();
        if (!ValidationUtils.isValidDosage(dosage)) {
            binding.etDosage.setError(getString(R.string.validation_required));
            isValid = false;
        }

        if (!ValidationUtils.isValidExpirationDate(selectedDate)) {
            binding.etExpiration.setError(getString(R.string.validation_required));
            isValid = false;
        }

        String quantityStr = binding.etQuantity.getText().toString().trim();
        if (!quantityStr.isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (!ValidationUtils.isValidQuantity(quantity)) {
                    binding.etQuantity.setError(getString(R.string.validation_invalid_quantity));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                binding.etQuantity.setError(getString(R.string.validation_invalid_quantity));
                isValid = false;
            }
        }

        return isValid;
    }

    protected MedicineEntity createMedicine() {
        String name = binding.etName.getText().toString().trim();
        String dosage = binding.etDosage.getText().toString().trim();
        String quantityStr = binding.etQuantity.getText().toString().trim();
        int quantity = quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr);
        String form = binding.etForm.getText().toString().trim();
        String comment = binding.etComment.getText().toString().trim();

        return new MedicineEntity(name, dosage, selectedDate, quantity, form, comment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
