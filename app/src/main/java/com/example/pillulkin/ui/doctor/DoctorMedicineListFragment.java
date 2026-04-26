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
import com.example.pillulkin.databinding.FragmentDoctorMedicineListBinding;
import com.example.pillulkin.ui.adapter.MedicineAdapter;

import java.util.ArrayList;

public class DoctorMedicineListFragment extends Fragment {
    private FragmentDoctorMedicineListBinding binding;
    private DoctorCodeEntryViewModel viewModel;
    private MedicineAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentDoctorMedicineListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(DoctorCodeEntryViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupSearchButton();
        observeData();

        viewModel.loadPatientData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            try {
                Navigation.findNavController(v).popBackStack();
            } catch (Exception e) {
                if (getActivity() != null) getActivity().onBackPressed();
            }
        });

        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_doctor_symptoms) {
                try {
                    Navigation.findNavController(requireView()).navigate(R.id.action_doctorMedicineList_to_symptoms);
                } catch (Exception ignored) {}
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new MedicineAdapter(medicine -> {});

        binding.rvMedicines.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMedicines.setAdapter(adapter);
    }

    private void setupSearchButton() {
        binding.btnSearch.setOnClickListener(v -> {
            try {
                Navigation.findNavController(v).navigate(R.id.action_doctorMedicineList_to_search);
            } catch (Exception ignored) {}
        });
    }

    private void observeData() {
        viewModel.getPatientData().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                viewModel.extractDataFromResponse(data);
                showPatientInfo(data);
            }
        });

        viewModel.getMedicines().observe(getViewLifecycleOwner(), medicines -> {
            if (adapter != null && binding != null) {
                adapter.submitList(medicines);
                boolean empty = medicines == null || medicines.isEmpty();
                binding.rvMedicines.setVisibility(empty ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void showPatientInfo(com.example.pillulkin.data.remote.model.DoctorFullDataResponse data) {
        if (binding == null) return;

        com.example.pillulkin.data.remote.model.PatientProfileResponse profile = data.getProfile();
        boolean hasInfo = false;

        if (profile != null) {
            if (profile.getName() != null && !profile.getName().isEmpty()) {
                binding.tvPatientName.setText(profile.getName());
                binding.tvPatientName.setVisibility(View.VISIBLE);
                hasInfo = true;
            } else {
                binding.tvPatientName.setVisibility(View.GONE);
            }

            if (profile.getAge() != null) {
                binding.tvPatientAge.setText("Возраст: " + profile.getAge());
                binding.tvPatientAge.setVisibility(View.VISIBLE);
                hasInfo = true;
            } else {
                binding.tvPatientAge.setVisibility(View.GONE);
            }

            if (profile.getAllergies() != null && !profile.getAllergies().isEmpty()) {
                binding.tvPatientAllergies.setText("Аллергии: " + profile.getAllergies());
                binding.tvPatientAllergies.setVisibility(View.VISIBLE);
                hasInfo = true;
            } else {
                binding.tvPatientAllergies.setVisibility(View.GONE);
            }

            if (profile.getContraindications() != null && !profile.getContraindications().isEmpty()) {
                binding.tvPatientContraindications.setText("Противопоказания: " + profile.getContraindications());
                binding.tvPatientContraindications.setVisibility(View.VISIBLE);
                hasInfo = true;
            } else {
                binding.tvPatientContraindications.setVisibility(View.GONE);
            }

            if (profile.getNotes() != null && !profile.getNotes().isEmpty()) {
                binding.tvPatientNotes.setText("Заметки: " + profile.getNotes());
                binding.tvPatientNotes.setVisibility(View.VISIBLE);
                hasInfo = true;
            } else {
                binding.tvPatientNotes.setVisibility(View.GONE);
            }
        }

        if (data.getEmail() != null && !data.getEmail().isEmpty()) {
            binding.tvPatientEmail.setText(data.getEmail());
            binding.tvPatientEmail.setVisibility(View.VISIBLE);
            hasInfo = true;
        } else {
            binding.tvPatientEmail.setVisibility(View.GONE);
        }

        binding.patientInfoScroll.setVisibility(hasInfo ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
    }
}
