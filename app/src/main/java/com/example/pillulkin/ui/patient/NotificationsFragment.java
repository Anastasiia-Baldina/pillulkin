package com.example.pillulkin.ui.patient;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pillulkin.R;
import com.example.pillulkin.data.local.entity.Reminder;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.databinding.FragmentNotificationsBinding;
import com.example.pillulkin.ui.adapter.ReminderAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotificationsViewModel viewModel;
    private ReminderAdapter adapter;
    private List<PatientMedicineResponse> patientMedicines = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable android.view.ViewGroup container,
                             @Nullable android.os.Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable android.os.Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupFab();
        observeData();
        requestNotificationPermission();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            try { Navigation.findNavController(v).popBackStack(); }
            catch (Exception e) { if (getActivity() != null) getActivity().onBackPressed(); }
        });
    }

    private void setupRecyclerView() {
        adapter = new ReminderAdapter(new ReminderAdapter.OnReminderActionListener() {
            @Override
            public void onToggle(Reminder reminder) {
                viewModel.toggleReminder(reminder);
            }

            @Override
            public void onDelete(Reminder reminder) {
                viewModel.deleteReminder(reminder);
            }

            @Override
            public void onClick(Reminder reminder) {
                showEditDialog(reminder);
            }
        });
        binding.rvReminders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvReminders.setAdapter(adapter);
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> showAddDialog());
    }

    private List<String> buildMedicineNames() {
        List<String> names = new ArrayList<>();
        names.add("— Не выбрано —");
        for (PatientMedicineResponse med : patientMedicines) {
            if (med.getMedicineName() != null) names.add(med.getMedicineName());
        }
        return names;
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_reminder, null);

        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        Spinner spinnerMedicine = dialogView.findViewById(R.id.spinnerMedicine);
        List<String> medicineNames = buildMedicineNames();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, medicineNames);
        spinnerMedicine.setAdapter(spinnerAdapter);

        EditText etCustomText = dialogView.findViewById(R.id.etCustomText);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.notification_add_title)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();

                    int selectedPos = spinnerMedicine.getSelectedItemPosition();
                    String medicineName = selectedPos > 0 ? medicineNames.get(selectedPos) : null;

                    String customText = etCustomText.getText() != null
                            ? etCustomText.getText().toString().trim() : "";
                    if (customText.isEmpty()) customText = null;

                    viewModel.addReminder(hour, minute, medicineName, customText);
                    Toast.makeText(requireContext(), R.string.notification_added, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showEditDialog(Reminder reminder) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_reminder, null);

        TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setHour(reminder.getHour());
        timePicker.setMinute(reminder.getMinute());

        Spinner spinnerMedicine = dialogView.findViewById(R.id.spinnerMedicine);
        List<String> medicineNames = buildMedicineNames();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, medicineNames);
        spinnerMedicine.setAdapter(spinnerAdapter);

        if (reminder.getMedicineName() != null) {
            for (int i = 1; i < medicineNames.size(); i++) {
                if (medicineNames.get(i).equals(reminder.getMedicineName())) {
                    spinnerMedicine.setSelection(i);
                    break;
                }
            }
        }

        EditText etCustomText = dialogView.findViewById(R.id.etCustomText);
        if (reminder.getCustomText() != null) {
            etCustomText.setText(reminder.getCustomText());
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.notification_edit_title)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    int hour = timePicker.getHour();
                    int minute = timePicker.getMinute();

                    int selectedPos = spinnerMedicine.getSelectedItemPosition();
                    String medicineName = selectedPos > 0 ? medicineNames.get(selectedPos) : null;

                    String customText = etCustomText.getText() != null
                            ? etCustomText.getText().toString().trim() : "";
                    if (customText.isEmpty()) customText = null;

                    viewModel.updateReminder(reminder, hour, minute, medicineName, customText);
                    Toast.makeText(requireContext(), R.string.notification_updated, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void observeData() {
        viewModel.getReminders().observe(getViewLifecycleOwner(), reminders -> {
            if (reminders != null && !reminders.isEmpty()) {
                adapter.submitList(reminders);
                binding.rvReminders.setVisibility(View.VISIBLE);
                binding.emptyState.setVisibility(View.GONE);
            } else {
                binding.rvReminders.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getMedicines().observe(getViewLifecycleOwner(), meds -> {
            if (meds != null) {
                patientMedicines = meds;
            }
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
