package com.example.pillulkin.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.dao.PatientProfileDao;
import com.example.pillulkin.data.local.entity.PatientProfileEntity;

public class PatientProfileRepository {
    private final PatientProfileDao profileDao;

    public PatientProfileRepository(Application application) {
        PillulkinDatabase db = PillulkinDatabase.getDatabase(application);
        profileDao = db.patientProfileDao();
    }

    public LiveData<PatientProfileEntity> getProfile() {
        return profileDao.getProfile();
    }

    public PatientProfileEntity getProfileSync() {
        return profileDao.getProfileSync();
    }

    public void insert(PatientProfileEntity profile) {
        PillulkinDatabase.databaseWriteExecutor.execute(() -> profileDao.insert(profile));
    }

    public void update(PatientProfileEntity profile) {
        PillulkinDatabase.databaseWriteExecutor.execute(() -> profileDao.update(profile));
    }

    public void delete(PatientProfileEntity profile) {
        PillulkinDatabase.databaseWriteExecutor.execute(() -> profileDao.delete(profile));
    }
}
