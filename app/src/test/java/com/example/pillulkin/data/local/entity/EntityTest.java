package com.example.pillulkin.data.local.entity;

import org.junit.Test;

import static org.junit.Assert.*;

public class EntityTest {

    @Test
    public void cachedMedicine_allFields() {
        CachedMedicine m = new CachedMedicine();
        m.setId(1L);
        m.setPatientId(10L);
        m.setMedicineId(5L);
        m.setMedicineName("Aspirin");
        m.setDosage("500mg");
        m.setForm("tablet");
        m.setAddedAt("2024-01-15");
        m.setExpirationDate("2025-12-31");
        m.setQuantity("30");
        m.setActiveSubstance("acetylsalicylic acid");
        m.setCachedAt(1000L);

        assertEquals(1L, m.getId());
        assertEquals(10L, m.getPatientId());
        assertEquals(Long.valueOf(5L), m.getMedicineId());
        assertEquals("Aspirin", m.getMedicineName());
        assertEquals("500mg", m.getDosage());
        assertEquals("tablet", m.getForm());
        assertEquals("2024-01-15", m.getAddedAt());
        assertEquals("2025-12-31", m.getExpirationDate());
        assertEquals("30", m.getQuantity());
        assertEquals("acetylsalicylic acid", m.getActiveSubstance());
        assertEquals(1000L, m.getCachedAt());
    }

    @Test
    public void cachedMedicine_nullables() {
        CachedMedicine m = new CachedMedicine();
        assertNull(m.getMedicineId());
        assertNull(m.getMedicineName());
        assertNull(m.getDosage());
        assertNull(m.getForm());
        assertNull(m.getAddedAt());
        assertNull(m.getExpirationDate());
        assertNull(m.getQuantity());
        assertNull(m.getActiveSubstance());
    }

    @Test
    public void cachedSymptom_allFields() {
        CachedSymptom s = new CachedSymptom();
        s.setId(1L);
        s.setPatientId(10L);
        s.setSymptom("headache");
        s.setTimestamp("2024-01-15T10:30:00");
        s.setCachedAt(2000L);
        s.setActual(true);

        assertEquals(1L, s.getId());
        assertEquals(10L, s.getPatientId());
        assertEquals("headache", s.getSymptom());
        assertEquals("2024-01-15T10:30:00", s.getTimestamp());
        assertEquals(2000L, s.getCachedAt());
        assertTrue(s.isActual());
    }

    @Test
    public void cachedSymptom_defaultActualIsTrue() {
        CachedSymptom s = new CachedSymptom();
        assertTrue(s.isActual());
    }

    @Test
    public void cachedSymptom_setActualFalse() {
        CachedSymptom s = new CachedSymptom();
        s.setActual(false);
        assertFalse(s.isActual());
    }

    @Test
    public void cachedProfile_allFields() {
        CachedProfile p = new CachedProfile();
        p.setPatientId(1L);
        p.setName("Ivan");
        p.setAge(30);
        p.setAllergies("penicillin");
        p.setContraindications("none");
        p.setNotes("healthy");
        p.setCachedAt(3000L);

        assertEquals(1L, p.getPatientId());
        assertEquals("Ivan", p.getName());
        assertEquals(Integer.valueOf(30), p.getAge());
        assertEquals("penicillin", p.getAllergies());
        assertEquals("none", p.getContraindications());
        assertEquals("healthy", p.getNotes());
        assertEquals(3000L, p.getCachedAt());
    }

    @Test
    public void cachedProfile_nullables() {
        CachedProfile p = new CachedProfile();
        assertNull(p.getName());
        assertNull(p.getAge());
        assertNull(p.getAllergies());
        assertNull(p.getContraindications());
        assertNull(p.getNotes());
    }

    @Test
    public void pendingOperation_allFields() {
        PendingOperation op = new PendingOperation();
        op.setId(1L);
        op.setType(PendingOperation.TYPE_PROFILE_UPDATE);
        op.setPayload("data");
        op.setEntityId(42L);
        op.setStatus(PendingOperation.STATUS_PENDING);
        op.setRetryCount(0);
        op.setCreatedAt(4000L);

        assertEquals(1L, op.getId());
        assertEquals("PROFILE_UPDATE", op.getType());
        assertEquals("data", op.getPayload());
        assertEquals(Long.valueOf(42L), op.getEntityId());
        assertEquals("PENDING", op.getStatus());
        assertEquals(0, op.getRetryCount());
        assertEquals(4000L, op.getCreatedAt());
    }

    @Test
    public void pendingOperation_constants() {
        assertEquals("PROFILE_UPDATE", PendingOperation.TYPE_PROFILE_UPDATE);
        assertEquals("SYMPTOM_ADD", PendingOperation.TYPE_SYMPTOM_ADD);
        assertEquals("SYMPTOM_DELETE", PendingOperation.TYPE_SYMPTOM_DELETE);
        assertEquals("MEDICINE_ADD", PendingOperation.TYPE_MEDICINE_ADD);
        assertEquals("MEDICINE_DELETE", PendingOperation.TYPE_MEDICINE_DELETE);
        assertEquals("PENDING", PendingOperation.STATUS_PENDING);
        assertEquals("SYNCING", PendingOperation.STATUS_SYNCING);
        assertEquals("FAILED", PendingOperation.STATUS_FAILED);
    }

    @Test
    public void pendingOperation_nullables() {
        PendingOperation op = new PendingOperation();
        assertNull(op.getType());
        assertNull(op.getPayload());
        assertNull(op.getEntityId());
        assertNull(op.getStatus());
    }

    @Test
    public void reminder_allFields() {
        Reminder r = new Reminder();
        r.setId(1);
        r.setHour(14);
        r.setMinute(30);
        r.setMedicineName("Aspirin");
        r.setCustomText("Take with food");
        r.setEnabled(true);

        assertEquals(1, r.getId());
        assertEquals(14, r.getHour());
        assertEquals(30, r.getMinute());
        assertEquals("Aspirin", r.getMedicineName());
        assertEquals("Take with food", r.getCustomText());
        assertTrue(r.isEnabled());
    }

    @Test
    public void reminder_displayTextEmptyMedicineName() {
        Reminder r = new Reminder();
        r.setMedicineName("");
        assertEquals("Не забудьте принять лекарство", r.getDisplayText());
    }

    @Test
    public void reminder_displayTextEmptyCustomText() {
        Reminder r = new Reminder();
        r.setMedicineName("Aspirin");
        r.setCustomText("");
        assertEquals("Не забудьте принять Aspirin", r.getDisplayText());
    }

    @Test
    public void reminder_timeFormatted_boundary() {
        Reminder r = new Reminder();
        r.setHour(23);
        r.setMinute(59);
        assertEquals("23:59", r.getTimeFormatted());
    }
}
