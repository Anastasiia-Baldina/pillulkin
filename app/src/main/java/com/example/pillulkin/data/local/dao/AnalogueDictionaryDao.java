package com.example.pillulkin.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.pillulkin.data.local.entity.AnalogueDictionaryEntryEntity;

import java.util.List;

@Dao
public interface AnalogueDictionaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AnalogueDictionaryEntryEntity entry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AnalogueDictionaryEntryEntity> entries);

    @Query("SELECT * FROM analogue_dictionary WHERE LOWER(sourceName) = LOWER(:medicineName) OR LOWER(analogueName) = LOWER(:medicineName)")
    List<AnalogueDictionaryEntryEntity> findAnalogues(String medicineName);

    @Query("SELECT * FROM analogue_dictionary WHERE LOWER(activeSubstance) = LOWER(:substance)")
    List<AnalogueDictionaryEntryEntity> findByActiveSubstance(String substance);

    @Query("DELETE FROM analogue_dictionary")
    void deleteAll();
}
