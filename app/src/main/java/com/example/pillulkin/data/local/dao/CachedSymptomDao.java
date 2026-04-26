package com.example.pillulkin.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.pillulkin.data.local.entity.CachedSymptom;

import java.util.List;

@Dao
public interface CachedSymptomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CachedSymptom> symptoms);

    @Query("SELECT * FROM cached_symptom WHERE patient_id = :patientId ORDER BY timestamp DESC")
    List<CachedSymptom> getSymptoms(long patientId);

    @Query("DELETE FROM cached_symptom WHERE patient_id = :patientId")
    void deleteByPatientId(long patientId);
}
