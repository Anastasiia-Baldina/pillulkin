package com.example.pillulkin.ui.doctor;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.repository.RecommendationRepository;
import com.example.pillulkin.domain.model.RecommendationResult;
import com.example.pillulkin.domain.usecase.GetMedicineByDiagnosisUseCase;
import com.example.pillulkin.domain.usecase.GetMedicineRecommendationsUseCase;

public class DoctorSearchViewModel extends AndroidViewModel {
    private final GetMedicineRecommendationsUseCase getMedicineRecommendationsUseCase;
    private final GetMedicineByDiagnosisUseCase getMedicineByDiagnosisUseCase;
    private final MutableLiveData<RecommendationResult> searchResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSearching = new MutableLiveData<>(false);

    public DoctorSearchViewModel(@NonNull Application application) {
        super(application);
        RecommendationRepository repository = new RecommendationRepository(application);
        getMedicineRecommendationsUseCase = new GetMedicineRecommendationsUseCase(repository);
        getMedicineByDiagnosisUseCase = new GetMedicineByDiagnosisUseCase(repository);
    }

    public LiveData<RecommendationResult> getSearchResult() {
        return searchResult;
    }

    public LiveData<Boolean> isSearching() {
        return isSearching;
    }

    public void searchByMedicineName(String medicineName) {
        isSearching.setValue(true);
        RecommendationResult result = getMedicineRecommendationsUseCase.execute(medicineName);
        searchResult.setValue(result);
        isSearching.setValue(false);
    }

    public void searchByDiagnosis(String diagnosis) {
        isSearching.setValue(true);
        RecommendationResult result = getMedicineByDiagnosisUseCase.execute(diagnosis);
        searchResult.setValue(result);
        isSearching.setValue(false);
    }
}
