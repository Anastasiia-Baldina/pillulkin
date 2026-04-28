package com.example.pillulkin.data.local.entity;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReminderEntityTest {

    private Reminder reminder;

    @Before
    public void setUp() {
        reminder = new Reminder();
    }

    @Test
    public void defaultIdIsZero() {
        assertEquals(0, reminder.getId());
    }

    @Test
    public void defaultHourIsZero() {
        assertEquals(0, reminder.getHour());
    }

    @Test
    public void defaultMinuteIsZero() {
        assertEquals(0, reminder.getMinute());
    }

    @Test
    public void defaultMedicineNameIsNull() {
        assertNull(reminder.getMedicineName());
    }

    @Test
    public void defaultCustomTextIsNull() {
        assertNull(reminder.getCustomText());
    }

    @Test
    public void defaultEnabledIsFalse() {
        assertFalse(reminder.isEnabled());
    }

    @Test
    public void setIdAndGetId() {
        reminder.setId(42);
        assertEquals(42, reminder.getId());
    }

    @Test
    public void setIdNegative() {
        reminder.setId(-1);
        assertEquals(-1, reminder.getId());
    }

    @Test
    public void setHourAndGetHour() {
        reminder.setHour(14);
        assertEquals(14, reminder.getHour());
    }

    @Test
    public void setHourZero() {
        reminder.setHour(0);
        assertEquals(0, reminder.getHour());
    }

    @Test
    public void setHourTwentyThree() {
        reminder.setHour(23);
        assertEquals(23, reminder.getHour());
    }

    @Test
    public void setMinuteAndGetMinute() {
        reminder.setMinute(30);
        assertEquals(30, reminder.getMinute());
    }

    @Test
    public void setMinuteZero() {
        reminder.setMinute(0);
        assertEquals(0, reminder.getMinute());
    }

    @Test
    public void setMinuteFiftyNine() {
        reminder.setMinute(59);
        assertEquals(59, reminder.getMinute());
    }

    @Test
    public void setMedicineNameAndGetMedicineName() {
        reminder.setMedicineName("Зодак");
        assertEquals("Зодак", reminder.getMedicineName());
    }

    @Test
    public void setMedicineNameToNull() {
        reminder.setMedicineName("Аспирин");
        reminder.setMedicineName(null);
        assertNull(reminder.getMedicineName());
    }

    @Test
    public void setCustomTextAndGetCustomText() {
        reminder.setCustomText("После еды");
        assertEquals("После еды", reminder.getCustomText());
    }

    @Test
    public void setCustomTextToNull() {
        reminder.setCustomText("text");
        reminder.setCustomText(null);
        assertNull(reminder.getCustomText());
    }

    @Test
    public void setEnabledAndGetEnabled() {
        reminder.setEnabled(true);
        assertTrue(reminder.isEnabled());
    }

    @Test
    public void setEnabledFalse() {
        reminder.setEnabled(true);
        reminder.setEnabled(false);
        assertFalse(reminder.isEnabled());
    }

    @Test
    public void getTimeFormatted_midnight() {
        reminder.setHour(0);
        reminder.setMinute(0);
        assertEquals("00:00", reminder.getTimeFormatted());
    }

    @Test
    public void getTimeFormatted_singleDigitHourAndMinute() {
        reminder.setHour(9);
        reminder.setMinute(5);
        assertEquals("09:05", reminder.getTimeFormatted());
    }

    @Test
    public void getTimeFormatted_doubleDigitHourAndMinute() {
        reminder.setHour(21);
        reminder.setMinute(30);
        assertEquals("21:30", reminder.getTimeFormatted());
    }

    @Test
    public void getTimeFormatted_endOfDay() {
        reminder.setHour(23);
        reminder.setMinute(59);
        assertEquals("23:59", reminder.getTimeFormatted());
    }

    @Test
    public void getTimeFormatted_noon() {
        reminder.setHour(12);
        reminder.setMinute(0);
        assertEquals("12:00", reminder.getTimeFormatted());
    }

    @Test
    public void getTimeFormatted_doubleDigitMinute() {
        reminder.setHour(8);
        reminder.setMinute(45);
        assertEquals("08:45", reminder.getTimeFormatted());
    }

    @Test
    public void getDisplayText_noMedicineNoCustom_returnsDefault() {
        assertEquals("Не забудьте принять лекарство", reminder.getDisplayText());
    }

    @Test
    public void getDisplayText_nullMedicineNullCustom_returnsDefault() {
        assertEquals("Не забудьте принять лекарство", reminder.getDisplayText());
    }

    @Test
    public void getDisplayText_emptyMedicineEmptyCustom_returnsDefault() {
        reminder.setMedicineName("");
        reminder.setCustomText("");
        assertEquals("Не забудьте принять лекарство", reminder.getDisplayText());
    }

    @Test
    public void getDisplayText_medicineOnly() {
        reminder.setMedicineName("Зодак");
        assertEquals("Не забудьте принять Зодак", reminder.getDisplayText());
    }

    @Test
    public void getDisplayText_emptyMedicineOnly() {
        reminder.setMedicineName("");
        assertEquals("Не забудьте принять лекарство", reminder.getDisplayText());
    }

    @Test
    public void getDisplayText_customTextOnly() {
        reminder.setCustomText("Пей таблетки!");
        assertEquals("Пей таблетки!", reminder.getDisplayText());
    }

    @Test
    public void getDisplayText_emptyCustomTextOnly() {
        reminder.setCustomText("");
        assertEquals("Не забудьте принять лекарство", reminder.getDisplayText());
    }

    @Test
    public void getDisplayText_customOverridesMedicine() {
        reminder.setMedicineName("Зодак");
        reminder.setCustomText("Свой текст");
        assertEquals("Свой текст", reminder.getDisplayText());
    }

    @Test
    public void getDisplayText_emptyCustomDoesNotOverrideMedicine() {
        reminder.setMedicineName("Аспирин");
        reminder.setCustomText("");
        assertEquals("Не забудьте принять Аспирин", reminder.getDisplayText());
    }

    @Test
    public void getDisplayText_nullCustomFallsBackToMedicine() {
        reminder.setMedicineName("Ибупрофен");
        reminder.setCustomText(null);
        assertEquals("Не забудьте принять Ибупрофен", reminder.getDisplayText());
    }

    @Test
    public void getDisplayText_nullMedicineWithCustomText() {
        reminder.setMedicineName(null);
        reminder.setCustomText("Напоминание");
        assertEquals("Напоминание", reminder.getDisplayText());
    }

    @Test
    public void allFieldsSet_correctValues() {
        reminder.setId(7);
        reminder.setHour(15);
        reminder.setMinute(45);
        reminder.setMedicineName("Парацетамол");
        reminder.setCustomText("После обеда");
        reminder.setEnabled(true);

        assertEquals(7, reminder.getId());
        assertEquals(15, reminder.getHour());
        assertEquals(45, reminder.getMinute());
        assertEquals("Парацетамол", reminder.getMedicineName());
        assertEquals("После обеда", reminder.getCustomText());
        assertTrue(reminder.isEnabled());
        assertEquals("15:45", reminder.getTimeFormatted());
        assertEquals("После обеда", reminder.getDisplayText());
    }

    @Test
    public void allFieldsSet_disabledReminder() {
        reminder.setId(3);
        reminder.setHour(8);
        reminder.setMinute(0);
        reminder.setMedicineName("Витамин D");
        reminder.setEnabled(false);

        assertEquals(3, reminder.getId());
        assertEquals("08:00", reminder.getTimeFormatted());
        assertEquals("Не забудьте принять Витамин D", reminder.getDisplayText());
        assertFalse(reminder.isEnabled());
    }

    @Test
    public void setFieldsMultipleTimes_lastValueWins() {
        reminder.setMedicineName("А");
        reminder.setMedicineName("Б");
        assertEquals("Б", reminder.getMedicineName());

        reminder.setHour(10);
        reminder.setHour(22);
        assertEquals(22, reminder.getHour());

        reminder.setEnabled(true);
        reminder.setEnabled(false);
        assertFalse(reminder.isEnabled());
    }
}
