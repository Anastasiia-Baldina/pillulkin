package com.example.pillulkin.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.dao.MedicineDao;
import com.example.pillulkin.data.local.entity.MedicineEntity;

import java.util.List;
import java.util.concurrent.Future;

public class MedicineRepository {
    private final MedicineDao medicineDao;

    public MedicineRepository(Application application) {
        PillulkinDatabase db = PillulkinDatabase.getDatabase(application);
        medicineDao = db.medicineDao();
    }

    public LiveData<List<MedicineEntity>> getAllMedicines() {
        return medicineDao.getAllMedicines();
    }

    public List<MedicineEntity> getAllMedicinesSync() {
        return medicineDao.getAllMedicinesSync();
    }

    public LiveData<List<MedicineEntity>> getAllMedicinesSortedByExpiration() {
        return medicineDao.getAllMedicinesSortedByExpiration();
    }

    public LiveData<List<MedicineEntity>> searchMedicines(String query) {
        return medicineDao.searchMedicines(query);
    }

    public LiveData<MedicineEntity> getMedicineById(long id) {
        return medicineDao.getMedicineById(id);
    }

    public MedicineEntity getMedicineByIdSync(long id) {
        return medicineDao.getMedicineByIdSync(id);
    }

    public MedicineEntity findByNameExact(String name) {
        return medicineDao.findByNameExact(name);
    }

    public List<MedicineEntity> findByNameContaining(String name) {
        return medicineDao.findByNameContaining(name);
    }

    public void insert(MedicineEntity medicine) {
        PillulkinDatabase.databaseWriteExecutor.execute(() -> medicineDao.insert(medicine));
    }

    public void update(MedicineEntity medicine) {
        PillulkinDatabase.databaseWriteExecutor.execute(() -> medicineDao.update(medicine));
    }

    public void delete(MedicineEntity medicine) {
        PillulkinDatabase.databaseWriteExecutor.execute(() -> medicineDao.delete(medicine));
    }
}
