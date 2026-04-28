package com.example.pillulkin.service;

import com.example.pillulkin.repository.ReferenceMedicineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CsvImportServiceTest {

    @Mock private ReferenceMedicineRepository referenceMedicineRepository;
    @InjectMocks private CsvImportService csvImportService;

    @Test
    void shouldSkipImportWhenMedicinesExist() {
        when(referenceMedicineRepository.count()).thenReturn(100L);

        int result = csvImportService.importMedicines();

        assertEquals(0, result);
        verify(referenceMedicineRepository, never()).saveAll(any());
    }

    @Test
    void shouldImportFromCsvWhenEmpty() {
        when(referenceMedicineRepository.count()).thenReturn(0L);

        int result = csvImportService.importMedicines();

        assertTrue(result > 0);
        verify(referenceMedicineRepository).saveAll(any());
    }
}
