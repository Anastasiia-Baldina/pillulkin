package com.example.pillulkin.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.entity.CachedSymptom;
import com.example.pillulkin.data.local.entity.PendingOperation;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.PatientSymptomResponse;
import com.example.pillulkin.sync.SyncManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SymptomsRepository {
    private final NetworkModule networkModule;
    private final PillulkinDatabase db;
    private final SyncManager syncManager;
    private final MutableLiveData<List<PatientSymptomResponse>> symptomsData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public SymptomsRepository(Application application) {
        networkModule = NetworkModule.getInstance(application);
        db = PillulkinDatabase.getDatabase(application);
        syncManager = new SyncManager(application);
    }

    public LiveData<List<PatientSymptomResponse>> getAllSymptoms() { return symptomsData; }
    public LiveData<Boolean> isLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }

    public void loadSymptoms() {
        if (!networkModule.isPatientLoggedIn()) {
            error.postValue("Not authenticated");
            return;
        }
        loadFromCache();
        isLoading.postValue(true);
        networkModule.getSymptoms().enqueue(new Callback<List<PatientSymptomResponse>>() {
            @Override
            public void onResponse(Call<List<PatientSymptomResponse>> call, Response<List<PatientSymptomResponse>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    symptomsData.postValue(response.body());
                    saveToCache(response.body());
                } else {
                    error.postValue("Failed to load symptoms: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<PatientSymptomResponse>> call, Throwable t) {
                isLoading.postValue(false);
                if (symptomsData.getValue() == null || symptomsData.getValue().isEmpty()) {
                    error.postValue("Network error: " + t.getMessage());
                }
            }
        });
    }

    private void loadFromCache() {
        long patientId = networkModule.getPatientId();
        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            List<CachedSymptom> cached = db.cachedSymptomDao().getSymptoms(patientId);
            if (cached != null && !cached.isEmpty() && symptomsData.getValue() == null) {
                symptomsData.postValue(toResponses(cached));
            }
        });
    }

    private void saveToCache(List<PatientSymptomResponse> responses) {
        long patientId = networkModule.getPatientId();
        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            db.cachedSymptomDao().deleteByPatientId(patientId);
            List<CachedSymptom> entities = new ArrayList<>();
            for (PatientSymptomResponse r : responses) {
                CachedSymptom e = new CachedSymptom();
                e.setId(r.getId());
                e.setPatientId(patientId);
                e.setSymptom(r.getSymptom());
                e.setTimestamp(r.getTimestamp());
                e.setCachedAt(System.currentTimeMillis());
                entities.add(e);
            }
            db.cachedSymptomDao().insertAll(entities);
        });
    }

    private List<PatientSymptomResponse> toResponses(List<CachedSymptom> cached) {
        List<PatientSymptomResponse> list = new ArrayList<>();
        for (CachedSymptom c : cached) {
            PatientSymptomResponse r = new PatientSymptomResponse();
            r.setId(c.getId());
            r.setPatientId(c.getPatientId());
            r.setSymptom(c.getSymptom());
            r.setTimestamp(c.getTimestamp());
            list.add(r);
        }
        return list;
    }

    public void addSymptom(String symptom) {
        if (!networkModule.isPatientLoggedIn()) {
            error.postValue("Not authenticated");
            return;
        }

        if (syncManager.isOnline()) {
            isLoading.postValue(true);
            networkModule.addSymptom(symptom).enqueue(new Callback<PatientSymptomResponse>() {
                @Override
                public void onResponse(Call<PatientSymptomResponse> call, Response<PatientSymptomResponse> response) {
                    isLoading.postValue(false);
                    if (response.isSuccessful()) {
                        loadSymptoms();
                    } else {
                        queueOperation(PendingOperation.TYPE_SYMPTOM_ADD, symptom);
                    }
                }

                @Override
                public void onFailure(Call<PatientSymptomResponse> call, Throwable t) {
                    isLoading.postValue(false);
                    queueOperation(PendingOperation.TYPE_SYMPTOM_ADD, symptom);
                }
            });
        } else {
            queueOperation(PendingOperation.TYPE_SYMPTOM_ADD, symptom);
            List<PatientSymptomResponse> current = symptomsData.getValue();
            if (current == null) current = new ArrayList<>();
            List<PatientSymptomResponse> updated = new ArrayList<>(current);
            PatientSymptomResponse newSymptom = new PatientSymptomResponse();
            newSymptom.setSymptom(symptom);
            newSymptom.setTimestamp(String.valueOf(System.currentTimeMillis()));
            updated.add(newSymptom);
            symptomsData.postValue(updated);
        }
    }

    public void deleteSymptom(long symptomId) {
        if (!networkModule.isPatientLoggedIn()) {
            error.postValue("Not authenticated");
            return;
        }

        if (syncManager.isOnline()) {
            isLoading.postValue(true);
            networkModule.deleteSymptom(symptomId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    isLoading.postValue(false);
                    if (response.isSuccessful()) {
                        loadSymptoms();
                    } else {
                        queueOperation(PendingOperation.TYPE_SYMPTOM_DELETE, String.valueOf(symptomId));
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    isLoading.postValue(false);
                    queueOperation(PendingOperation.TYPE_SYMPTOM_DELETE, String.valueOf(symptomId));
                }
            });
        } else {
            queueOperation(PendingOperation.TYPE_SYMPTOM_DELETE, String.valueOf(symptomId));
            List<PatientSymptomResponse> current = symptomsData.getValue();
            if (current != null) {
                List<PatientSymptomResponse> updated = new ArrayList<>();
                for (PatientSymptomResponse s : current) {
                    if (s.getId() != symptomId) updated.add(s);
                }
                symptomsData.postValue(updated);
            }
        }
    }

    public void renewSymptom(long symptomId) {
        if (!networkModule.isPatientLoggedIn()) {
            error.postValue("Not authenticated");
            return;
        }

        isLoading.postValue(true);
        networkModule.renewSymptom(symptomId).enqueue(new Callback<PatientSymptomResponse>() {
            @Override
            public void onResponse(Call<PatientSymptomResponse> call, Response<PatientSymptomResponse> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    loadSymptoms();
                } else {
                    error.postValue("Failed to renew symptom: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PatientSymptomResponse> call, Throwable t) {
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
}
