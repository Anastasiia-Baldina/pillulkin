package com.example.pillulkin.ui;

import android.app.DatePickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.pillulkin.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Calendar;

public class CustomMedicineDialogHelper {

    public interface OnSaveListener {
        void onSave(String name, String dosage, String form, String activeSubstance,
                    String expirationDate, String quantity);
    }

    public static void show(android.content.Context context, OnSaveListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_custom_medicine, null);
        EditText etName = dialogView.findViewById(R.id.etCustomMedicineName);
        EditText etDosage = dialogView.findViewById(R.id.etCustomDosage);
        EditText etForm = dialogView.findViewById(R.id.etCustomForm);
        EditText etActiveSubstance = dialogView.findViewById(R.id.etCustomActiveSubstance);
        EditText etExpiration = dialogView.findViewById(R.id.etCustomExpirationDate);
        EditText etQuantity = dialogView.findViewById(R.id.etCustomQuantity);

        etExpiration.setFocusable(false);
        etExpiration.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
                String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                etExpiration.setText(date);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.add_custom_medicine)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        android.widget.Toast.makeText(context, R.string.custom_medicine_name_required, android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String dosage = etDosage.getText().toString().trim();
                    String form = etForm.getText().toString().trim();
                    String activeSubstance = etActiveSubstance.getText().toString().trim();
                    String expDate = etExpiration.getText().toString().trim();
                    String quantity = etQuantity.getText().toString().trim();

                    listener.onSave(
                            name,
                            dosage.isEmpty() ? null : dosage,
                            form.isEmpty() ? null : form,
                            activeSubstance.isEmpty() ? null : activeSubstance,
                            expDate.isEmpty() ? null : expDate,
                            quantity.isEmpty() ? null : quantity
                    );
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}