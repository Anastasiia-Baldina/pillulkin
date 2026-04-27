package com.example.pillulkin.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.DoctorCodeResponse;
import com.example.pillulkin.data.remote.model.DoctorFullDataResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorAccessCodeRepository {
    private final NetworkModule networkModule;
    private final MutableLiveData<String> generatedCode = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<DoctorFullDataResponse> patientData = new MutableLiveData<>();

    public DoctorAccessCodeRepository(Application application) {
        networkModule = NetworkModule.getInstance(application);
    }

    public LiveData<String> getGeneratedCode() {
        return generatedCode;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<DoctorFullDataResponse> getPatientData() {
        return patientData;
    }

    public void generateAccessCode(long patientId, int minutes) {
        isLoading.postValue(true);
        networkModule.generateDoctorCode(patientId, minutes).enqueue(new Callback<DoctorCodeResponse>() {
            @Override
            public void onResponse(Call<DoctorCodeResponse> call, Response<DoctorCodeResponse> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    generatedCode.postValue(response.body().getCode());
                } else {
                    error.postValue("Failed to generate code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<DoctorCodeResponse> call, Throwable t) {
                isLoading.postValue(false);
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void loginAsDoctor(String code) {
        isLoading.postValue(true);
        networkModule.doctorLogin(code).enqueue(new Callback<com.example.pillulkin.data.remote.model.AuthResponse>() {
            @Override
            public void onResponse(Call<com.example.pillulkin.data.remote.model.AuthResponse> call, Response<com.example.pillulkin.data.remote.model.AuthResponse> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    networkModule.saveDoctorToken(response.body().getToken());
                    if (response.body().getPatientId() != null) {
                        networkModule.saveDoctorPatientId(response.body().getPatientId());
                    }
                    networkModule.saveDoctorExpires(System.currentTimeMillis() + 60 * 60 * 1000L);
                    loginSuccess.postValue(true);
                } else {
                    error.postValue("Invalid code");
                }
            }

            @Override
            public void onFailure(Call<com.example.pillulkin.data.remote.model.AuthResponse> call, Throwable t) {
                isLoading.postValue(false);
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void loadPatientFullData() {
        if (!networkModule.isDoctorLoggedIn()) {
            error.postValue("Not authenticated as doctor");
            return;
        }

        isLoading.postValue(true);
        networkModule.getDoctorPatientData().enqueue(new Callback<DoctorFullDataResponse>() {
            @Override
            public void onResponse(Call<DoctorFullDataResponse> call, Response<DoctorFullDataResponse> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    patientData.postValue(response.body());
                } else {
                    error.postValue("Failed to get patient data: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<DoctorFullDataResponse> call, Throwable t) {
                isLoading.postValue(false);
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void logout() {
        networkModule.clearDoctorSession();
    }

    public void notifyLoginSuccess() {
        loginSuccess.postValue(true);
    }
}
