package com.example.pillulkin.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.dao.SymptomDao;
import com.example.pillulkin.data.local.entity.SymptomEntity;

import java.util.List;

public class SymptomsRepository {
    private final SymptomDao symptomDao;

    public SymptomsRepository(Application application) {
        PillulkinDatabase db = PillulkinDatabase.getDatabase(application);
        symptomDao = db.symptomDao();
    }

    public LiveData<List<SymptomEntity>> getAllSymptoms() {
        return symptomDao.getAllSymptoms();
    }

    public List<SymptomEntity> getAllSymptomsSync() {
        return symptomDao.getAllSymptomsSync();
    }

    public void insert(SymptomEntity symptom) {
        PillulkinDatabase.databaseWriteExecutor.execute(() -> symptomDao.insert(symptom));
    }

    public void delete(SymptomEntity symptom) {
        PillulkinDatabase.databaseWriteExecutor.execute(() -> symptomDao.delete(symptom));
    }
}
