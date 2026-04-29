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
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.PrescriptionResponse;
import com.example.pillulkin.databinding.FragmentDoctorPrescriptionsBinding;
import com.example.pillulkin.ui.adapter.PrescriptionAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorPrescriptionsFragment extends Fragment {

    private FragmentDoctorPrescriptionsBinding binding;
    private PrescriptionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDoctorPrescriptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupRecyclerView();
        loadPrescriptions();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            try { Navigation.findNavController(v).popBackStack(); }
            catch (Exception e) { if (getActivity() != null) getActivity().onBackPressed(); }
        });

        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_doctor_medicine) {
                try { Navigation.findNavController(requireView()).popBackStack(); }
                catch (Exception ignored) {}
                return true;
            } else if (id == R.id.action_doctor_symptoms) {
                try { Navigation.findNavController(requireView())
                        .navigate(R.id.action_doctorPrescriptions_to_symptoms); }
                catch (Exception ignored) {}
                return true;
            } else if (id == R.id.action_doctor_logout) {
                NetworkModule.getInstance(requireContext().getApplicationContext()).clearDoctorSession();
                Navigation.findNavController(requireView())
                        .popBackStack(R.id.roleSelectionFragment, false);
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new PrescriptionAdapter(null);
        binding.rvPrescriptions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPrescriptions.setAdapter(adapter);
    }

    private void loadPrescriptions() {
        NetworkModule nm = NetworkModule.getInstance(requireContext().getApplicationContext());
        long patientId = nm.getDoctorPatientId();
        if (patientId <= 0) return;

        nm.getPrescriptions(patientId).enqueue(new Callback<List<PrescriptionResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<PrescriptionResponse>> call,
                                   @NonNull Response<List<PrescriptionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PrescriptionResponse> list = response.body();
                    if (list.isEmpty()) {
                        binding.rvPrescriptions.setVisibility(View.GONE);
                        binding.emptyState.setVisibility(View.VISIBLE);
                    } else {
                        adapter.submitList(list);
                        binding.rvPrescriptions.setVisibility(View.VISIBLE);
                        binding.emptyState.setVisibility(View.GONE);
                    }
                } else {
                    binding.rvPrescriptions.setVisibility(View.GONE);
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PrescriptionResponse>> call,
                                  @NonNull Throwable t) {
                binding.rvPrescriptions.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
