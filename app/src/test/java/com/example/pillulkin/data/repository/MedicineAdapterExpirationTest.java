package com.example.pillulkin.data.repository;

import com.example.pillulkin.data.remote.model.PatientMedicineResponse;

import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.*;

public class MedicineAdapterExpirationTest {

    @Test
    public void expirationDateParsing_isoFormat() {
        PatientMedicineResponse med = new PatientMedicineResponse();
        med.setId(1L);
        med.setMedicineName("Test");
        med.setExpirationDate(LocalDate.now().plusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE));
        assertNotNull(med.getExpirationDate());
        assertFalse(med.getExpirationDate().isEmpty());
    }

    @Test
    public void expirationDate_nullMeansNoExpiry() {
        PatientMedicineResponse med = new PatientMedicineResponse();
        med.setId(1L);
        med.setMedicineName("Test");
        assertNull(med.getExpirationDate());
    }

    @Test
    public void expirationDate_futureDate() {
        LocalDate future = LocalDate.now().plusDays(60);
        String dateStr = future.format(DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate parsed = LocalDate.parse(dateStr);
        assertTrue(parsed.isAfter(LocalDate.now()));
        assertTrue(parsed.isAfter(LocalDate.now().plusDays(30)));
    }

    @Test
    public void expirationDate_expiringSoon() {
        LocalDate soon = LocalDate.now().plusDays(15);
        String dateStr = soon.format(DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate parsed = LocalDate.parse(dateStr);
        assertTrue(parsed.isAfter(LocalDate.now()));
        assertFalse(parsed.isAfter(LocalDate.now().plusDays(30)));
    }

    @Test
    public void expirationDate_alreadyExpired() {
        LocalDate past = LocalDate.now().minusDays(1);
        String dateStr = past.format(DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate parsed = LocalDate.parse(dateStr);
        assertFalse(parsed.isAfter(LocalDate.now()));
    }
}
