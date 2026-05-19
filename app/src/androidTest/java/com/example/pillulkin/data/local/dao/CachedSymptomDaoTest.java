package com.example.pillulkin.data.local.dao;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.entity.CachedSymptom;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class CachedSymptomDaoTest {

    private PillulkinDatabase db;
    private CachedSymptomDao dao;

    @Before
    public void setUp() {
        db = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                PillulkinDatabase.class
        ).allowMainThreadQueries().build();
        dao = db.cachedSymptomDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    private CachedSymptom createSymptom(long id, long patientId, String symptom, String timestamp) {
        CachedSymptom s = new CachedSymptom();
        s.setId(id);
        s.setPatientId(patientId);
        s.setSymptom(symptom);
        s.setTimestamp(timestamp);
        return s;
    }

    @Test
    public void insertAll_andReadBack() {
        CachedSymptom s1 = createSymptom(1, 100, "headache", "2025-01-01T10:00:00");
        CachedSymptom s2 = createSymptom(2, 100, "fever", "2025-01-02T10:00:00");
        dao.insertAll(Arrays.asList(s1, s2));

        List<CachedSymptom> result = dao.getSymptoms(100);
        assertEquals(2, result.size());
    }

    @Test
    public void getSymptoms_filtersByPatientId() {
        CachedSymptom s1 = createSymptom(1, 100, "headache", "2025-01-01T10:00:00");
        CachedSymptom s2 = createSymptom(2, 200, "fever", "2025-01-02T10:00:00");
        dao.insertAll(Arrays.asList(s1, s2));

        assertEquals(1, dao.getSymptoms(100).size());
        assertEquals("headache", dao.getSymptoms(100).get(0).getSymptom());
    }

    @Test
    public void getSymptoms_orderedByTimestampDesc() {
        dao.insertAll(Arrays.asList(
                createSymptom(1, 100, "old", "2025-01-01T10:00:00"),
                createSymptom(2, 100, "new", "2025-06-01T10:00:00"),
                createSymptom(3, 100, "mid", "2025-03-01T10:00:00")
        ));

        List<CachedSymptom> result = dao.getSymptoms(100);
        assertEquals(3, result.size());
        assertEquals("new", result.get(0).getSymptom());
        assertEquals("mid", result.get(1).getSymptom());
        assertEquals("old", result.get(2).getSymptom());
    }

    @Test
    public void deleteByPatientId() {
        dao.insertAll(Arrays.asList(
                createSymptom(1, 100, "a", "2025-01-01T10:00:00"),
                createSymptom(2, 200, "b", "2025-01-01T10:00:00")
        ));

        dao.deleteByPatientId(100);
        assertTrue(dao.getSymptoms(100).isEmpty());
        assertEquals(1, dao.getSymptoms(200).size());
    }

    @Test
    public void insertAll_replacesOnConflict() {
        dao.insertAll(Collections.singletonList(
                createSymptom(1, 100, "headache", "2025-01-01T10:00:00")));

        dao.insertAll(Collections.singletonList(
                createSymptom(1, 100, "migraine", "2025-02-01T10:00:00")));

        List<CachedSymptom> result = dao.getSymptoms(100);
        assertEquals(1, result.size());
        assertEquals("migraine", result.get(0).getSymptom());
    }

    @Test
    public void insertAll_preservesAllFields() {
        CachedSymptom s = new CachedSymptom();
        s.setId(1);
        s.setPatientId(100);
        s.setSymptom("headache");
        s.setTimestamp("2025-01-15T14:30:00");
        s.setCachedAt(5000L);
        s.setActual(true);
        dao.insertAll(Collections.singletonList(s));

        CachedSymptom read = dao.getSymptoms(100).get(0);
        assertEquals(1, read.getId());
        assertEquals(100, read.getPatientId());
        assertEquals("headache", read.getSymptom());
        assertEquals("2025-01-15T14:30:00", read.getTimestamp());
        assertEquals(5000L, read.getCachedAt());
        assertTrue(read.isActual());
    }

    @Test
    public void insertAll_actualDefaultsToTrue() {
        CachedSymptom s = new CachedSymptom();
        s.setId(1);
        s.setPatientId(100);
        s.setSymptom("test");
        dao.insertAll(Collections.singletonList(s));

        CachedSymptom read = dao.getSymptoms(100).get(0);
        assertTrue(read.isActual());
    }
}
