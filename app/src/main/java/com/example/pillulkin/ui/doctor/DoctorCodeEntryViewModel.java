package com.example.pillulkin.ui.doctor;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.repository.DoctorAccessCodeRepository;
import com.example.pillulkin.domain.usecase.ValidateAccessCodeUseCase;

public class DoctorCodeEntryViewModel extends AndroidViewModel {
    private final ValidateAccessCodeUseCase validateCodeUseCase;
    private final MutableLiveData<ValidateAccessCodeUseCase.ValidationResult> validationResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> accessGranted = new MutableLiveData<>(false);

    public DoctorCodeEntryViewModel(@NonNull Application application) {
        super(application);
        DoctorAccessCodeRepository repository = new DoctorAccessCodeRepository(application);
        validateCodeUseCase = new ValidateAccessCodeUseCase(repository);
    }

    public LiveData<ValidateAccessCodeUseCase.ValidationResult> getValidationResult() {
        return validationResult;
    }

    public LiveData<Boolean> isAccessGranted() {
        return accessGranted;
    }

    public void validateCode(String code) {
        ValidateAccessCodeUseCase.ValidationResult result = validateCodeUseCase.execute(code);
        validationResult.setValue(result);
        accessGranted.setValue(result.isValid());
    }
}
