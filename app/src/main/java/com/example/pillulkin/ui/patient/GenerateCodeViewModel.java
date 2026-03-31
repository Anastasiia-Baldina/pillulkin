package com.example.pillulkin.ui.patient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.repository.DoctorAccessCodeRepository;
import com.example.pillulkin.domain.usecase.GenerateAccessCodeUseCase;

public class GenerateCodeViewModel extends AndroidViewModel {
    private final GenerateAccessCodeUseCase generateCodeUseCase;
    private final MutableLiveData<String> generatedCode = new MutableLiveData<>();
    private final MutableLiveData<Boolean> codeGenerated = new MutableLiveData<>(false);

    public GenerateCodeViewModel(@NonNull Application application) {
        super(application);
        DoctorAccessCodeRepository repository = new DoctorAccessCodeRepository(application);
        generateCodeUseCase = new GenerateAccessCodeUseCase(repository);
    }

    public LiveData<String> getGeneratedCode() {
        return generatedCode;
    }

    public LiveData<Boolean> isCodeGenerated() {
        return codeGenerated;
    }

    public void generateCode() {
        String code = generateCodeUseCase.execute();
        generatedCode.setValue(code);
        codeGenerated.setValue(true);
    }
}
