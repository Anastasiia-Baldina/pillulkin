package com.example.pillulkin.ui.patient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.repository.DoctorAccessCodeRepository;

public class GenerateCodeViewModel extends AndroidViewModel {
    private final DoctorAccessCodeRepository repository;
    private final MutableLiveData<Boolean> isCodeGenerated = new MutableLiveData<>(false);
    private final MutableLiveData<String> localCode = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLocalMode = new MutableLiveData<>(false);

    public GenerateCodeViewModel(@NonNull Application application) {
        super(application);
        repository = new DoctorAccessCodeRepository(application);
        isLocalMode.postValue(NetworkModule.getInstance(application).isLocalMode());
    }

    public LiveData<String> getGeneratedCode() {
        return repository.getGeneratedCode();
    }

    public LiveData<Boolean> isLoading() {
        return repository.isLoading();
    }

    public LiveData<String> getError() {
        return repository.getError();
    }

    public LiveData<Boolean> isCodeGenerated() {
        return isCodeGenerated;
    }

    public LiveData<String> getLocalCode() {
        return localCode;
    }

    public LiveData<Boolean> isLocalMode() {
        return isLocalMode;
    }

    public void generateCode(int minutes) {
        NetworkModule nm = NetworkModule.getInstance(getApplication());
        if (nm.isLocalMode()) {
            String code = nm.generateLocalDoctorCode();
            localCode.postValue(code);
            isCodeGenerated.postValue(true);
        } else {
            long patientId = nm.getPatientId();
            repository.generateAccessCode(patientId, minutes);
            isCodeGenerated.postValue(true);
        }
    }
}
