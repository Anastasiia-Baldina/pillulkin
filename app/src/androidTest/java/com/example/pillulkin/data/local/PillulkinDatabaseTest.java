package com.example.pillulkin.data.local;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pillulkin.data.local.dao.CachedMedicineDao;
import com.example.pillulkin.data.local.dao.CachedProfileDao;
import com.example.pillulkin.data.local.dao.CachedSymptomDao;
import com.example.pillulkin.data.local.dao.PendingOperationDao;
import com.example.pillulkin.data.local.dao.ReminderDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PillulkinDatabaseTest {

    private PillulkinDatabase db;

    @Before
    public void setUp() {
        db = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                PillulkinDatabase.class
        ).allowMainThreadQueries().build();
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void cachedProfileDao_notNull() {
        CachedProfileDao dao = db.cachedProfileDao();
        assertNotNull(dao);
    }

    @Test
    public void cachedSymptomDao_notNull() {
        CachedSymptomDao dao = db.cachedSymptomDao();
        assertNotNull(dao);
    }

    @Test
    public void cachedMedicineDao_notNull() {
        CachedMedicineDao dao = db.cachedMedicineDao();
        assertNotNull(dao);
    }

    @Test
    public void pendingOperationDao_notNull() {
        PendingOperationDao dao = db.pendingOperationDao();
        assertNotNull(dao);
    }

    @Test
    public void reminderDao_notNull() {
        ReminderDao dao = db.reminderDao();
        assertNotNull(dao);
    }

    @Test
    public void allDaosReturnSameInstance() {
        assertSame(db.cachedProfileDao(), db.cachedProfileDao());
        assertSame(db.cachedSymptomDao(), db.cachedSymptomDao());
        assertSame(db.cachedMedicineDao(), db.cachedMedicineDao());
        assertSame(db.pendingOperationDao(), db.pendingOperationDao());
        assertSame(db.reminderDao(), db.reminderDao());
    }
}
