package com.example.pillulkin.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.pillulkin.data.local.entity.DiagnosisMappingEntryEntity;

import java.util.List;

@Dao
public interface DiagnosisMappingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DiagnosisMappingEntryEntity entry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DiagnosisMappingEntryEntity> entries);

    @Query("SELECT * FROM diagnosis_mapping WHERE LOWER(diagnosisName) LIKE '%' || LOWER(:diagnosis) || '%'")
    List<DiagnosisMappingEntryEntity> findByDiagnosis(String diagnosis);

    @Query("DELETE FROM diagnosis_mapping")
    void deleteAll();
}
