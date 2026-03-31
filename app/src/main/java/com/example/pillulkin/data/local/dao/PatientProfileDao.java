package com.example.pillulkin.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.pillulkin.data.local.entity.PatientProfileEntity;

@Dao
public interface PatientProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PatientProfileEntity profile);

    @Update
    void update(PatientProfileEntity profile);

    @Delete
    void delete(PatientProfileEntity profile);

    @Query("SELECT * FROM patient_profile LIMIT 1")
    LiveData<PatientProfileEntity> getProfile();

    @Query("SELECT * FROM patient_profile LIMIT 1")
    PatientProfileEntity getProfileSync();

    @Query("DELETE FROM patient_profile")
    void deleteAll();
}
