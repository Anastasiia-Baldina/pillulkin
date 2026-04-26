package com.example.pillulkin.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.entity.CachedMedicine;
import com.example.pillulkin.data.local.entity.PendingOperation;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;
import com.example.pillulkin.sync.SyncManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MedicineRepository {
    private final NetworkModule networkModule;
    private final PillulkinDatabase db;
    private final SyncManager syncManager;
    private final MutableLiveData<List<PatientMedicineResponse>> patientMedicinesData = new MutableLiveData<>();
    private final MutableLiveData<List<ReferenceMedicineResponse>> searchResultsData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public MedicineRepository(Application application) {
        networkModule = NetworkModule.getInstance(application);
        db = PillulkinDatabase.getDatabase(application);
        syncManager = new SyncManager(application);
    }

    public LiveData<List<PatientMedicineResponse>> getPatientMedicines() { return patientMedicinesData; }
    public LiveData<List<ReferenceMedicineResponse>> getSearchResults() { return searchResultsData; }
    public LiveData<Boolean> isLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }

    public void loadPatientMedicines() {
        if (!networkModule.isPatientLoggedIn()) {
            error.postValue("Not authenticated");
            return;
        }
        loadFromCache();
        isLoading.postValue(true);
        networkModule.getMedicines().enqueue(new Callback<List<PatientMedicineResponse>>() {
            @Override
            public void onResponse(Call<List<PatientMedicineResponse>> call, Response<List<PatientMedicineResponse>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<PatientMedicineResponse> meds = response.body();
                    Collections.sort(meds, Comparator.comparing(m -> m.getMedicineName().toLowerCase()));
                    patientMedicinesData.postValue(meds);
                    saveToCache(meds);
                } else {
                    error.postValue("Failed to load medicines: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<PatientMedicineResponse>> call, Throwable t) {
                isLoading.postValue(false);
                if (patientMedicinesData.getValue() == null || patientMedicinesData.getValue().isEmpty()) {
                    error.postValue("Network error: " + t.getMessage());
                }
            }
        });
    }

    private void loadFromCache() {
        long patientId = networkModule.getPatientId();
        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            List<CachedMedicine> cached = db.cachedMedicineDao().getMedicines(patientId);
            if (cached != null && !cached.isEmpty() && patientMedicinesData.getValue() == null) {
                List<PatientMedicineResponse> responses = toResponses(cached);
                Collections.sort(responses, Comparator.comparing(m -> m.getMedicineName().toLowerCase()));
                patientMedicinesData.postValue(responses);
            }
        });
    }

    private void saveToCache(List<PatientMedicineResponse> responses) {
        long patientId = networkModule.getPatientId();
        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            db.cachedMedicineDao().deleteByPatientId(patientId);
            List<CachedMedicine> entities = new ArrayList<>();
            for (PatientMedicineResponse r : responses) {
                CachedMedicine e = new CachedMedicine();
                e.setId(r.getId());
                e.setPatientId(patientId);
                e.setMedicineId(r.getMedicineId());
                e.setMedicineName(r.getMedicineName());
                e.setDosage(r.getDosage());
                e.setForm(r.getForm());
                e.setAddedAt(r.getAddedAt());
                e.setCachedAt(System.currentTimeMillis());
                entities.add(e);
            }
            db.cachedMedicineDao().insertAll(entities);
        });
    }

    private List<PatientMedicineResponse> toResponses(List<CachedMedicine> cached) {
        List<PatientMedicineResponse> list = new ArrayList<>();
        for (CachedMedicine c : cached) {
            PatientMedicineResponse r = new PatientMedicineResponse();
            r.setId(c.getId());
            r.setPatientId(c.getPatientId());
            r.setMedicineId(c.getMedicineId());
            r.setMedicineName(c.getMedicineName());
            r.setDosage(c.getDosage());
            r.setForm(c.getForm());
            r.setAddedAt(c.getAddedAt());
            list.add(r);
        }
        return list;
    }

    public void addPatientMedicine(long medicineId, String expirationDate, String quantity) {
        if (!networkModule.isPatientLoggedIn()) {
            error.postValue("Not authenticated");
            return;
        }
        if (syncManager.isOnline()) {
            isLoading.postValue(true);
            networkModule.addMedicine(medicineId, expirationDate, quantity).enqueue(new Callback<PatientMedicineResponse>() {
                @Override
                public void onResponse(Call<PatientMedicineResponse> call, Response<PatientMedicineResponse> response) {
                    isLoading.postValue(false);
                    if (response.isSuccessful()) {
                        loadPatientMedicines();
                    } else {
                        queueOperation(PendingOperation.TYPE_MEDICINE_ADD, String.valueOf(medicineId));
                    }
                }

                @Override
                public void onFailure(Call<PatientMedicineResponse> call, Throwable t) {
                    isLoading.postValue(false);
                    queueOperation(PendingOperation.TYPE_MEDICINE_ADD, String.valueOf(medicineId));
                }
            });
        } else {
            queueOperation(PendingOperation.TYPE_MEDICINE_ADD, String.valueOf(medicineId));
        }
    }

    public void deletePatientMedicine(long medicineId) {
        if (!networkModule.isPatientLoggedIn()) {
            error.postValue("Not authenticated");
            return;
        }
        if (syncManager.isOnline()) {
            isLoading.postValue(true);
            networkModule.deleteMedicine(medicineId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    isLoading.postValue(false);
                    if (response.isSuccessful()) {
                        loadPatientMedicines();
                    } else {
                        queueOperation(PendingOperation.TYPE_MEDICINE_DELETE, String.valueOf(medicineId));
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    isLoading.postValue(false);
                    queueOperation(PendingOperation.TYPE_MEDICINE_DELETE, String.valueOf(medicineId));
                }
            });
        } else {
            queueOperation(PendingOperation.TYPE_MEDICINE_DELETE, String.valueOf(medicineId));
            List<PatientMedicineResponse> current = patientMedicinesData.getValue();
            if (current != null) {
                List<PatientMedicineResponse> updated = new ArrayList<>();
                for (PatientMedicineResponse m : current) {
                    if (m.getId() != medicineId) updated.add(m);
                }
                patientMedicinesData.postValue(updated);
            }
        }
    }

    public void updatePatientMedicine(long patientMedicineId, String expirationDate, String quantity) {
        if (!networkModule.isPatientLoggedIn()) {
            error.postValue("Not authenticated");
            return;
        }
        isLoading.postValue(true);
        networkModule.updateMedicine(patientMedicineId, expirationDate, quantity).enqueue(new Callback<PatientMedicineResponse>() {
            @Override
            public void onResponse(Call<PatientMedicineResponse> call, Response<PatientMedicineResponse> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    loadPatientMedicines();
                } else {
                    error.postValue("Failed to update medicine: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PatientMedicineResponse> call, Throwable t) {
                isLoading.postValue(false);
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    private void queueOperation(String type, String payload) {
        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            PendingOperation op = new PendingOperation();
            op.setType(type);
            op.setPayload(payload);
            op.setStatus(PendingOperation.STATUS_PENDING);
            op.setRetryCount(0);
            op.setCreatedAt(System.currentTimeMillis());
            db.pendingOperationDao().insert(op);
        });
    }

    public void searchMedicines(String query) {
        isLoading.postValue(true);
        networkModule.searchMedicines(query).enqueue(new Callback<List<ReferenceMedicineResponse>>() {
            @Override
            public void onResponse(Call<List<ReferenceMedicineResponse>> call, Response<List<ReferenceMedicineResponse>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    searchResultsData.postValue(response.body());
                } else {
                    error.postValue("Search failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ReferenceMedicineResponse>> call, Throwable t) {
                isLoading.postValue(false);
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }

    public void getRecommendations(List<String> symptoms) {
        isLoading.postValue(true);
        networkModule.getRecommendations(symptoms).enqueue(new Callback<List<ReferenceMedicineResponse>>() {
            @Override
            public void onResponse(Call<List<ReferenceMedicineResponse>> call, Response<List<ReferenceMedicineResponse>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    searchResultsData.postValue(response.body());
                } else {
                    error.postValue("Recommendations failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ReferenceMedicineResponse>> call, Throwable t) {
                isLoading.postValue(false);
                error.postValue("Network error: " + t.getMessage());
            }
        });
    }
}
