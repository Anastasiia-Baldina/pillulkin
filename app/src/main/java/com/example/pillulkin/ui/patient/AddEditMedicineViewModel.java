package com.example.pillulkin.ui.patient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;
import com.example.pillulkin.data.repository.MedicineRepository;

import java.util.List;

public class AddEditMedicineViewModel extends AndroidViewModel {
    private final MedicineRepository repository;
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public AddEditMedicineViewModel(@NonNull Application application) {
        super(application);
        repository = new MedicineRepository(application);
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<List<ReferenceMedicineResponse>> getSearchResults() {
        return repository.getSearchResults();
    }

    public void searchMedicines(String query) {
        repository.searchMedicines(query);
    }

    public void addMedicine(long medicineId, String expirationDate, String quantity) {
        repository.addPatientMedicine(medicineId, expirationDate, quantity);
        saveSuccess.postValue(true);
    }

    public void updateMedicine(long patientMedicineId, String expirationDate, String quantity) {
        repository.updatePatientMedicine(patientMedicineId, expirationDate, quantity);
        saveSuccess.postValue(true);
    }

    public void deleteMedicine(long medicineId) {
        repository.deletePatientMedicine(medicineId);
    }
}
