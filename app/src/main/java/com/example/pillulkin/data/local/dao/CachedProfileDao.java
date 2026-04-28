package com.example.pillulkin.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.pillulkin.data.local.entity.CachedProfile;

@Dao
public interface CachedProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CachedProfile profile);

    @Query("SELECT * FROM cached_profile WHERE patient_id = :patientId LIMIT 1")
    CachedProfile getProfile(long patientId);

    @Query("DELETE FROM cached_profile WHERE patient_id = :patientId")
    void deleteByPatientId(long patientId);
}
