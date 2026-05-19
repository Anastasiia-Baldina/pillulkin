package com.example.pillulkin.data.local.dao;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.entity.CachedProfile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class CachedProfileDaoTest {

    private PillulkinDatabase db;
    private CachedProfileDao dao;

    @Before
    public void setUp() {
        db = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                PillulkinDatabase.class
        ).allowMainThreadQueries().build();
        dao = db.cachedProfileDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    private CachedProfile createProfile(long patientId, String name) {
        CachedProfile p = new CachedProfile();
        p.setPatientId(patientId);
        p.setName(name);
        return p;
    }

    @Test
    public void insert_andReadBack() {
        CachedProfile p = createProfile(100, "Ivan");
        p.setAge(30);
        p.setAllergies("penicillin");
        p.setContraindications("none");
        p.setNotes("healthy");
        p.setCachedAt(1000L);
        dao.insert(p);

        CachedProfile read = dao.getProfile(100);
        assertNotNull(read);
        assertEquals(100, read.getPatientId());
        assertEquals("Ivan", read.getName());
        assertEquals(Integer.valueOf(30), read.getAge());
        assertEquals("penicillin", read.getAllergies());
        assertEquals("none", read.getContraindications());
        assertEquals("healthy", read.getNotes());
        assertEquals(1000L, read.getCachedAt());
    }

    @Test
    public void getProfile_returnsNullForUnknownPatient() {
        assertNull(dao.getProfile(999));
    }

    @Test
    public void insert_replacesOnConflict() {
        dao.insert(createProfile(100, "Ivan"));
        assertEquals("Ivan", dao.getProfile(100).getName());

        dao.insert(createProfile(100, "Maria"));
        assertEquals("Maria", dao.getProfile(100).getName());
    }

    @Test
    public void deleteByPatientId() {
        dao.insert(createProfile(100, "Ivan"));
        dao.insert(createProfile(200, "Maria"));

        dao.deleteByPatientId(100);
        assertNull(dao.getProfile(100));
        assertNotNull(dao.getProfile(200));
    }

    @Test
    public void insert_minimalFields() {
        CachedProfile p = new CachedProfile();
        p.setPatientId(300);
        dao.insert(p);

        CachedProfile read = dao.getProfile(300);
        assertNotNull(read);
        assertEquals(300, read.getPatientId());
        assertNull(read.getName());
        assertNull(read.getAge());
        assertNull(read.getAllergies());
        assertNull(read.getContraindications());
        assertNull(read.getNotes());
    }

    @Test
    public void deleteByPatientId_nonExistent_noError() {
        dao.deleteByPatientId(999);
    }
}
