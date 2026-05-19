package com.example.pillulkin.controller;

import com.example.pillulkin.dto.*;
import com.example.pillulkin.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/{patientId}/profile")
    public ResponseEntity<PatientProfileResponse> getProfile(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientService.getProfile(patientId));
    }

    @PutMapping("/{patientId}/profile")
    public ResponseEntity<PatientProfileResponse> updateProfile(
            @PathVariable Long patientId,
            @RequestBody PatientProfileRequest request) {
        return ResponseEntity.ok(patientService.updateProfile(patientId, request));
    }

    @GetMapping("/{patientId}/symptoms")
    public ResponseEntity<List<PatientSymptomResponse>> getSymptoms(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientService.getSymptoms(patientId));
    }

    @PostMapping("/{patientId}/symptoms")
    public ResponseEntity<PatientSymptomResponse> addSymptom(
            @PathVariable Long patientId,
            @Valid @RequestBody PatientSymptomRequest request) {
        return ResponseEntity.ok(patientService.addSymptom(patientId, request.getSymptom()));
    }

    @DeleteMapping("/{patientId}/symptoms/{symptomId}")
    public ResponseEntity<Void> removeSymptom(
            @PathVariable Long patientId,
            @PathVariable Long symptomId) {
        patientService.removeSymptom(patientId, symptomId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{patientId}/symptoms/{symptomId}/renew")
    public ResponseEntity<PatientSymptomResponse> renewSymptom(
            @PathVariable Long patientId,
            @PathVariable Long symptomId) {
        return ResponseEntity.ok(patientService.renewSymptom(patientId, symptomId));
    }

    @GetMapping("/{patientId}/medicines")
    public ResponseEntity<List<PatientMedicineResponse>> getMedicines(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientService.getMedicines(patientId));
    }

    @PostMapping("/{patientId}/medicines")
    public ResponseEntity<PatientMedicineResponse> addMedicine(
            @PathVariable Long patientId,
            @RequestBody PatientMedicineRequest request) {
        return ResponseEntity.ok(patientService.addMedicine(patientId, request));
    }

    @PostMapping("/{patientId}/medicines/custom")
    public ResponseEntity<PatientMedicineResponse> addCustomMedicine(
            @PathVariable Long patientId,
            @Valid @RequestBody CustomMedicineRequest request) {
        return ResponseEntity.ok(patientService.addCustomMedicine(patientId, request));
    }

    @DeleteMapping("/{patientId}/medicines/{medicineId}")
    public ResponseEntity<Void> removeMedicine(
            @PathVariable Long patientId,
            @PathVariable Long medicineId) {
        patientService.removeMedicine(patientId, medicineId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{patientId}/medicines/{patientMedicineId}")
    public ResponseEntity<PatientMedicineResponse> updateMedicine(
            @PathVariable Long patientId,
            @PathVariable Long patientMedicineId,
            @RequestBody PatientMedicineRequest request) {
        return ResponseEntity.ok(patientService.updateMedicine(patientId, patientMedicineId, request));
    }
}