package com.example.pillulkin.service;

import com.example.pillulkin.dto.DiagnosisRequest;
import com.example.pillulkin.dto.DiagnosisResponse;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiagnosisServiceTest {

    private final DiagnosisService service = new DiagnosisService();

    @Test
    void shouldReturnColdWhenSymptomsChecked() {
        DiagnosisRequest request = DiagnosisRequest.builder()
                .symptoms(Arrays.asList(1, 1, 0, 1, 0, 0, 1, 0))
                .build();

        DiagnosisResponse response = service.diagnose(request);

        assertEquals("Простуда", response.getDiagnosis());
        assertTrue(response.getConfidence() > 0);
    }

    @Test
    void shouldReturnNoDataWhenAllZeros() {
        DiagnosisRequest request = DiagnosisRequest.builder()
                .symptoms(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0))
                .build();

        DiagnosisResponse response = service.diagnose(request);

        assertEquals("Недостаточно данных для диагноза", response.getDiagnosis());
        assertEquals(0.0, response.getConfidence());
    }

    @Test
    void shouldReturnColdWhenSingleSymptomChecked() {
        DiagnosisRequest request = DiagnosisRequest.builder()
                .symptoms(Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0))
                .build();

        DiagnosisResponse response = service.diagnose(request);

        assertNotNull(response.getDiagnosis());
        assertTrue(response.getConfidence() > 0);
    }

    @Test
    void shouldReturnColdWhenAllSymptomsChecked() {
        DiagnosisRequest request = DiagnosisRequest.builder()
                .symptoms(Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1))
                .build();

        DiagnosisResponse response = service.diagnose(request);

        assertEquals("Простуда", response.getDiagnosis());
        assertTrue(response.getConfidence() > 0);
    }
}
