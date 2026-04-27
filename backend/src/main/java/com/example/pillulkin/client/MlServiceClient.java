package com.example.pillulkin.client;

import com.example.pillulkin.dto.MlDiagnosisRequest;
import com.example.pillulkin.dto.MlDiagnosisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class MlServiceClient {

    private final RestTemplate restTemplate;
    private final String mlServiceUrl;

    public MlServiceClient(@Value("${ml.service.url:http://localhost:8001}") String mlServiceUrl) {
        this.mlServiceUrl = mlServiceUrl;
        this.restTemplate = new RestTemplate();
    }

    public MlDiagnosisResponse predict(MlDiagnosisRequest request) {
        String url = mlServiceUrl + "/predict";
        log.info("Calling ML service: {}", url);
        try {
            return restTemplate.postForObject(url, request, MlDiagnosisResponse.class);
        } catch (Exception e) {
            log.error("Failed to call ML service: {}", e.getMessage());
            return null;
        }
    }
}
