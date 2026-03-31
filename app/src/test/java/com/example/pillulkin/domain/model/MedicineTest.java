package com.example.pillulkin.domain.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class MedicineTest {
    @Test
    public void constructor_setsAllFields() {
        Medicine medicine = new Medicine(1, "Парацетамол", "500 мг", "12.12.2025", 20, "Таблетки", "При головной боли");
        assertEquals(1, medicine.getId());
        assertEquals("Парацетамол", medicine.getName());
        assertEquals("500 мг", medicine.getDosage());
        assertEquals("12.12.2025", medicine.getExpirationDate());
        assertEquals(20, medicine.getQuantity());
        assertEquals("Таблетки", medicine.getForm());
        assertEquals("При головной боли", medicine.getComment());
    }

    @Test
    public void setters_updateFields() {
        Medicine medicine = new Medicine(0, "", "", "", 0, "", "");
        medicine.setId(10);
        medicine.setName("Ибупрофен");
        medicine.setDosage("400 мг");
        medicine.setExpirationDate("01.06.2026");
        medicine.setQuantity(30);
        medicine.setForm("Капсулы");
        medicine.setComment("От боли");

        assertEquals(10, medicine.getId());
        assertEquals("Ибупрофен", medicine.getName());
        assertEquals("400 мг", medicine.getDosage());
        assertEquals("01.06.2026", medicine.getExpirationDate());
        assertEquals(30, medicine.getQuantity());
        assertEquals("Капсулы", medicine.getForm());
        assertEquals("От боли", medicine.getComment());
    }
}
