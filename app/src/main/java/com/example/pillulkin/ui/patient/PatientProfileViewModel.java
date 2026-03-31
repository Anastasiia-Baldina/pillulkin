package com.example.pillulkin.ui.patient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.pillulkin.data.local.entity.PatientProfileEntity;
import com.example.pillulkin.data.repository.PatientProfileRepository;

public class PatientProfileViewModel extends AndroidViewModel {
    private final PatientProfileRepository repository;

    public PatientProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new PatientProfileRepository(application);
    }

    public LiveData<PatientProfileEntity> getProfile() {
        return repository.getProfile();
    }

    public void saveProfile(PatientProfileEntity profile) {
        PatientProfileEntity existing = repository.getProfileSync();
        if (existing != null) {
            profile.setId(existing.getId());
            repository.update(profile);
        } else {
            repository.insert(profile);
        }
    }
}
