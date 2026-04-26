package com.example.pillulkin.service;

import com.example.pillulkin.dto.ReferenceMedicineResponse;
import com.example.pillulkin.entity.ReferenceMedicine;
import com.example.pillulkin.repository.ReferenceMedicineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReferenceMedicineServiceTest {

    @Mock
    private ReferenceMedicineRepository referenceMedicineRepository;

    @InjectMocks
    private ReferenceMedicineService referenceMedicineService;

    private ReferenceMedicine testMedicine;

    @BeforeEach
    void setUp() {
        testMedicine = ReferenceMedicine.builder()
                .id(1L)
                .name("Test Medicine")
                .dosage("10mg")
                .form("tablet")
                .activeSubstance("test substance")
                .indications("test indications")
                .contraindications("test contraindications")
                .category("Test Category")
                .build();
    }

    @Test
    void shouldGetAllMedicines() {
        when(referenceMedicineRepository.findAll()).thenReturn(Arrays.asList(testMedicine));

        List<ReferenceMedicineResponse> result = referenceMedicineService.getAllMedicines();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Medicine", result.get(0).getName());
    }

    @Test
    void shouldGetMedicinesByCategory() {
        when(referenceMedicineRepository.findByCategory("Test Category"))
                .thenReturn(Arrays.asList(testMedicine));

        List<ReferenceMedicineResponse> result =
                referenceMedicineService.getMedicinesByCategory("Test Category");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Category", result.get(0).getCategory());
    }

    @Test
    void shouldSearchMedicines() {
        when(referenceMedicineRepository.searchMedicines("test"))
                .thenReturn(Arrays.asList(testMedicine));

        List<ReferenceMedicineResponse> result =
                referenceMedicineService.searchMedicines("test");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetRecommendations() {
        List<String> symptoms = Arrays.asList("headache", "fever");

        when(referenceMedicineRepository.searchMedicines("headache fever"))
                .thenReturn(Arrays.asList(testMedicine));

        List<ReferenceMedicineResponse> result =
                referenceMedicineService.getRecommendations(symptoms);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnEmptyListForEmptySymptoms() {
        List<ReferenceMedicineResponse> result =
                referenceMedicineService.getRecommendations(Collections.emptyList());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCountMedicines() {
        when(referenceMedicineRepository.count()).thenReturn(100L);

        long count = referenceMedicineService.count();

        assertEquals(100L, count);
    }
}