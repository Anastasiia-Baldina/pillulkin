package com.example.pillulkin.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.pillulkin.data.local.entity.CachedMedicine;

import java.util.List;

@Dao
public interface CachedMedicineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CachedMedicine> medicines);

    @Query("SELECT * FROM cached_medicine WHERE patient_id = :patientId")
    List<CachedMedicine> getMedicines(long patientId);

    @Query("SELECT COUNT(*) FROM cached_medicine WHERE patient_id = :patientId")
    int countByPatientId(long patientId);

    @Query("DELETE FROM cached_medicine WHERE patient_id = :patientId")
    void deleteByPatientId(long patientId);

    @Query("DELETE FROM cached_medicine WHERE id = :id")
    void deleteById(long id);
}
