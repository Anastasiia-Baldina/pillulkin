package com.example.pillulkin.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.pillulkin.data.local.entity.PendingOperation;

import java.util.List;

@Dao
public interface PendingOperationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PendingOperation operation);

    @Update
    void update(PendingOperation operation);

    @Query("SELECT * FROM pending_operation WHERE status = :status ORDER BY created_at ASC")
    List<PendingOperation> getByStatus(String status);

    @Query("SELECT * FROM pending_operation WHERE status != 'FAILED' ORDER BY created_at ASC")
    List<PendingOperation> getPending();

    @Query("DELETE FROM pending_operation WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT COUNT(*) FROM pending_operation WHERE status = 'PENDING'")
    int getPendingCount();
}
