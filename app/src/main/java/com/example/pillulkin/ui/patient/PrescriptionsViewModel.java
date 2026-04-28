package com.example.pillulkin.ui.patient;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.PrescriptionResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrescriptionsViewModel extends AndroidViewModel {

    private final NetworkModule networkModule;
    private final MutableLiveData<List<PrescriptionResponse>> prescriptions = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public PrescriptionsViewModel(@NonNull Application application) {
        super(application);
        networkModule = NetworkModule.getInstance(application);
    }

    public LiveData<List<PrescriptionResponse>> getPrescriptions() { return prescriptions; }
    public LiveData<Boolean> isLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }

    public void loadPrescriptions() {
        long patientId = networkModule.getPatientId();
        if (patientId <= 0) return;
        isLoading.postValue(true);
        networkModule.getPrescriptions(patientId).enqueue(new Callback<List<PrescriptionResponse>>() {
            @Override
            public void onResponse(Call<List<PrescriptionResponse>> call, Response<List<PrescriptionResponse>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    prescriptions.postValue(response.body());
                } else {
                    error.postValue("Ошибка: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<PrescriptionResponse>> call, Throwable t) {
                isLoading.postValue(false);
                error.postValue("Ошибка сети");
            }
        });
    }

    public void moveToCabinet(PrescriptionResponse prescription) {
        long patientId = networkModule.getPatientId();
        networkModule.moveToCabinet(patientId, prescription.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadPrescriptions();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}
