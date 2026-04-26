package com.example.pillulkin.data.repository;

import com.example.pillulkin.data.remote.model.PatientSymptomResponse;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.*;

public class SymptomsAdapterTest {

    @Test
    public void isSymptomOutdated_returnsFalseForRecentSymptom() {
        String recent = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(new Date());
        assertFalse(SymptomsAdapter.isSymptomOutdated(recent));
    }

    @Test
    public void isSymptomOutdated_returnsTrueForOldSymptom() {
        long eightDaysAgo = System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000);
        String old = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(new Date(eightDaysAgo));
        assertTrue(SymptomsAdapter.isSymptomOutdated(old));
    }

    @Test
    public void isSymptomOutdated_returnsFalseForNull() {
        assertFalse(SymptomsAdapter.isSymptomOutdated(null));
    }

    @Test
    public void isSymptomOutdated_returnsFalseForEmpty() {
        assertFalse(SymptomsAdapter.isSymptomOutdated(""));
    }

    @Test
    public void isSymptomOutdated_returnsFalseForExactlySevenDays() {
        long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        String sevenDays = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(new Date(sevenDaysAgo));
        assertFalse(SymptomsAdapter.isSymptomOutdated(sevenDays));
    }
}
