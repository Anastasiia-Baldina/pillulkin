package com.example.pillulkin.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.pillulkin.data.local.entity.SymptomEntity;

import java.util.List;

@Dao
public interface SymptomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SymptomEntity symptom);

    @Delete
    void delete(SymptomEntity symptom);

    @Query("SELECT * FROM symptoms ORDER BY timestamp DESC")
    LiveData<List<SymptomEntity>> getAllSymptoms();

    @Query("SELECT * FROM symptoms ORDER BY timestamp DESC")
    List<SymptomEntity> getAllSymptomsSync();

    @Query("DELETE FROM symptoms")
    void deleteAll();
}
