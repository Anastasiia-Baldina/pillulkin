package com.example.pillulkin.ui.doctor;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;
import com.example.pillulkin.data.repository.MedicineRepository;

import java.util.Arrays;
import java.util.List;

public class DoctorSearchViewModel extends AndroidViewModel {
    private final MedicineRepository repository;
    private final MutableLiveData<Boolean> isSearching = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public DoctorSearchViewModel(@NonNull Application application) {
        super(application);
        repository = new MedicineRepository(application);
    }

    public LiveData<List<ReferenceMedicineResponse>> getSearchResults() {
        return repository.getSearchResults();
    }

    public LiveData<Boolean> isSearching() {
        return isSearching;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void searchByMedicineName(String query) {
        isSearching.setValue(true);
        repository.searchMedicines(query);
    }

    public void searchBySymptoms(List<String> symptoms) {
        isSearching.setValue(true);
        repository.getRecommendations(symptoms);
    }

    public void searchByDiagnosis(String diagnosis) {
        isSearching.setValue(true);
        List<String> symptoms = Arrays.asList(diagnosis.split("[,;]\\s*"));
        repository.getRecommendations(symptoms);
    }
}
