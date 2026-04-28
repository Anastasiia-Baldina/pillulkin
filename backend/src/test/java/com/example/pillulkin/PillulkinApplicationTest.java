package com.example.pillulkin;

import com.example.pillulkin.service.CsvImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class PillulkinApplicationTest {

    @Autowired
    private CsvImportService csvImportService;

    @Test
    void shouldRunImportMedicinesWithZeroCount() {
        PillulkinApplication app = new PillulkinApplication();
        CommandLineRunner runner = app.importMedicines(csvImportService);
        assertDoesNotThrow(() -> runner.run());
    }

    @Test
    void shouldRunImportMedicinesWithPositiveCount() {
        CsvImportService mockService = mock(CsvImportService.class);
        when(mockService.importMedicines()).thenReturn(42);

        PillulkinApplication app = new PillulkinApplication();
        CommandLineRunner runner = app.importMedicines(mockService);
        assertDoesNotThrow(() -> runner.run());
    }
}
