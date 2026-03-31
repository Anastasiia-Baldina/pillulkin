package com.example.pillulkin.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class DateUtilsTest {
    @Test
    public void parseDate_withValidDate_returnsNotNull() {
        assertNotNull(DateUtils.parseDate("12.12.2025"));
    }

    @Test
    public void parseDate_withInvalidDate_returnsNull() {
        assertNull(DateUtils.parseDate("invalid"));
    }

    @Test
    public void parseDate_withNull_returnsNull() {
        assertNull(DateUtils.parseDate(null));
    }

    @Test
    public void isExpired_withPastDate_returnsTrue() {
        assertTrue(DateUtils.isExpired("01.01.2020"));
    }

    @Test
    public void isExpired_withFutureDate_returnsFalse() {
        assertFalse(DateUtils.isExpired("01.01.2030"));
    }

    @Test
    public void isExpired_withInvalidDate_returnsFalse() {
        assertFalse(DateUtils.isExpired("invalid"));
    }

    @Test
    public void getExpirationStatus_withExpiredDate_returnsExpired() {
        assertEquals("EXPIRED", DateUtils.getExpirationStatus("01.01.2020"));
    }

    @Test
    public void getExpirationStatus_withFarFutureDate_returnsValid() {
        assertEquals("VALID", DateUtils.getExpirationStatus("01.01.2030"));
    }

    @Test
    public void getExpirationStatus_withInvalidDate_returnsValid() {
        assertEquals("VALID", DateUtils.getExpirationStatus("invalid"));
    }

    @Test
    public void formatDate_withValidDate_returnsFormattedString() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(2025, 11, 15);
        String formatted = DateUtils.formatDate(cal.getTime());
        assertNotNull(formatted);
        assertTrue(formatted.contains("15"));
        assertTrue(formatted.contains("12") || formatted.contains("11"));
        assertTrue(formatted.contains("2025"));
    }
}
