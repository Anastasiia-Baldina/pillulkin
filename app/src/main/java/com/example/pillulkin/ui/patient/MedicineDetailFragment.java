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
import com.example.pillulkin.data.local.entity.MedicineEntity;
import com.example.pillulkin.databinding.FragmentMedicineDetailBinding;
import com.example.pillulkin.utils.DateUtils;

public class MedicineDetailFragment extends Fragment {
    private FragmentMedicineDetailBinding binding;
    private AddEditMedicineViewModel viewModel;
    private long medicineId;
    private MedicineEntity currentMedicine;

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
        viewModel = new ViewModelProvider(this).get(AddEditMedicineViewModel.class);

        if (getArguments() != null) {
            medicineId = getArguments().getLong("medicineId");
        }

        setupToolbar();
        setupButtons();
        loadMedicine();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });
    }

    private void setupButtons() {
        binding.btnEdit.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong("medicineId", medicineId);
            Navigation.findNavController(v).navigate(R.id.editMedicineFragment, args);
        });

        binding.btnDelete.setOnClickListener(v -> {
            if (currentMedicine != null) {
                viewModel.deleteMedicine(currentMedicine);
                Toast.makeText(requireContext(), R.string.success_deleted, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(v).popBackStack();
            }
        });
    }

    private void loadMedicine() {
        viewModel.getMedicine(medicineId).observe(getViewLifecycleOwner(), medicine -> {
            if (medicine != null) {
                currentMedicine = medicine;
                binding.tvName.setText(medicine.getName());
                binding.tvDosage.setText(getString(R.string.medicine_dosage) + ": " + medicine.getDosage());
                binding.tvForm.setText(getString(R.string.medicine_form) + ": " + medicine.getForm());
                binding.tvExpiration.setText(getString(R.string.medicine_expiration) + ": " + medicine.getExpirationDate());
                binding.tvQuantity.setText(getString(R.string.medicine_quantity) + ": " + medicine.getQuantity());

                if (medicine.getComment() != null && !medicine.getComment().isEmpty()) {
                    binding.tvComment.setVisibility(View.VISIBLE);
                    binding.tvComment.setText(getString(R.string.medicine_comment) + ": " + medicine.getComment());
                } else {
                    binding.tvComment.setVisibility(View.GONE);
                }

                String status = DateUtils.getExpirationStatus(medicine.getExpirationDate());
                if ("EXPIRED".equals(status)) {
                    binding.tvStatus.setText(R.string.medicine_status_expired);
                    binding.tvStatus.getBackground().setTint(requireContext().getColor(R.color.expired));
                    binding.tvStatus.setTextColor(requireContext().getColor(R.color.on_error));
                } else if ("EXPIRING_SOON".equals(status)) {
                    binding.tvStatus.setText(R.string.medicine_status_expiring_soon);
                    binding.tvStatus.getBackground().setTint(requireContext().getColor(R.color.warning));
                } else {
                    binding.tvStatus.setText(R.string.medicine_status_valid);
                    binding.tvStatus.getBackground().setTint(requireContext().getColor(R.color.valid));
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
