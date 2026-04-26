package com.example.pillulkin.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.entity.CachedProfile;
import com.example.pillulkin.data.local.entity.PendingOperation;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.PatientProfileRequest;
import com.example.pillulkin.data.remote.model.PatientProfileResponse;
import com.example.pillulkin.sync.SyncManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientProfileRepository {
    private final NetworkModule networkModule;
    private final PillulkinDatabase db;
    private final SyncManager syncManager;
    private final MutableLiveData<PatientProfileResponse> profileData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public PatientProfileRepository(Application application) {
        networkModule = NetworkModule.getInstance(application);
        db = PillulkinDatabase.getDatabase(application);
        syncManager = new SyncManager(application);
    }

    public LiveData<PatientProfileResponse> getProfile() { return profileData; }
    public LiveData<Boolean> isLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }

    public void loadProfile() {
        if (!networkModule.isPatientLoggedIn()) {
            loadFromCache();
            return;
        }

        loadFromCache();

        isLoading.postValue(true);
        networkModule.getProfile().enqueue(new Callback<PatientProfileResponse>() {
            @Override
            public void onResponse(Call<PatientProfileResponse> call, Response<PatientProfileResponse> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    profileData.postValue(response.body());
                    saveToCache(response.body());
                } else {
                    error.postValue("Failed to load profile: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PatientProfileResponse> call, Throwable t) {
                isLoading.postValue(false);
                if (profileData.getValue() == null) {
                    error.postValue("Network error: " + t.getMessage());
                }
            }
        });
    }

    private void loadFromCache() {
        long patientId = networkModule.getPatientId();
        if (patientId <= 0) return;
        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            CachedProfile cached = db.cachedProfileDao().getProfile(patientId);
            if (cached != null && profileData.getValue() == null) {
                PatientProfileResponse response = new PatientProfileResponse();
                response.setId(cached.getPatientId());
                response.setPatientId(cached.getPatientId());
                response.setName(cached.getName());
                response.setAge(cached.getAge());
                response.setAllergies(cached.getAllergies());
                response.setContraindications(cached.getContraindications());
                response.setNotes(cached.getNotes());
                profileData.postValue(response);
            }
        });
    }

    private void saveToCache(PatientProfileResponse apiProfile) {
        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            CachedProfile cached = new CachedProfile();
            cached.setPatientId(apiProfile.getPatientId() != null ? apiProfile.getPatientId() : 0);
            cached.setName(apiProfile.getName());
            cached.setAge(apiProfile.getAge());
            cached.setAllergies(apiProfile.getAllergies());
            cached.setContraindications(apiProfile.getContraindications());
            cached.setNotes(apiProfile.getNotes());
            cached.setCachedAt(System.currentTimeMillis());
            db.cachedProfileDao().insert(cached);
        });
    }

    public void saveProfile(String name, Integer age, String allergies, String contraindications, String notes) {
        if (!networkModule.isPatientLoggedIn()) {
            error.postValue("Not authenticated");
            return;
        }

        PatientProfileResponse optimistic = new PatientProfileResponse();
        optimistic.setPatientId(networkModule.getPatientId());
        optimistic.setName(name);
        optimistic.setAge(age);
        optimistic.setAllergies(allergies);
        optimistic.setContraindications(contraindications);
        optimistic.setNotes(notes);
        profileData.postValue(optimistic);
        saveToCache(optimistic);

        String payload = (name != null ? name : "") + "|" +
                (age != null ? age : "") + "|" +
                (allergies != null ? allergies : "") + "|" +
                (contraindications != null ? contraindications : "") + "|" +
                (notes != null ? notes : "");

        if (syncManager.isOnline()) {
            isLoading.postValue(true);
            PatientProfileRequest request = new PatientProfileRequest(name, age, allergies, contraindications, notes);
            networkModule.updateProfile(request).enqueue(new Callback<PatientProfileResponse>() {
                @Override
                public void onResponse(Call<PatientProfileResponse> call, Response<PatientProfileResponse> response) {
                    isLoading.postValue(false);
                    if (response.isSuccessful() && response.body() != null) {
                        profileData.postValue(response.body());
                        saveToCache(response.body());
                    } else {
                        queueOfflineOperation(payload);
                    }
                }

                @Override
                public void onFailure(Call<PatientProfileResponse> call, Throwable t) {
                    isLoading.postValue(false);
                    queueOfflineOperation(payload);
                }
            });
        } else {
            queueOfflineOperation(payload);
        }
    }

    private void queueOfflineOperation(String payload) {
        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            PendingOperation op = new PendingOperation();
            op.setType(PendingOperation.TYPE_PROFILE_UPDATE);
            op.setPayload(payload);
            op.setStatus(PendingOperation.STATUS_PENDING);
            op.setRetryCount(0);
            op.setCreatedAt(System.currentTimeMillis());
            db.pendingOperationDao().insert(op);
        });
    }

    public void logout() {
        networkModule.clearPatientSession();
        profileData.postValue(null);
    }
}
