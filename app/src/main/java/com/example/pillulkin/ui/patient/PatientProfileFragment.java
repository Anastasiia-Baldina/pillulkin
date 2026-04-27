package com.example.pillulkin.ui.patient;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.pillulkin.R;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.AuthResponse;
import com.example.pillulkin.data.remote.model.PatientProfileResponse;
import com.example.pillulkin.databinding.FragmentPatientProfileBinding;
import com.example.pillulkin.sync.LocalSyncHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientProfileFragment extends Fragment {
    private FragmentPatientProfileBinding binding;
    private PatientProfileViewModel viewModel;
    private NetworkModule networkModule;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private static final String WEB_CLIENT_ID = "796860851053-ajbge1rt3hehhov8t2vi6o66ucrc1osg.apps.googleusercontent.com";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        binding = FragmentPatientProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PatientProfileViewModel.class);
        networkModule = NetworkModule.getInstance(requireContext());

        setupToolbar();
        setupSaveButton();
        setupPrescriptionsButton();
        setupAuthStatus();
        loadProfile();
    }

    private void setupAuthStatus() {
        if (networkModule.isLocalMode()) {
            binding.tvAuthStatus.setText(R.string.local_mode_label);
            binding.tvAuthStatus.setVisibility(View.VISIBLE);
            binding.btnGoogleSignIn.setVisibility(View.VISIBLE);
            binding.tvGooglePrompt.setVisibility(View.VISIBLE);
            setupGoogleSignIn();
        } else {
            binding.tvAuthStatus.setVisibility(View.GONE);
            binding.btnGoogleSignIn.setVisibility(View.GONE);
            binding.tvGooglePrompt.setVisibility(View.GONE);
        }
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        try {
                            var task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account.getIdToken() != null) {
                                authenticateWithBackend(account.getIdToken());
                            }
                        } catch (ApiException e) {
                            Toast.makeText(requireContext(), "Ошибка Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        binding.btnGoogleSignIn.setOnClickListener(v -> {
            googleSignInClient.signOut().addOnCompleteListener(unused -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });
    }

    private void authenticateWithBackend(String idToken) {
        networkModule.loginWithGoogle(idToken).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    networkModule.savePatientToken(auth.getToken());
                    if (auth.getPatientId() != null) {
                        networkModule.savePatientId(auth.getPatientId());
                    }

                    if (networkModule.isLocalMode()) {
                        networkModule.clearLocalMode();
                        LocalSyncHelper.syncLocalToServer(requireContext().getApplicationContext(), auth.getPatientId());
                    }

                    binding.tvAuthStatus.setVisibility(View.GONE);
                    binding.btnGoogleSignIn.setVisibility(View.GONE);
                    binding.tvGooglePrompt.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), R.string.success_saved, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Ошибка авторизации: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
            } else if (id == R.id.action_generate_code) {
                Navigation.findNavController(requireView()).navigate(R.id.action_medicineList_to_generateCode);
                return true;
            } else if (id == R.id.action_notifications) {
                Navigation.findNavController(requireView()).navigate(R.id.action_medicineList_to_notifications);
                return true;
            } else if (id == R.id.action_logout) {
                showLogoutDialog();
                return true;
            }
            return false;
        });
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            String ageStr = binding.etAge.getText().toString().trim();
            Integer age = null;
            if (!ageStr.isEmpty()) {
                try {
                    age = Integer.parseInt(ageStr);
                } catch (NumberFormatException e) {
                    binding.etAge.setError(getString(R.string.validation_invalid_age));
                    return;
                }
            }

            String allergies = binding.etAllergies.getText().toString().trim();
            String contraindications = binding.etContraindications.getText().toString().trim();
            String notes = binding.etNotes.getText().toString().trim();

            viewModel.saveProfile(name, age, allergies, contraindications, notes);
            Toast.makeText(requireContext(), R.string.success_saved, Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).popBackStack();
        });
    }

    private void setupPrescriptionsButton() {
        binding.btnPrescriptions.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profile_to_prescriptions);
        });
    }

    private void loadProfile() {
        viewModel.loadProfile();
        viewModel.getProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                if (profile.getName() != null) binding.etName.setText(profile.getName());
                if (profile.getAge() != null) binding.etAge.setText(String.valueOf(profile.getAge()));
                if (profile.getAllergies() != null) binding.etAllergies.setText(profile.getAllergies());
                if (profile.getContraindications() != null) binding.etContraindications.setText(profile.getContraindications());
                if (profile.getNotes() != null) binding.etNotes.setText(profile.getNotes());
            }
        });
    }

    private void showLogoutDialog() {
        String[] options = {getString(R.string.logout_switch_profile), getString(R.string.logout_sign_out)};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_dialog_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Navigation.findNavController(requireView()).popBackStack(R.id.roleSelectionFragment, false);
                    } else {
                        NetworkModule.getInstance(requireContext().getApplicationContext()).clearPatientSession();
                        Navigation.findNavController(requireView()).popBackStack(R.id.roleSelectionFragment, false);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
