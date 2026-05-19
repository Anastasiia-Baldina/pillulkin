package com.example.pillulkin.data.local.dao;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.entity.CachedMedicine;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class CachedMedicineDaoTest {

    private PillulkinDatabase db;
    private CachedMedicineDao dao;

    @Before
    public void setUp() {
        db = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                PillulkinDatabase.class
        ).allowMainThreadQueries().build();
        dao = db.cachedMedicineDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    private CachedMedicine createMedicine(long id, long patientId, String name, String dosage) {
        CachedMedicine m = new CachedMedicine();
        m.setId(id);
        m.setPatientId(patientId);
        m.setMedicineName(name);
        m.setDosage(dosage);
        return m;
    }

    @Test
    public void insertAll_andReadBack() {
        CachedMedicine m1 = createMedicine(1, 100, "Aspirin", "500mg");
        CachedMedicine m2 = createMedicine(2, 100, "Nurofen", "200mg");
        dao.insertAll(Arrays.asList(m1, m2));

        List<CachedMedicine> result = dao.getMedicines(100);
        assertEquals(2, result.size());
    }

    @Test
    public void getMedicines_filtersByPatientId() {
        CachedMedicine m1 = createMedicine(1, 100, "Aspirin", "500mg");
        CachedMedicine m2 = createMedicine(2, 200, "Nurofen", "200mg");
        dao.insertAll(Arrays.asList(m1, m2));

        assertEquals(1, dao.getMedicines(100).size());
        assertEquals("Aspirin", dao.getMedicines(100).get(0).getMedicineName());
        assertEquals(1, dao.getMedicines(200).size());
        assertEquals("Nurofen", dao.getMedicines(200).get(0).getMedicineName());
    }

    @Test
    public void getMedicines_emptyForUnknownPatient() {
        CachedMedicine m = createMedicine(1, 100, "Aspirin", "500mg");
        dao.insertAll(Collections.singletonList(m));

        assertTrue(dao.getMedicines(999).isEmpty());
    }

    @Test
    public void deleteByPatientId() {
        CachedMedicine m1 = createMedicine(1, 100, "Aspirin", "500mg");
        CachedMedicine m2 = createMedicine(2, 100, "Nurofen", "200mg");
        CachedMedicine m3 = createMedicine(3, 200, "Ibuprofen", "400mg");
        dao.insertAll(Arrays.asList(m1, m2, m3));

        dao.deleteByPatientId(100);
        assertTrue(dao.getMedicines(100).isEmpty());
        assertEquals(1, dao.getMedicines(200).size());
    }

    @Test
    public void insertAll_replacesOnConflict() {
        CachedMedicine m1 = createMedicine(1, 100, "Aspirin", "500mg");
        dao.insertAll(Collections.singletonList(m1));

        CachedMedicine m1Updated = createMedicine(1, 100, "Aspirin Forte", "1000mg");
        dao.insertAll(Collections.singletonList(m1Updated));

        List<CachedMedicine> result = dao.getMedicines(100);
        assertEquals(1, result.size());
        assertEquals("Aspirin Forte", result.get(0).getMedicineName());
        assertEquals("1000mg", result.get(0).getDosage());
    }

    @Test
    public void insertAll_allFieldsPreserved() {
        CachedMedicine m = new CachedMedicine();
        m.setId(1);
        m.setPatientId(100);
        m.setMedicineId(5L);
        m.setMedicineName("Aspirin");
        m.setDosage("500mg");
        m.setForm("tablet");
        m.setAddedAt("2025-01-01");
        m.setExpirationDate("2026-12-31");
        m.setQuantity("30");
        m.setActiveSubstance("acetylsalicylic acid");
        m.setCachedAt(1000L);
        dao.insertAll(Collections.singletonList(m));

        CachedMedicine read = dao.getMedicines(100).get(0);
        assertEquals(1, read.getId());
        assertEquals(100, read.getPatientId());
        assertEquals(Long.valueOf(5L), read.getMedicineId());
        assertEquals("Aspirin", read.getMedicineName());
        assertEquals("500mg", read.getDosage());
        assertEquals("tablet", read.getForm());
        assertEquals("2025-01-01", read.getAddedAt());
        assertEquals("2026-12-31", read.getExpirationDate());
        assertEquals("30", read.getQuantity());
        assertEquals("acetylsalicylic acid", read.getActiveSubstance());
        assertEquals(1000L, read.getCachedAt());
    }
}
