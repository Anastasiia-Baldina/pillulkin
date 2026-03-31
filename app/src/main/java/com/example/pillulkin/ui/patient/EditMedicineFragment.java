package com.example.pillulkin.ui.patient;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.example.pillulkin.R;
import com.example.pillulkin.data.local.entity.MedicineEntity;

public class EditMedicineFragment extends AddMedicineFragment {
    private long medicineId = -1;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            medicineId = getArguments().getLong("medicineId", -1);
        }

        if (medicineId != -1) {
            binding.toolbar.setTitle(R.string.edit_medicine_title);
            loadMedicine();
        }
    }

    private void loadMedicine() {
        viewModel.getMedicine(medicineId).observe(getViewLifecycleOwner(), medicine -> {
            if (medicine != null) {
                binding.etName.setText(medicine.getName());
                binding.etDosage.setText(medicine.getDosage());
                binding.etExpiration.setText(medicine.getExpirationDate());
                selectedDate = medicine.getExpirationDate();
                binding.etQuantity.setText(String.valueOf(medicine.getQuantity()));
                binding.etForm.setText(medicine.getForm());
                binding.etComment.setText(medicine.getComment());
            }
        });
    }

    @Override
    protected MedicineEntity createMedicine() {
        MedicineEntity medicine = super.createMedicine();
        medicine.setId(medicineId);
        return medicine;
    }
}
