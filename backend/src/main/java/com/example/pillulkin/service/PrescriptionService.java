package com.example.pillulkin.service;

import com.example.pillulkin.dto.PrescriptionRequest;
import com.example.pillulkin.dto.PrescriptionResponse;
import com.example.pillulkin.entity.Patient;
import com.example.pillulkin.entity.Prescription;
import com.example.pillulkin.entity.ReferenceMedicine;
import com.example.pillulkin.repository.PatientRepository;
import com.example.pillulkin.repository.PrescriptionRepository;
import com.example.pillulkin.repository.ReferenceMedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final ReferenceMedicineRepository referenceMedicineRepository;

    @Transactional
    public PrescriptionResponse createPrescription(Long patientId, PrescriptionRequest request) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        ReferenceMedicine medicine = referenceMedicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found"));

        String dosageInstructions = buildDosageText(request);

        Prescription prescription = Prescription.builder()
                .patient(patient)
                .referenceMedicine(medicine)
                .dosageInstructions(dosageInstructions)
                .frequency(request.getFrequency())
                .mealTiming(request.getMealTiming())
                .timeOffset(request.getTimeOffset())
                .customInstructions(request.getCustomInstructions())
                .prescribedAt(LocalDateTime.now())
                .status("ACTIVE")
                .build();

        prescription = prescriptionRepository.save(prescription);
        return mapToResponse(prescription);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getActivePrescriptions(Long patientId) {
        return prescriptionRepository.findByPatientIdAndStatusOrderByPrescribedAtDesc(patientId, "ACTIVE")
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getAllPrescriptions(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByPrescribedAtDesc(patientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void moveToCabinet(Long patientId, Long prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Prescription not found"));

        if (!prescription.getPatient().getId().equals(patientId)) {
            throw new IllegalArgumentException("Prescription does not belong to this patient");
        }

        prescription.setStatus("MOVED");
        prescriptionRepository.save(prescription);
    }

    private String buildDosageText(PrescriptionRequest request) {
        if (request.getCustomInstructions() != null && !request.getCustomInstructions().isEmpty()) {
            return request.getCustomInstructions();
        }

        StringBuilder sb = new StringBuilder();
        if (request.getFrequency() != null) {
            sb.append(request.getFrequency()).append(" раз в день");
        }
        if (request.getTimeOffset() != null && !request.getTimeOffset().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(request.getTimeOffset());
        }
        if (request.getMealTiming() != null && !request.getMealTiming().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(request.getMealTiming());
        }
        return sb.toString();
    }

    private PrescriptionResponse mapToResponse(Prescription p) {
        ReferenceMedicine med = p.getReferenceMedicine();
        return PrescriptionResponse.builder()
                .id(p.getId())
                .patientId(p.getPatient().getId())
                .medicineId(med.getId())
                .medicineName(med.getName())
                .dosage(med.getDosage())
                .form(med.getForm())
                .activeSubstance(med.getActiveSubstance())
                .dosageInstructions(p.getDosageInstructions())
                .frequency(p.getFrequency())
                .mealTiming(p.getMealTiming())
                .timeOffset(p.getTimeOffset())
                .customInstructions(p.getCustomInstructions())
                .prescribedAt(p.getPrescribedAt())
                .status(p.getStatus())
                .build();
    }
}
