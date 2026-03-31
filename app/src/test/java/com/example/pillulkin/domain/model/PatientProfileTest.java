package com.example.pillulkin.domain.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class PatientProfileTest {
    @Test
    public void constructor_setsAllFields() {
        PatientProfile profile = new PatientProfile(1, "Иван", 30, "Пенициллин", "Язва", "Примечания");
        assertEquals(1, profile.getId());
        assertEquals("Иван", profile.getName());
        assertEquals(30, profile.getAge());
        assertEquals("Пенициллин", profile.getAllergies());
        assertEquals("Язва", profile.getContraindications());
        assertEquals("Примечания", profile.getNotes());
    }

    @Test
    public void setters_updateFields() {
        PatientProfile profile = new PatientProfile(0, "", 0, "", "", "");
        profile.setId(5);
        profile.setName("Мария");
        profile.setAge(25);
        profile.setAllergies("Аспирин");
        profile.setContraindications("Беременность");
        profile.setNotes("Заметка");

        assertEquals(5, profile.getId());
        assertEquals("Мария", profile.getName());
        assertEquals(25, profile.getAge());
        assertEquals("Аспирин", profile.getAllergies());
        assertEquals("Беременность", profile.getContraindications());
        assertEquals("Заметка", profile.getNotes());
    }
}
