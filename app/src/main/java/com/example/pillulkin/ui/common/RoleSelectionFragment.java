package com.example.pillulkin.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.pillulkin.R;
import com.example.pillulkin.databinding.FragmentRoleSelectionBinding;

public class RoleSelectionFragment extends Fragment {
    private FragmentRoleSelectionBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentRoleSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.cardPatient.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_roleSelection_to_patient);
        });

        binding.cardDoctor.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_roleSelection_to_doctor);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
