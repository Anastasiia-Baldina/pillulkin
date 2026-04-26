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

import com.example.pillulkin.R;
import com.example.pillulkin.databinding.FragmentMedicineDetailBinding;

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
            } else if (id == R.id.action_logout) {
                Navigation.findNavController(requireView()).popBackStack(R.id.nav_main, false);
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
            binding.tvQuantity.setText("Количество: " + quantity);
            binding.tvQuantity.setVisibility(View.VISIBLE);
        } else {
            binding.tvQuantity.setVisibility(View.GONE);
        }

        binding.tvStatus.setVisibility(View.GONE);
        binding.tvComment.setVisibility(View.GONE);
    }

    private void setupButtons() {
        binding.btnEdit.setVisibility(View.GONE);

        if (getArguments() != null) {
            long medicineId = getArguments().getLong("medicineId", -1);
            binding.btnDelete.setOnClickListener(v -> {
                AddEditMedicineViewModel viewModel = new ViewModelProvider(this).get(AddEditMedicineViewModel.class);
                viewModel.deleteMedicine(medicineId);
                Toast.makeText(requireContext(), R.string.success_deleted, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(v).popBackStack();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
