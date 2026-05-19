package com.example.pillulkin.ui.patient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.data.repository.MedicineRepository;

import java.util.List;

public class MedicineListViewModel extends AndroidViewModel {
    private final MedicineRepository repository;

    public MedicineListViewModel(@NonNull Application application) {
        super(application);
        repository = new MedicineRepository(application);
    }

    public LiveData<List<PatientMedicineResponse>> getMedicines() {
        return repository.getPatientMedicines();
    }

    public LiveData<Boolean> isLoading() {
        return repository.isLoading();
    }

    public LiveData<String> getError() {
        return repository.getError();
    }

    public void loadMedicines() {
        repository.loadPatientMedicines();
    }

    public void loadMedicinesFromCache() {
        repository.loadMedicinesFromCache();
    }

    public void deleteMedicine(long medicineId) {
        repository.deletePatientMedicine(medicineId);
    }
}
