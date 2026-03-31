package com.example.pillulkin.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.pillulkin.data.local.entity.DoctorAccessCodeEntity;

@Dao
public interface DoctorAccessCodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(DoctorAccessCodeEntity code);

    @Query("SELECT * FROM doctor_access_codes WHERE code = :code AND status = 'ACTIVE' AND expiresAt > :currentTime LIMIT 1")
    DoctorAccessCodeEntity validateCode(String code, long currentTime);

    @Query("SELECT * FROM doctor_access_codes WHERE status = 'ACTIVE' ORDER BY createdAt DESC LIMIT 1")
    DoctorAccessCodeEntity getActiveCode();

    @Query("UPDATE doctor_access_codes SET status = 'EXPIRED' WHERE expiresAt <= :currentTime AND status = 'ACTIVE'")
    void expireOldCodes(long currentTime);

    @Query("UPDATE doctor_access_codes SET status = 'USED' WHERE code = :code")
    void markCodeAsUsed(String code);

    @Query("DELETE FROM doctor_access_codes")
    void deleteAll();
}
