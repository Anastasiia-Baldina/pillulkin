package com.example.pillulkin.ui.patient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;
import com.example.pillulkin.data.repository.MedicineRepository;

import java.util.Arrays;
import java.util.List;

public class AddEditMedicineViewModel extends AndroidViewModel {
    private final MedicineRepository repository;
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private boolean isSymptomMode = false;

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

    public boolean isSymptomMode() {
        return isSymptomMode;
    }

    public void setSymptomMode(boolean symptomMode) {
        isSymptomMode = symptomMode;
    }

    public void searchMedicines(String query) {
        if (isSymptomMode) {
            List<String> symptoms = Arrays.asList(query.split("[,;]\\s*"));
            repository.getRecommendations(symptoms);
        } else {
            repository.searchMedicines(query);
        }
    }

    public void addMedicine(long medicineId, String expirationDate, String quantity,
                            String medicineName, String dosage, String form, String activeSubstance) {
        repository.addPatientMedicine(medicineId, expirationDate, quantity, medicineName, dosage, form, activeSubstance);
        saveSuccess.postValue(true);
    }

    public void addCustomMedicine(String name, String dosage, String form,
                                  String activeSubstance, String indications,
                                  String contraindications, String expirationDate,
                                  String quantity) {
        repository.addCustomMedicine(name, dosage, form, activeSubstance, indications, contraindications, expirationDate, quantity);
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
