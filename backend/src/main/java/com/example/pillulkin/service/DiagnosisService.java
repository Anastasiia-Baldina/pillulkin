package com.example.pillulkin.service;

import com.example.pillulkin.dto.DiagnosisRequest;
import com.example.pillulkin.dto.DiagnosisResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiagnosisService {

    public DiagnosisResponse diagnose(DiagnosisRequest request) {
        List<Integer> symptoms = request.getSymptoms();
        long checkedCount = symptoms.stream().filter(v -> v == 1).count();

        if (checkedCount == 0) {
            return DiagnosisResponse.builder()
                    .diagnosis("Недостаточно данных для диагноза")
                    .confidence(0.0)
                    .build();
        }

        return DiagnosisResponse.builder()
                .diagnosis("Простуда")
                .confidence(0.85)
                .build();
    }
}
