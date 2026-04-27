package com.example.pillulkin.service;

import com.example.pillulkin.client.MlServiceClient;
import com.example.pillulkin.dto.DiagnosisRequest;
import com.example.pillulkin.dto.DiagnosisResponse;
import com.example.pillulkin.dto.MlDiagnosisResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagnosisServiceTest {

    @Mock
    private MlServiceClient mlServiceClient;

    @InjectMocks
    private DiagnosisService service;

    @Test
    void shouldReturnQuestionsOnInitialStep() {
        MlDiagnosisResponse mlResponse = MlDiagnosisResponse.builder()
                .predictions(List.of())
                .suggested_questions(List.of("есть_ли_температура", "болит_ли_горло"))
                .build();
        when(mlServiceClient.predict(any())).thenReturn(mlResponse);

        DiagnosisRequest request = DiagnosisRequest.builder()
                .symptoms(List.of("кашель", "насморк"))
                .step("initial")
                .build();

        DiagnosisResponse response = service.diagnose(request);

        assertFalse(response.isFinal());
        assertEquals(2, response.getSuggestedQuestions().size());
        assertTrue(response.getSuggestedQuestions().contains("есть ли температура"));
        assertTrue(response.getSuggestedQuestions().contains("болит ли горло"));
    }

    @Test
    void shouldReturnDiagnosisOnFinalStep() {
        MlDiagnosisResponse mlResponse = MlDiagnosisResponse.builder()
                .predictions(List.of(
                        MlDiagnosisResponse.Prediction.builder()
                                .disease("Простуда")
                                .probability(0.85)
                                .build()
                ))
                .suggested_questions(List.of())
                .build();
        when(mlServiceClient.predict(any())).thenReturn(mlResponse);

        DiagnosisRequest request = DiagnosisRequest.builder()
                .symptoms(List.of("кашель", "насморк"))
                .step("final")
                .build();

        DiagnosisResponse response = service.diagnose(request);

        assertTrue(response.isFinal());
        assertEquals("Простуда", response.getDiagnosis());
        assertEquals(0.85, response.getConfidence(), 0.001);
        assertTrue(response.getSuggestedQuestions().isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenMlServiceUnavailable() {
        when(mlServiceClient.predict(any())).thenReturn(null);

        DiagnosisRequest request = DiagnosisRequest.builder()
                .symptoms(List.of("кашель"))
                .step("initial")
                .build();

        DiagnosisResponse response = service.diagnose(request);

        assertFalse(response.isFinal());
        assertTrue(response.getSuggestedQuestions().isEmpty());
    }

    @Test
    void shouldMergeAnswersIntoSymptoms() {
        MlDiagnosisResponse mlResponse = MlDiagnosisResponse.builder()
                .predictions(List.of(
                        MlDiagnosisResponse.Prediction.builder()
                                .disease("Грипп")
                                .probability(0.72)
                                .build()
                ))
                .suggested_questions(List.of())
                .build();
        when(mlServiceClient.predict(any())).thenReturn(mlResponse);

        DiagnosisRequest request = DiagnosisRequest.builder()
                .symptoms(List.of("кашель"))
                .step("final")
                .answers(List.of("высокая температура"))
                .build();

        DiagnosisResponse response = service.diagnose(request);

        assertTrue(response.isFinal());
        assertEquals("Грипп", response.getDiagnosis());
        assertEquals(0.72, response.getConfidence(), 0.001);
    }

    @Test
    void shouldHandleEmptyPredictions() {
        MlDiagnosisResponse mlResponse = MlDiagnosisResponse.builder()
                .predictions(List.of())
                .suggested_questions(List.of())
                .build();
        when(mlServiceClient.predict(any())).thenReturn(mlResponse);

        DiagnosisRequest request = DiagnosisRequest.builder()
                .symptoms(List.of("что-то непонятное"))
                .step("final")
                .build();

        DiagnosisResponse response = service.diagnose(request);

        assertTrue(response.isFinal());
        assertEquals("Не удалось определить диагноз", response.getDiagnosis());
        assertEquals(0.0, response.getConfidence(), 0.001);
    }
}
