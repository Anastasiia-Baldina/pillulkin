package com.example.pillulkin.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.pillulkin.data.local.entity.Reminder;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert
    long insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("SELECT * FROM reminder ORDER BY hour, minute")
    LiveData<List<Reminder>> getAll();

    @Query("SELECT * FROM reminder WHERE is_enabled = 1 ORDER BY hour, minute")
    List<Reminder> getEnabled();

    @Query("DELETE FROM reminder WHERE id = :id")
    void deleteById(int id);
}
