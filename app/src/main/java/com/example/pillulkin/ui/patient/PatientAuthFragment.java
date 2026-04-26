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
import androidx.navigation.Navigation;

import com.example.pillulkin.R;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.AuthResponse;
import com.example.pillulkin.databinding.FragmentPatientAuthBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientAuthFragment extends Fragment {
    private FragmentPatientAuthBinding binding;
    private NetworkModule networkModule;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private static final String WEB_CLIENT_ID = "796860851053-ajbge1rt3hehhov8t2vi6o66ucrc1osg.apps.googleusercontent.com";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                    @Nullable Bundle savedInstanceState) {
        binding = FragmentPatientAuthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        networkModule = NetworkModule.getInstance(requireContext());

        if (networkModule.isPatientLoggedIn()) {
            navigateToPatient();
            return;
        }

        setupGoogleSignIn();
        setupButtons();
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
                            } else {
                                Toast.makeText(requireContext(), "Ошибка: пустой токен Google", Toast.LENGTH_SHORT).show();
                            }
                        } catch (ApiException e) {
                            Toast.makeText(requireContext(), "Ошибка Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void setupButtons() {
        binding.btnGoogleSignIn.setOnClickListener(v -> {
            googleSignInClient.signOut().addOnCompleteListener(unused -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });

        binding.btnLogin.setOnClickListener(v -> attemptLogin(false));
        binding.btnRegister.setOnClickListener(v -> attemptLogin(true));
    }

    private void authenticateWithBackend(String idToken) {
        binding.progressBar.setVisibility(View.VISIBLE);

        networkModule.loginWithGoogle(idToken).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    networkModule.savePatientToken(auth.getToken());
                    if (auth.getPatientId() != null) {
                        networkModule.savePatientId(auth.getPatientId());
                    }
                    navigateToPatient();
                } else {
                    Toast.makeText(requireContext(), "Ошибка авторизации: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptLogin(boolean isRegister) {
        String email = binding.editEmail.getText().toString().trim();
        String password = binding.editPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);
        binding.btnRegister.setEnabled(false);

        Callback<AuthResponse> callback = new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                binding.btnRegister.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    networkModule.savePatientToken(auth.getToken());
                    if (auth.getPatientId() != null) {
                        networkModule.savePatientId(auth.getPatientId());
                    }
                    navigateToPatient();
                } else {
                    String msg = isRegister ? "Ошибка регистрации: " : "Неверный email или пароль";
                    Toast.makeText(requireContext(), msg + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLogin.setEnabled(true);
                binding.btnRegister.setEnabled(true);
                Toast.makeText(requireContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        if (isRegister) {
            networkModule.register(email, password).enqueue(callback);
        } else {
            networkModule.login(email, password).enqueue(callback);
        }
    }

    private void navigateToPatient() {
        Navigation.findNavController(requireView()).navigate(R.id.action_patientAuth_to_patient);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
