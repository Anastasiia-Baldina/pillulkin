package com.example.pillulkin.ui.patient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.DiagnosisRequest;
import com.example.pillulkin.data.remote.model.DiagnosisResponse;
import com.example.pillulkin.data.remote.model.PatientSymptomResponse;
import com.example.pillulkin.data.repository.SymptomsRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SymptomsViewModel extends AndroidViewModel {
    private final SymptomsRepository repository;
    private final MutableLiveData<String> diagnosisResult = new MutableLiveData<>();
    private final MutableLiveData<List<String>> suggestedQuestions = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingDiagnosis = new MutableLiveData<>(false);
    private List<String> currentSymptoms = new ArrayList<>();

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

    public LiveData<String> getDiagnosisResult() {
        return diagnosisResult;
    }

    public LiveData<List<String>> getSuggestedQuestions() {
        return suggestedQuestions;
    }

    public LiveData<Boolean> isLoadingDiagnosis() {
        return isLoadingDiagnosis;
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

    public void diagnoseInitial(List<String> symptoms) {
        currentSymptoms = new ArrayList<>(symptoms);
        isLoadingDiagnosis.postValue(true);
        DiagnosisRequest request = new DiagnosisRequest(symptoms, "initial", null);
        NetworkModule.getInstance(getApplication()).diagnose(request).enqueue(new Callback<DiagnosisResponse>() {
            @Override
            public void onResponse(Call<DiagnosisResponse> call, Response<DiagnosisResponse> response) {
                isLoadingDiagnosis.postValue(false);
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
                isLoadingDiagnosis.postValue(false);
                diagnosisResult.postValue("Ошибка сети: " + t.getMessage());
            }
        });
    }

    public void requestFinalDiagnosis(List<String> answers) {
        isLoadingDiagnosis.postValue(true);
        DiagnosisRequest request = new DiagnosisRequest(currentSymptoms, "final", answers);
        NetworkModule.getInstance(getApplication()).diagnose(request).enqueue(new Callback<DiagnosisResponse>() {
            @Override
            public void onResponse(Call<DiagnosisResponse> call, Response<DiagnosisResponse> response) {
                isLoadingDiagnosis.postValue(false);
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
                isLoadingDiagnosis.postValue(false);
                diagnosisResult.postValue("Ошибка сети: " + t.getMessage());
            }
        });
    }
}
