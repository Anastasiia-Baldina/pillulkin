package com.example.pillulkin.ui.patient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.pillulkin.data.local.entity.SymptomEntity;
import com.example.pillulkin.data.repository.SymptomsRepository;

import java.util.List;

public class SymptomsViewModel extends AndroidViewModel {
    private final SymptomsRepository repository;

    public SymptomsViewModel(@NonNull Application application) {
        super(application);
        repository = new SymptomsRepository(application);
    }

    public LiveData<List<SymptomEntity>> getSymptoms() {
        return repository.getAllSymptoms();
    }

    public void addSymptom(String description) {
        SymptomEntity symptom = new SymptomEntity(description, System.currentTimeMillis());
        repository.insert(symptom);
    }

    public void deleteSymptom(SymptomEntity symptom) {
        repository.delete(symptom);
    }
}
