package com.example.pillulkin.ui.patient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.pillulkin.data.remote.model.PatientSymptomResponse;
import com.example.pillulkin.data.repository.SymptomsRepository;

import java.util.List;

public class SymptomsViewModel extends AndroidViewModel {
    private final SymptomsRepository repository;

    public SymptomsViewModel(@NonNull Application application) {
        super(application);
        repository = new SymptomsRepository(application);
    }

    public LiveData<List<PatientSymptomResponse>> getSymptoms() {
        return repository.getAllSymptoms();
    }

    public LiveData<Boolean> isLoading() {
        return repository.isLoading();
    }

    public LiveData<String> getError() {
        return repository.getError();
    }

    public void loadSymptoms() {
        repository.loadSymptoms();
    }

    public void addSymptom(String symptom) {
        repository.addSymptom(symptom);
    }

    public void deleteSymptom(long symptomId) {
        repository.deleteSymptom(symptomId);
    }

    public void renewSymptom(long symptomId) {
        repository.renewSymptom(symptomId);
    }
}
