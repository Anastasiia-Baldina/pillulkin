package com.example.pillulkin.ui.patient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.pillulkin.data.remote.model.PatientProfileResponse;
import com.example.pillulkin.data.repository.PatientProfileRepository;

public class PatientProfileViewModel extends AndroidViewModel {
    private final PatientProfileRepository repository;

    public PatientProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new PatientProfileRepository(application);
    }

    public LiveData<PatientProfileResponse> getProfile() {
        return repository.getProfile();
    }

    public LiveData<Boolean> isLoading() {
        return repository.isLoading();
    }

    public LiveData<String> getError() {
        return repository.getError();
    }

    public void loadProfile() {
        repository.loadProfile();
    }

    public void saveProfile(String name, Integer age, String allergies, String contraindications, String notes) {
        repository.saveProfile(name, age, allergies, contraindications, notes);
    }

    public void logout() {
        repository.logout();
    }
}
