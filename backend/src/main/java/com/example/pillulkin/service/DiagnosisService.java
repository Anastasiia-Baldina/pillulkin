package com.example.pillulkin.service;

import com.example.pillulkin.client.MlServiceClient;
import com.example.pillulkin.dto.DiagnosisRequest;
import com.example.pillulkin.dto.DiagnosisResponse;
import com.example.pillulkin.dto.MlDiagnosisRequest;
import com.example.pillulkin.dto.MlDiagnosisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiagnosisService {

    private final MlServiceClient mlServiceClient;

    public DiagnosisResponse diagnose(DiagnosisRequest request) {
        List<String> allSymptoms = new ArrayList<>(request.getSymptoms());

        if (request.getAnswers() != null && !request.getAnswers().isEmpty()) {
            for (String answer : request.getAnswers()) {
                if (!allSymptoms.contains(answer)) {
                    allSymptoms.add(answer);
                }
            }
        }

        boolean isFinalStep = "final".equalsIgnoreCase(request.getStep());

        MlDiagnosisRequest mlRequest = MlDiagnosisRequest.builder()
                .symptoms(allSymptoms)
                .top_k(5)
                .max_questions(8)
                .build();

        MlDiagnosisResponse mlResponse = mlServiceClient.predict(mlRequest);

        if (mlResponse == null) {
            return DiagnosisResponse.builder()
                    .isFinal(false)
                    .suggestedQuestions(List.of())
                    .build();
        }

        List<String> questions = new ArrayList<>();
        if (mlResponse.getSuggested_questions() != null) {
            for (String q : mlResponse.getSuggested_questions()) {
                questions.add(q.replace("_", " "));
            }
        }

        if (!isFinalStep) {
            return DiagnosisResponse.builder()
                    .isFinal(false)
                    .suggestedQuestions(questions)
                    .build();
        }

        String diagnosis = "Не удалось определить диагноз";
        double confidence = 0.0;

        if (mlResponse.getPredictions() != null && !mlResponse.getPredictions().isEmpty()) {
            MlDiagnosisResponse.Prediction top = mlResponse.getPredictions().get(0);
            diagnosis = top.getDisease();
            confidence = top.getProbability();
        }

        return DiagnosisResponse.builder()
                .diagnosis(diagnosis)
                .confidence(confidence)
                .isFinal(true)
                .suggestedQuestions(List.of())
                .build();
    }
}
