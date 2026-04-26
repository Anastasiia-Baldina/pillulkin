package com.example.pillulkin.controller;

import com.example.pillulkin.dto.DoctorFullDataResponse;
import com.example.pillulkin.service.DoctorAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorAccessService doctorAccessService;

    @GetMapping("/patients/{patientId}/full-data")
    public ResponseEntity<DoctorFullDataResponse> getPatientFullData(
            @PathVariable Long patientId,
            @RequestHeader("X-Doctor-Token") String token) {
        if (!doctorAccessService.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(doctorAccessService.getPatientFullData(patientId));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestHeader("X-Doctor-Token") String token) {
        boolean valid = doctorAccessService.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }
}