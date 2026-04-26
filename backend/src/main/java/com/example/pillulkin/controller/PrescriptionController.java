package com.example.pillulkin.controller;

import com.example.pillulkin.dto.PrescriptionRequest;
import com.example.pillulkin.dto.PrescriptionResponse;
import com.example.pillulkin.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients/{patientId}/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @PostMapping
    public ResponseEntity<PrescriptionResponse> createPrescription(
            @PathVariable Long patientId,
            @RequestBody PrescriptionRequest request) {
        return ResponseEntity.ok(prescriptionService.createPrescription(patientId, request));
    }

    @GetMapping
    public ResponseEntity<List<PrescriptionResponse>> getPrescriptions(
            @PathVariable Long patientId,
            @RequestParam(required = false) String status) {
        if ("active".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(prescriptionService.getActivePrescriptions(patientId));
        }
        return ResponseEntity.ok(prescriptionService.getAllPrescriptions(patientId));
    }

    @PutMapping("/{prescriptionId}/move-to-cabinet")
    public ResponseEntity<Void> moveToCabinet(
            @PathVariable Long patientId,
            @PathVariable Long prescriptionId) {
        prescriptionService.moveToCabinet(patientId, prescriptionId);
        return ResponseEntity.ok().build();
    }
}
