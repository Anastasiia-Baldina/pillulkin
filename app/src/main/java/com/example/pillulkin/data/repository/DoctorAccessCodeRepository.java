package com.example.pillulkin.data.repository;

import android.app.Application;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.dao.DoctorAccessCodeDao;
import com.example.pillulkin.data.local.entity.DoctorAccessCodeEntity;

public class DoctorAccessCodeRepository {
    private final DoctorAccessCodeDao codeDao;

    public DoctorAccessCodeRepository(Application application) {
        PillulkinDatabase db = PillulkinDatabase.getDatabase(application);
        codeDao = db.doctorAccessCodeDao();
    }

    public long insert(DoctorAccessCodeEntity code) {
        java.util.concurrent.Future<Long> future = PillulkinDatabase.databaseWriteExecutor.submit(() -> codeDao.insert(code));
        try {
            return future.get();
        } catch (Exception e) {
            return -1;
        }
    }

    public DoctorAccessCodeEntity validateCode(String code) {
        long currentTime = System.currentTimeMillis();
        return codeDao.validateCode(code, currentTime);
    }

    public DoctorAccessCodeEntity getActiveCode() {
        return codeDao.getActiveCode();
    }

    public void expireOldCodes() {
        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            codeDao.expireOldCodes(System.currentTimeMillis());
        });
    }

    public void markCodeAsUsed(String code) {
        PillulkinDatabase.databaseWriteExecutor.execute(() -> codeDao.markCodeAsUsed(code));
    }
}
