package com.example.pillulkin.ui.doctor;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.DiagnosisRequest;
import com.example.pillulkin.data.remote.model.DiagnosisResponse;
import com.example.pillulkin.data.remote.model.DoctorFullDataResponse;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.data.remote.model.PatientSymptomResponse;
import com.example.pillulkin.data.repository.DoctorAccessCodeRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DoctorCodeEntryViewModel extends AndroidViewModel {
    private final DoctorAccessCodeRepository repository;
    private final MutableLiveData<List<PatientMedicineResponse>> medicinesData = new MutableLiveData<>();
    private final MutableLiveData<List<PatientSymptomResponse>> symptomsData = new MutableLiveData<>();
    private final MutableLiveData<DoctorFullDataResponse> patientFullData = new MutableLiveData<>();
    private final MutableLiveData<String> diagnosisResult = new MutableLiveData<>();
    private final MutableLiveData<List<String>> suggestedQuestions = new MutableLiveData<>();
    private List<String> currentSymptoms = new ArrayList<>();

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
        return patientFullData;
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
        repository.getPatientData().observeForever(data -> {
            if (data != null) {
                extractDataFromResponse(data);
            }
        });
    }

    public void extractDataFromResponse(DoctorFullDataResponse data) {
        if (data != null) {
            patientFullData.postValue(data);
            medicinesData.postValue(data.getMedicines() != null ? data.getMedicines() : new ArrayList<>());
            symptomsData.postValue(data.getSymptoms() != null ? data.getSymptoms() : new ArrayList<>());
        }
    }

    public void logout() {
        repository.logout();
    }

    public LiveData<String> getDiagnosisResult() {
        return diagnosisResult;
    }

    public LiveData<List<String>> getSuggestedQuestions() {
        return suggestedQuestions;
    }

    public void diagnoseInitial(List<String> symptoms) {
        currentSymptoms = new ArrayList<>(symptoms);
        DiagnosisRequest request = new DiagnosisRequest(symptoms, "initial", null);
        NetworkModule.getInstance(getApplication()).diagnose(request).enqueue(new Callback<DiagnosisResponse>() {
            @Override
            public void onResponse(Call<DiagnosisResponse> call, Response<DiagnosisResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DiagnosisResponse body = response.body();
                    if (body.getSuggestedQuestions() != null && !body.getSuggestedQuestions().isEmpty()) {
                        suggestedQuestions.postValue(body.getSuggestedQuestions());
                    } else {
                        requestFinalDiagnosis(new ArrayList<>());
                    }
                } else {
                    diagnosisResult.postValue("Ошибка диагностики");
                }
            }

            @Override
            public void onFailure(Call<DiagnosisResponse> call, Throwable t) {
                diagnosisResult.postValue("Ошибка сети: " + t.getMessage());
            }
        });
    }

    public void requestFinalDiagnosis(List<String> answers) {
        DiagnosisRequest request = new DiagnosisRequest(currentSymptoms, "final", answers);
        NetworkModule.getInstance(getApplication()).diagnose(request).enqueue(new Callback<DiagnosisResponse>() {
            @Override
            public void onResponse(Call<DiagnosisResponse> call, Response<DiagnosisResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DiagnosisResponse body = response.body();
                    String text = body.getDiagnosis();
                    if (body.getConfidence() > 0) {
                        text += " (" + Math.round(body.getConfidence() * 100) + "%)";
                    }
                    diagnosisResult.postValue(text);
                } else {
                    diagnosisResult.postValue("Ошибка диагностики");
                }
            }

            @Override
            public void onFailure(Call<DiagnosisResponse> call, Throwable t) {
                diagnosisResult.postValue("Ошибка сети: " + t.getMessage());
            }
        });
    }
}
