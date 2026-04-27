package com.example.pillulkin.data.local.entity;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReminderTest {

    @Test
    public void displayText_withCustomText() {
        Reminder r = new Reminder();
        r.setCustomText("Пей таблетки!");
        assertEquals("Пей таблетки!", r.getDisplayText());
    }

    @Test
    public void displayText_withMedicineName() {
        Reminder r = new Reminder();
        r.setMedicineName("Зодак");
        assertEquals("Не забудьте принять Зодак", r.getDisplayText());
    }

    @Test
    public void displayText_default() {
        Reminder r = new Reminder();
        assertEquals("Не забудьте принять лекарство", r.getDisplayText());
    }

    @Test
    public void displayText_customOverridesMedicine() {
        Reminder r = new Reminder();
        r.setMedicineName("Зодак");
        r.setCustomText("Свой текст");
        assertEquals("Свой текст", r.getDisplayText());
    }

    @Test
    public void timeFormatted() {
        Reminder r = new Reminder();
        r.setHour(9);
        r.setMinute(5);
        assertEquals("09:05", r.getTimeFormatted());
    }

    @Test
    public void timeFormatted_midnight() {
        Reminder r = new Reminder();
        r.setHour(0);
        r.setMinute(0);
        assertEquals("00:00", r.getTimeFormatted());
    }

    @Test
    public void timeFormatted_evening() {
        Reminder r = new Reminder();
        r.setHour(21);
        r.setMinute(30);
        assertEquals("21:30", r.getTimeFormatted());
    }
}
