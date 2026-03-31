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
import com.example.pillulkin.databinding.FragmentDoctorMedicineListBinding;
import com.example.pillulkin.ui.adapter.MedicineAdapter;
import com.example.pillulkin.ui.patient.MedicineListViewModel;
import com.example.pillulkin.utils.DateUtils;

public class DoctorMedicineListFragment extends Fragment {
    private FragmentDoctorMedicineListBinding binding;
    private MedicineListViewModel viewModel;
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
        
        try {
            viewModel = new ViewModelProvider(this).get(MedicineListViewModel.class);
            setupToolbar();
            setupRecyclerView();
            setupSearchButton();
            observeData();
        } catch (Exception e) {
            if (binding != null) {
                binding.rvMedicines.setVisibility(View.GONE);
            }
        }
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            try {
                Navigation.findNavController(v).popBackStack();
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_doctor_medicine) {
                return true;
            } else if (item.getItemId() == R.id.action_doctor_symptoms) {
                try {
                    Navigation.findNavController(requireView()).navigate(R.id.action_doctorMedicineList_to_symptoms);
                } catch (Exception e) {
                }
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        if (binding == null || getContext() == null) {
            return;
        }
        
        adapter = new MedicineAdapter(medicine -> {
        }, medicine -> {
            try {
                return DateUtils.getExpirationStatus(medicine.getExpirationDate());
            } catch (Exception e) {
                return "VALID";
            }
        });

        binding.rvMedicines.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMedicines.setAdapter(adapter);
    }

    private void setupSearchButton() {
        if (binding == null) {
            return;
        }
        
        binding.btnSearch.setOnClickListener(v -> {
            try {
                Navigation.findNavController(v).navigate(R.id.action_doctorMedicineList_to_search);
            } catch (Exception e) {
            }
        });
    }

    private void observeData() {
        if (viewModel == null) {
            return;
        }
        
        viewModel.getMedicines().observe(getViewLifecycleOwner(), medicines -> {
            if (adapter != null && binding != null) {
                adapter.submitList(medicines);
                if (medicines == null || medicines.isEmpty()) {
                    binding.rvMedicines.setVisibility(View.GONE);
                } else {
                    binding.rvMedicines.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapter = null;
    }
}
