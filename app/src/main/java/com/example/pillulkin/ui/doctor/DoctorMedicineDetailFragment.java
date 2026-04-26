package com.example.pillulkin.ui.doctor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.pillulkin.R;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.PrescriptionRequest;
import com.example.pillulkin.data.remote.model.PrescriptionResponse;
import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;
import com.example.pillulkin.databinding.FragmentDoctorMedicineDetailBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorMedicineDetailFragment extends Fragment {

    private FragmentDoctorMedicineDetailBinding binding;
    private ReferenceMedicineResponse medicine;
    private long patientId;

    private String selectedMealTiming = "";
    private boolean showTimeOffset = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDoctorMedicineDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            long medId = getArguments().getLong("medicineId", -1);
            String medName = getArguments().getString("medicineName", "");
            String dosage = getArguments().getString("dosage", "");
            String form = getArguments().getString("form", "");
            String substance = getArguments().getString("activeSubstance", "");
            patientId = getArguments().getLong("patientId", -1);

            medicine = new ReferenceMedicineResponse();
            medicine.setId(medId);
            medicine.setName(medName);
            medicine.setDosage(dosage);
            medicine.setForm(form);
            medicine.setActiveSubstance(substance);
        }

        setupToolbar();
        setupMedicineInfo();
        setupSpinners();
        setupAddButton();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            try { Navigation.findNavController(v).popBackStack(); }
            catch (Exception e) { if (getActivity() != null) getActivity().onBackPressed(); }
        });
    }

    private void setupMedicineInfo() {
        if (medicine != null) {
            binding.toolbar.setTitle(medicine.getName());
            binding.tvMedicineName.setText(medicine.getName());
            String dosageText = medicine.getDosage();
            if (medicine.getForm() != null && !medicine.getForm().isEmpty()) {
                dosageText += " \u2022 " + medicine.getForm();
            }
            binding.tvDosage.setText(dosageText);
            if (medicine.getActiveSubstance() != null && !medicine.getActiveSubstance().isEmpty()) {
                binding.tvActiveSubstance.setText(
                        getString(R.string.active_substance_label) + " " + medicine.getActiveSubstance());
                binding.tvActiveSubstance.setVisibility(View.VISIBLE);
            } else {
                binding.tvActiveSubstance.setVisibility(View.GONE);
            }
        }
    }

    private void setupSpinners() {
        String[] frequencies = {"", "1 раз в день", "2 раза в день", "3 раза в день", "4 раза в день", "5 раз в день"};
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, frequencies);
        binding.spinnerFrequency.setAdapter(freqAdapter);

        String[] mealTimings = {"", "до еды", "во время еды", "после еды", "независимо от приема пищи"};
        ArrayAdapter<String> mealAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, mealTimings);
        binding.spinnerMealTiming.setAdapter(mealAdapter);

        String[] timeOffsets = {"", "за 1 час", "за 30 минут", "за 15 минут", "через 1 час", "через 30 минут", "через 15 минут"};
        ArrayAdapter<String> offsetAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, timeOffsets);
        binding.spinnerTimeOffset.setAdapter(offsetAdapter);

        binding.spinnerMealTiming.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMealTiming = mealTimings[position];
                boolean showOffset = "до еды".equals(selectedMealTiming) || "после еды".equals(selectedMealTiming);
                binding.tvTimeOffsetLabel.setVisibility(showOffset ? View.VISIBLE : View.GONE);
                binding.spinnerTimeOffset.setVisibility(showOffset ? View.VISIBLE : View.GONE);
                showTimeOffset = showOffset;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupAddButton() {
        binding.btnAddPrescription.setOnClickListener(v -> {
            if (medicine == null || patientId <= 0) return;

            Integer frequency = null;
            int freqPos = binding.spinnerFrequency.getSelectedItemPosition();
            if (freqPos > 0) frequency = freqPos;

            String mealTiming = showTimeOffset ? selectedMealTiming : selectedMealTiming;

            String timeOffset = "";
            if (showTimeOffset && binding.spinnerTimeOffset.getSelectedItemPosition() > 0) {
                timeOffset = (String) binding.spinnerTimeOffset.getSelectedItem();
            }

            String custom = binding.etCustomInstructions.getText() != null
                    ? binding.etCustomInstructions.getText().toString().trim() : "";

            PrescriptionRequest request = new PrescriptionRequest(
                    medicine.getId(), null, frequency, mealTiming,
                    timeOffset.isEmpty() ? null : timeOffset,
                    custom.isEmpty() ? null : custom);

            NetworkModule nm = NetworkModule.getInstance(requireContext().getApplicationContext());
            nm.createPrescription(patientId, request).enqueue(new Callback<PrescriptionResponse>() {
                @Override
                public void onResponse(Call<PrescriptionResponse> call, Response<PrescriptionResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), R.string.prescription_added, Toast.LENGTH_SHORT).show();
                        try { Navigation.findNavController(requireView()).popBackStack(); }
                        catch (Exception ignored) {}
                    } else {
                        Toast.makeText(requireContext(), "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PrescriptionResponse> call, Throwable t) {
                    Toast.makeText(requireContext(), "Ошибка сети", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
