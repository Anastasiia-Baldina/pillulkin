package com.example.pillulkin.controller;

import com.example.pillulkin.client.MlServiceClient;
import com.example.pillulkin.dto.DiagnosisRequest;
import com.example.pillulkin.dto.MlDiagnosisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DiagnosisControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MlServiceClient mlServiceClient;

    @Test
    void shouldReturnSuggestedQuestionsOnInitialStep() throws Exception {
        MlDiagnosisResponse mlResponse = MlDiagnosisResponse.builder()
                .suggested_questions(List.of("есть_ли_температура", "сколько_дней"))
                .predictions(Collections.emptyList())
                .build();
        when(mlServiceClient.predict(any())).thenReturn(mlResponse);

        DiagnosisRequest request = DiagnosisRequest.builder()
                .symptoms(List.of("головная боль", "кашель"))
                .step("initial")
                .answers(null)
                .build();

        mockMvc.perform(post("/api/v1/diagnose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestedQuestions").isArray())
                .andExpect(jsonPath("$.suggestedQuestions[0]").value("есть ли температура"))
                .andExpect(jsonPath("$.suggestedQuestions[1]").value("сколько дней"))
                .andExpect(jsonPath("$.final").value(false));
    }

    @Test
    void shouldReturnDiagnosisOnFinalStep() throws Exception {
        MlDiagnosisResponse.Prediction prediction = MlDiagnosisResponse.Prediction.builder()
                .disease("ОРВИ")
                .probability(0.87)
                .build();

        MlDiagnosisResponse mlResponse = MlDiagnosisResponse.builder()
                .predictions(List.of(prediction))
                .suggested_questions(Collections.emptyList())
                .build();
        when(mlServiceClient.predict(any())).thenReturn(mlResponse);

        DiagnosisRequest request = DiagnosisRequest.builder()
                .symptoms(List.of("насморк", "кашель"))
                .step("final")
                .answers(List.of("температура 37.5", "3 дня"))
                .build();

        mockMvc.perform(post("/api/v1/diagnose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosis").value("ОРВИ"))
                .andExpect(jsonPath("$.confidence").value(0.87))
                .andExpect(jsonPath("$.final").value(true));
    }

    @Test
    void shouldHandleNullMlResponse() throws Exception {
        when(mlServiceClient.predict(any())).thenReturn(null);

        DiagnosisRequest request = DiagnosisRequest.builder()
                .symptoms(List.of("головная боль"))
                .step("initial")
                .build();

        mockMvc.perform(post("/api/v1/diagnose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.final").value(false))
                .andExpect(jsonPath("$.suggestedQuestions").isEmpty());
    }

    @Test
    void shouldReturnDefaultDiagnosisWhenNoPredictions() throws Exception {
        MlDiagnosisResponse mlResponse = MlDiagnosisResponse.builder()
                .predictions(Collections.emptyList())
                .suggested_questions(Collections.emptyList())
                .build();
        when(mlServiceClient.predict(any())).thenReturn(mlResponse);

        DiagnosisRequest request = DiagnosisRequest.builder()
                .symptoms(List.of("симптом"))
                .step("final")
                .build();

        mockMvc.perform(post("/api/v1/diagnose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosis").value("Не удалось определить диагноз"))
                .andExpect(jsonPath("$.confidence").value(0.0));
    }

    @Test
    void shouldRejectEmptySymptoms() throws Exception {
        DiagnosisRequest request = DiagnosisRequest.builder()
                .symptoms(Collections.emptyList())
                .step("initial")
                .build();

        mockMvc.perform(post("/api/v1/diagnose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeduplicateAnswers() throws Exception {
        MlDiagnosisResponse.Prediction prediction = MlDiagnosisResponse.Prediction.builder()
                .disease("Грипп")
                .probability(0.75)
                .build();

        MlDiagnosisResponse mlResponse = MlDiagnosisResponse.builder()
                .predictions(List.of(prediction))
                .suggested_questions(Collections.emptyList())
                .build();
        when(mlServiceClient.predict(any())).thenReturn(mlResponse);

        DiagnosisRequest request = DiagnosisRequest.builder()
                .symptoms(List.of("головная боль"))
                .step("final")
                .answers(List.of("головная боль", "температура"))
                .build();

        mockMvc.perform(post("/api/v1/diagnose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosis").value("Грипп"));
    }
}
