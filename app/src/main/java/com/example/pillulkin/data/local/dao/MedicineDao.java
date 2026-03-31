package com.example.pillulkin.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.pillulkin.data.local.entity.MedicineEntity;

import java.util.List;

@Dao
public interface MedicineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(MedicineEntity medicine);

    @Update
    void update(MedicineEntity medicine);

    @Delete
    void delete(MedicineEntity medicine);

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    LiveData<List<MedicineEntity>> getAllMedicines();

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    List<MedicineEntity> getAllMedicinesSync();

    @Query("SELECT * FROM medicines ORDER BY expirationDate ASC")
    LiveData<List<MedicineEntity>> getAllMedicinesSortedByExpiration();

    @Query("SELECT * FROM medicines WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<MedicineEntity>> searchMedicines(String query);

    @Query("SELECT * FROM medicines WHERE id = :id")
    LiveData<MedicineEntity> getMedicineById(long id);

    @Query("SELECT * FROM medicines WHERE id = :id")
    MedicineEntity getMedicineByIdSync(long id);

    @Query("SELECT * FROM medicines WHERE LOWER(name) = LOWER(:name)")
    MedicineEntity findByNameExact(String name);

    @Query("SELECT * FROM medicines WHERE LOWER(name) LIKE '%' || LOWER(:name) || '%'")
    List<MedicineEntity> findByNameContaining(String name);

    @Query("DELETE FROM medicines")
    void deleteAll();
}
