package com.example.pillulkin.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.pillulkin.data.local.dao.CachedMedicineDao;
import com.example.pillulkin.data.local.dao.CachedProfileDao;
import com.example.pillulkin.data.local.dao.CachedSymptomDao;
import com.example.pillulkin.data.local.dao.PendingOperationDao;
import com.example.pillulkin.data.local.dao.ReminderDao;
import com.example.pillulkin.data.local.entity.CachedMedicine;
import com.example.pillulkin.data.local.entity.CachedProfile;
import com.example.pillulkin.data.local.entity.CachedSymptom;
import com.example.pillulkin.data.local.entity.PendingOperation;
import com.example.pillulkin.data.local.entity.Reminder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                CachedProfile.class,
                CachedSymptom.class,
                CachedMedicine.class,
                PendingOperation.class,
                Reminder.class
        },
        version = 2,
        exportSchema = false
)
public abstract class PillulkinDatabase extends RoomDatabase {

    private static volatile PillulkinDatabase instance;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public static synchronized PillulkinDatabase getDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    PillulkinDatabase.class,
                    "pillulkin_cache"
            ).fallbackToDestructiveMigration().build();
        }
        return instance;
    }

    public abstract CachedProfileDao cachedProfileDao();

    public abstract CachedSymptomDao cachedSymptomDao();

    public abstract CachedMedicineDao cachedMedicineDao();

    public abstract PendingOperationDao pendingOperationDao();

    public abstract ReminderDao reminderDao();
}
