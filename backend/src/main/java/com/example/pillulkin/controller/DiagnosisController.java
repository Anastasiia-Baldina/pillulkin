package com.example.pillulkin.controller;

import com.example.pillulkin.dto.DiagnosisRequest;
import com.example.pillulkin.dto.DiagnosisResponse;
import com.example.pillulkin.service.DiagnosisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/diagnose")
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    @PostMapping
    public ResponseEntity<DiagnosisResponse> diagnose(@Valid @RequestBody DiagnosisRequest request) {
        return ResponseEntity.ok(diagnosisService.diagnose(request));
    }
}
