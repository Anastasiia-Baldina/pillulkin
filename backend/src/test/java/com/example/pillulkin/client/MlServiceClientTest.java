package com.example.pillulkin.client;

import com.example.pillulkin.dto.MlDiagnosisRequest;
import com.example.pillulkin.dto.MlDiagnosisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MlServiceClientTest {

    private MlServiceClient mlServiceClient;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        mlServiceClient = new MlServiceClient("http://localhost:8001");
        ReflectionTestUtils.setField(mlServiceClient, "restTemplate", restTemplate);
    }

    @Test
    void shouldReturnPredictionOnSuccess() {
        MlDiagnosisResponse expected = MlDiagnosisResponse.builder()
                .predictions(List.of())
                .suggested_questions(List.of("q1"))
                .build();

        when(restTemplate.postForObject(anyString(), any(), eq(MlDiagnosisResponse.class)))
                .thenReturn(expected);

        MlDiagnosisRequest request = MlDiagnosisRequest.builder()
                .symptoms(List.of("headache"))
                .build();

        MlDiagnosisResponse result = mlServiceClient.predict(request);

        assertNotNull(result);
        assertEquals(1, result.getSuggested_questions().size());
    }

    @Test
    void shouldReturnNullOnException() {
        when(restTemplate.postForObject(anyString(), any(), eq(MlDiagnosisResponse.class)))
                .thenThrow(new RestClientException("Connection refused"));

        MlDiagnosisRequest request = MlDiagnosisRequest.builder()
                .symptoms(List.of("headache"))
                .build();

        MlDiagnosisResponse result = mlServiceClient.predict(request);

        assertNull(result);
    }

    @Test
    void shouldConstructWithUrl() {
        MlServiceClient client = new MlServiceClient("http://test:1234");
        String url = (String) ReflectionTestUtils.getField(client, "mlServiceUrl");
        assertEquals("http://test:1234", url);
    }
}
