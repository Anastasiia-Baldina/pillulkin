package com.example.pillulkin.ui.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pillulkin.R;
import com.example.pillulkin.databinding.FragmentMedicineListBinding;
import com.example.pillulkin.ui.adapter.MedicineAdapter;
import com.example.pillulkin.utils.DateUtils;

public class MedicineListFragment extends Fragment {
    private FragmentMedicineListBinding binding;
    private MedicineListViewModel viewModel;
    private MedicineAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentMedicineListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MedicineListViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupSortButton();
        setupFab();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_symptoms) {
                Navigation.findNavController(requireView()).navigate(R.id.action_medicineList_to_symptoms);
                return true;
            } else if (item.getItemId() == R.id.action_profile) {
                Navigation.findNavController(requireView()).navigate(R.id.action_medicineList_to_profile);
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new MedicineAdapter(medicine -> {
            Bundle args = new Bundle();
            args.putLong("medicineId", medicine.getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_medicineList_to_medicineDetail, args);
        }, medicine -> DateUtils.getExpirationStatus(medicine.getExpirationDate()));

        binding.rvMedicines.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMedicines.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    viewModel.searchMedicines(s.toString()).observe(getViewLifecycleOwner(), medicines -> {
                        adapter.submitList(medicines);
                        updateEmptyState(medicines.isEmpty());
                    });
                } else {
                    observeData();
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void setupSortButton() {
        binding.btnSort.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            popup.getMenu().add(0, 1, 0, R.string.sort_by_name);
            popup.getMenu().add(0, 2, 1, R.string.sort_by_date);
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    viewModel.setSortOrder("name");
                } else {
                    viewModel.setSortOrder("expiration");
                }
                observeData();
                return true;
            });
            popup.show();
        });
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_medicineList_to_addMedicine);
        });
    }

    private void observeData() {
        viewModel.getMedicines().observe(getViewLifecycleOwner(), medicines -> {
            adapter.submitList(medicines);
            updateEmptyState(medicines.isEmpty());
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvMedicines.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
