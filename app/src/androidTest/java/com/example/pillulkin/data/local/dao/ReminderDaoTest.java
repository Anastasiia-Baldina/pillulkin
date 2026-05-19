package com.example.pillulkin.data.local.dao;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.entity.Reminder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ReminderDaoTest {

    private PillulkinDatabase db;
    private ReminderDao dao;

    @Before
    public void setUp() {
        db = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                PillulkinDatabase.class
        ).allowMainThreadQueries().build();
        dao = db.reminderDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    private Reminder createReminder(int hour, int minute, String name, boolean enabled) {
        Reminder r = new Reminder();
        r.setHour(hour);
        r.setMinute(minute);
        r.setMedicineName(name);
        r.setEnabled(enabled);
        return r;
    }

    @Test
    public void insert_andReadBack() {
        Reminder r = createReminder(8, 30, "Aspirin", true);
        long id = dao.insert(r);
        assertTrue(id > 0);

        List<Reminder> all = dao.getEnabled();
        assertEquals(1, all.size());
        assertEquals(8, all.get(0).getHour());
        assertEquals(30, all.get(0).getMinute());
        assertEquals("Aspirin", all.get(0).getMedicineName());
        assertTrue(all.get(0).isEnabled());
    }

    @Test
    public void insert_disabled_notInGetEnabled() {
        Reminder r = createReminder(10, 0, "Nurofen", false);
        dao.insert(r);

        List<Reminder> enabled = dao.getEnabled();
        assertTrue(enabled.isEmpty());
    }

    @Test
    public void insert_multiple_orderedByHourMinute() {
        dao.insert(createReminder(14, 0, "C", true));
        dao.insert(createReminder(8, 30, "A", true));
        dao.insert(createReminder(8, 0, "B", true));

        List<Reminder> enabled = dao.getEnabled();
        assertEquals(3, enabled.size());
        assertEquals("B", enabled.get(0).getMedicineName());
        assertEquals("A", enabled.get(1).getMedicineName());
        assertEquals("C", enabled.get(2).getMedicineName());
    }

    @Test
    public void deleteById() {
        Reminder r = createReminder(9, 0, "Test", true);
        long id = dao.insert(r);
        assertEquals(1, dao.getEnabled().size());

        dao.deleteById((int) id);
        assertTrue(dao.getEnabled().isEmpty());
    }

    @Test
    public void update() {
        Reminder r = createReminder(9, 0, "Old", true);
        long id = dao.insert(r);

        r.setId((int) id);
        r.setMedicineName("New");
        r.setEnabled(false);
        dao.update(r);

        List<Reminder> enabled = dao.getEnabled();
        assertTrue(enabled.isEmpty());
    }

    @Test
    public void delete_reminder() {
        Reminder r = createReminder(12, 0, "Del", true);
        dao.insert(r);
        assertEquals(1, dao.getEnabled().size());

        List<Reminder> all = dao.getEnabled();
        dao.delete(all.get(0));
        assertTrue(dao.getEnabled().isEmpty());
    }

    @Test
    public void insert_returnsDifferentIds() {
        long id1 = dao.insert(createReminder(8, 0, "A", true));
        long id2 = dao.insert(createReminder(9, 0, "B", true));
        assertNotEquals(id1, id2);
    }

    @Test
    public void insert_mixedEnabledDisabled() {
        dao.insert(createReminder(8, 0, "A", true));
        dao.insert(createReminder(9, 0, "B", false));
        dao.insert(createReminder(10, 0, "C", true));
        dao.insert(createReminder(11, 0, "D", false));

        List<Reminder> enabled = dao.getEnabled();
        assertEquals(2, enabled.size());
        assertEquals("A", enabled.get(0).getMedicineName());
        assertEquals("C", enabled.get(1).getMedicineName());
    }

    @Test
    public void insert_withCustomText() {
        Reminder r = createReminder(8, 0, "Aspirin", true);
        r.setCustomText("Take after meal");
        dao.insert(r);

        List<Reminder> all = dao.getEnabled();
        assertEquals(1, all.size());
        assertEquals("Take after meal", all.get(0).getCustomText());
        assertEquals("Take after meal", all.get(0).getDisplayText());
    }

    @Test
    public void update_toggleEnabled() {
        Reminder r = createReminder(9, 0, "Test", true);
        long id = dao.insert(r);
        assertEquals(1, dao.getEnabled().size());

        r.setId((int) id);
        r.setEnabled(false);
        dao.update(r);
        assertTrue(dao.getEnabled().isEmpty());

        r.setEnabled(true);
        dao.update(r);
        assertEquals(1, dao.getEnabled().size());
    }

    @Test
    public void deleteById_nonExistent_noError() {
        dao.deleteById(999);
    }

    @Test
    public void getAll_liveData_emptyInitially() throws InterruptedException {
        androidx.lifecycle.LiveData<List<Reminder>> liveData = dao.getAll();
        LiveDataUtils.getOrAwaitValue(liveData, 2, TimeUnit.SECONDS);
    }

    @Test
    public void getAll_liveData_afterInsert() throws InterruptedException {
        dao.insert(createReminder(8, 0, "A", true));
        dao.insert(createReminder(9, 0, "B", false));

        androidx.lifecycle.LiveData<List<Reminder>> liveData = dao.getAll();
        LiveDataUtils.getOrAwaitValue(liveData, 2, TimeUnit.SECONDS);
    }
}
