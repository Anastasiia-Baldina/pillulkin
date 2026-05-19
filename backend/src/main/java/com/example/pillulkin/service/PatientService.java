package com.example.pillulkin.service;

import com.example.pillulkin.dto.*;
import com.example.pillulkin.entity.*;
import com.example.pillulkin.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PatientSymptomRepository patientSymptomRepository;
    private final PatientMedicineRepository patientMedicineRepository;
    private final ReferenceMedicineRepository referenceMedicineRepository;

    @Transactional(readOnly = true)
    public PatientProfileResponse getProfile(Long patientId) {
        PatientProfile profile = patientProfileRepository.findByPatientId(patientId)
                .orElseGet(() -> PatientProfile.builder()
                        .patient(patientRepository.getReferenceById(patientId))
                        .build());
        return mapToProfileResponse(profile);
    }

    @Transactional
    public PatientProfileResponse updateProfile(Long patientId, PatientProfileRequest request) {
        Patient patient = patientRepository.getReferenceById(patientId);
        PatientProfile profile = patientProfileRepository.findByPatientId(patientId)
                .orElseGet(() -> PatientProfile.builder()
                        .patient(patient)
                        .build());

        if (request.getName() != null) profile.setName(request.getName());
        if (request.getAge() != null) profile.setAge(request.getAge());
        if (request.getAllergies() != null) profile.setAllergies(request.getAllergies());
        if (request.getContraindications() != null) profile.setContraindications(request.getContraindications());
        if (request.getNotes() != null) profile.setNotes(request.getNotes());

        profile = patientProfileRepository.save(profile);
        return mapToProfileResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<PatientSymptomResponse> getSymptoms(Long patientId) {
        return patientSymptomRepository.findByPatientIdOrderByTimestampDesc(patientId)
                .stream()
                .map(this::mapToSymptomResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PatientSymptomResponse addSymptom(Long patientId, String symptom) {
        Patient patient = patientRepository.getReferenceById(patientId);
        PatientSymptom symptomEntity = PatientSymptom.builder()
                .patient(patient)
                .symptom(symptom)
                .timestamp(LocalDateTime.now())
                .build();
        symptomEntity = patientSymptomRepository.save(symptomEntity);
        return mapToSymptomResponse(symptomEntity);
    }

    @Transactional
    public void removeSymptom(Long patientId, Long symptomId) {
        PatientSymptom symptom = patientSymptomRepository.findById(symptomId)
                .orElseThrow(() -> new IllegalArgumentException("Symptom not found"));
        if (!symptom.getPatient().getId().equals(patientId)) {
            throw new IllegalArgumentException("Symptom does not belong to patient");
        }
        patientSymptomRepository.delete(symptom);
    }

    @Transactional
    public PatientSymptomResponse renewSymptom(Long patientId, Long symptomId) {
        PatientSymptom symptom = patientSymptomRepository.findById(symptomId)
                .orElseThrow(() -> new IllegalArgumentException("Symptom not found"));
        if (!symptom.getPatient().getId().equals(patientId)) {
            throw new IllegalArgumentException("Symptom does not belong to patient");
        }
        symptom.setTimestamp(LocalDateTime.now());
        symptom = patientSymptomRepository.save(symptom);
        return mapToSymptomResponse(symptom);
    }

    @Transactional(readOnly = true)
    public List<PatientMedicineResponse> getMedicines(Long patientId) {
        return patientMedicineRepository.findByPatientId(patientId)
                .stream()
                .map(this::mapToMedicineResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PatientMedicineResponse addMedicine(Long patientId, PatientMedicineRequest request) {
        Patient patient = patientRepository.getReferenceById(patientId);

        if (request.getMedicineId() != null) {
            if (patientMedicineRepository.existsByPatientIdAndReferenceMedicineId(patientId, request.getMedicineId())) {
                throw new IllegalArgumentException("Medicine already in kit");
            }
        }

        ReferenceMedicine referenceMedicine = request.getMedicineId() != null
                ? referenceMedicineRepository.getReferenceById(request.getMedicineId())
                : null;

        PatientMedicine patientMedicine = PatientMedicine.builder()
                .patient(patient)
                .referenceMedicine(referenceMedicine)
                .addedAt(LocalDateTime.now())
                .expirationDate(request.getExpirationDate())
                .quantity(request.getQuantity())
                .build();
        patientMedicine = patientMedicineRepository.save(patientMedicine);
        return mapToMedicineResponse(patientMedicine);
    }

    @Transactional
    public PatientMedicineResponse addCustomMedicine(Long patientId, CustomMedicineRequest request) {
        Patient patient = patientRepository.getReferenceById(patientId);

        PatientMedicine patientMedicine = PatientMedicine.builder()
                .patient(patient)
                .referenceMedicine(null)
                .medicineName(request.getName())
                .medicineDosage(request.getDosage())
                .medicineForm(request.getForm())
                .medicineActiveSubstance(request.getActiveSubstance())
                .addedAt(LocalDateTime.now())
                .expirationDate(request.getExpirationDate())
                .quantity(request.getQuantity())
                .build();
        patientMedicine = patientMedicineRepository.save(patientMedicine);
        return mapToMedicineResponse(patientMedicine);
    }

    @Transactional
    public void removeMedicine(Long patientId, Long medicineId) {
        PatientMedicine medicine = patientMedicineRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found in kit"));
        if (!medicine.getPatient().getId().equals(patientId)) {
            throw new IllegalArgumentException("Medicine does not belong to patient");
        }
        patientMedicineRepository.delete(medicine);
    }

    @Transactional
    public PatientMedicineResponse updateMedicine(Long patientId, Long patientMedicineId, PatientMedicineRequest request) {
        PatientMedicine medicine = patientMedicineRepository.findById(patientMedicineId)
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found"));
        if (!medicine.getPatient().getId().equals(patientId)) {
            throw new IllegalArgumentException("Medicine does not belong to patient");
        }
        if (request.getExpirationDate() != null) {
            medicine.setExpirationDate(request.getExpirationDate());
        }
        if (request.getQuantity() != null) {
            medicine.setQuantity(request.getQuantity());
        }
        medicine = patientMedicineRepository.save(medicine);
        return mapToMedicineResponse(medicine);
    }

    private PatientProfileResponse mapToProfileResponse(PatientProfile profile) {
        return PatientProfileResponse.builder()
                .id(profile.getId())
                .patientId(profile.getPatient() != null ? profile.getPatient().getId() : null)
                .name(profile.getName())
                .age(profile.getAge())
                .allergies(profile.getAllergies())
                .contraindications(profile.getContraindications())
                .notes(profile.getNotes())
                .build();
    }

    private PatientSymptomResponse mapToSymptomResponse(PatientSymptom symptom) {
        return PatientSymptomResponse.builder()
                .id(symptom.getId())
                .patientId(symptom.getPatient().getId())
                .symptom(symptom.getSymptom())
                .timestamp(symptom.getTimestamp())
                .build();
    }

    private PatientMedicineResponse mapToMedicineResponse(PatientMedicine medicine) {
        String medName = medicine.getReferenceMedicine() != null
                ? medicine.getReferenceMedicine().getName()
                : medicine.getMedicineName();
        String medDosage = medicine.getReferenceMedicine() != null
                ? medicine.getReferenceMedicine().getDosage()
                : medicine.getMedicineDosage();
        String medForm = medicine.getReferenceMedicine() != null
                ? medicine.getReferenceMedicine().getForm()
                : medicine.getMedicineForm();
        String medActiveSubstance = medicine.getReferenceMedicine() != null
                ? medicine.getReferenceMedicine().getActiveSubstance()
                : medicine.getMedicineActiveSubstance();
        Long medId = medicine.getReferenceMedicine() != null
                ? medicine.getReferenceMedicine().getId()
                : null;

        return PatientMedicineResponse.builder()
                .id(medicine.getId())
                .patientId(medicine.getPatient().getId())
                .medicineId(medId)
                .medicineName(medName)
                .dosage(medDosage)
                .form(medForm)
                .activeSubstance(medActiveSubstance)
                .addedAt(medicine.getAddedAt())
                .expirationDate(medicine.getExpirationDate())
                .quantity(medicine.getQuantity())
                .build();
    }
}