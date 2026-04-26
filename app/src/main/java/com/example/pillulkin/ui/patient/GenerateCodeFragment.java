package com.example.pillulkin.ui.patient;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import com.example.pillulkin.databinding.FragmentGenerateCodeBinding;

public class GenerateCodeFragment extends Fragment {
    private FragmentGenerateCodeBinding binding;
    private GenerateCodeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentGenerateCodeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GenerateCodeViewModel.class);

        setupToolbar();
        setupButtons();
        observeData();
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
            } else if (id == R.id.action_logout) {
                Navigation.findNavController(requireView()).popBackStack(R.id.nav_main, false);
                return true;
            }
            return false;
        });
    }

    private void setupButtons() {
        binding.btnGenerate.setOnClickListener(v -> {
            viewModel.generateCode(60);
        });

        binding.btnCopy.setOnClickListener(v -> {
            String code = viewModel.getGeneratedCode().getValue();
            if (code != null) {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("access_code", code);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), R.string.code_copied, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeData() {
        viewModel.getGeneratedCode().observe(getViewLifecycleOwner(), code -> {
            binding.tvCode.setText(code);
        });

        viewModel.isCodeGenerated().observe(getViewLifecycleOwner(), isGenerated -> {
            binding.cardCode.setVisibility(isGenerated ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
