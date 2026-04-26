package com.example.pillulkin.ui.doctor;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.remote.model.DoctorFullDataResponse;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.data.remote.model.PatientSymptomResponse;
import com.example.pillulkin.data.repository.DoctorAccessCodeRepository;

import java.util.ArrayList;
import java.util.List;

public class DoctorCodeEntryViewModel extends AndroidViewModel {
    private final DoctorAccessCodeRepository repository;
    private final MutableLiveData<List<PatientMedicineResponse>> medicinesData = new MutableLiveData<>();
    private final MutableLiveData<List<PatientSymptomResponse>> symptomsData = new MutableLiveData<>();

    public DoctorCodeEntryViewModel(@NonNull Application application) {
        super(application);
        repository = new DoctorAccessCodeRepository(application);
    }

    public LiveData<Boolean> isLoading() {
        return repository.isLoading();
    }

    public LiveData<String> getError() {
        return repository.getError();
    }

    public LiveData<Boolean> getLoginSuccess() {
        return repository.getLoginSuccess();
    }

    public LiveData<DoctorFullDataResponse> getPatientData() {
        return repository.getPatientData();
    }

    public LiveData<List<PatientMedicineResponse>> getMedicines() {
        return medicinesData;
    }

    public LiveData<List<PatientSymptomResponse>> getSymptoms() {
        return symptomsData;
    }

    public void loginWithCode(String code) {
        repository.loginAsDoctor(code);
    }

    public void loadPatientData() {
        repository.loadPatientFullData();
    }

    public void extractDataFromResponse(DoctorFullDataResponse data) {
        if (data != null) {
            medicinesData.postValue(data.getMedicines() != null ? data.getMedicines() : new ArrayList<>());
            symptomsData.postValue(data.getSymptoms() != null ? data.getSymptoms() : new ArrayList<>());
        }
    }

    public void logout() {
        repository.logout();
    }
}
