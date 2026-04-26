package com.example.pillulkin.service;

import com.example.pillulkin.dto.*;
import com.example.pillulkin.entity.*;
import com.example.pillulkin.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorAccessService {

    private final PatientRepository patientRepository;
    private final DoctorAccessCodeRepository doctorAccessCodeRepository;
    private final DoctorAccessSessionRepository doctorAccessSessionRepository;
    private final PatientService patientService;
    private final PasswordService passwordService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CODE_CHARS = "0123456789";

    @Transactional
    public String generateCode(Long patientId, Integer expiresInMinutes) {
        patientRepository.getReferenceById(patientId);

        String code = generateRandomCode(6);
        String codeHash = passwordService.hash(code);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(expiresInMinutes != null ? expiresInMinutes : 60);

        DoctorAccessCode accessCode = DoctorAccessCode.builder()
                .patient(patientRepository.getReferenceById(patientId))
                .codeHash(codeHash)
                .createdAt(now)
                .expiresAt(expiresAt)
                .status("ACTIVE")
                .build();
        doctorAccessCodeRepository.save(accessCode);

        return code;
    }

    @Transactional
    public DoctorLoginResult loginWithCode(String code) {
        List<DoctorAccessCode> activeCodes = doctorAccessCodeRepository.findActiveCodes(LocalDateTime.now());

        DoctorAccessCode accessCode = activeCodes.stream()
                .filter(ac -> passwordService.matches(code, ac.getCodeHash()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired code"));

        doctorAccessCodeRepository.markAsUsed(accessCode.getId());

        String token = java.util.UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);

        DoctorAccessSession session = DoctorAccessSession.builder()
                .doctorAccessCode(accessCode)
                .token(token)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .build();
        doctorAccessSessionRepository.save(session);

        Long patientId = accessCode.getPatient().getId();
        return new DoctorLoginResult(token, patientId);
    }

    public record DoctorLoginResult(String token, Long patientId) {}

    public boolean validateToken(String token) {
        return doctorAccessSessionRepository.findValidSession(token, LocalDateTime.now()).isPresent();
    }

    public DoctorFullDataResponse getPatientFullData(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        PatientProfileResponse profile = patientService.getProfile(patientId);
        List<PatientSymptomResponse> symptoms = patientService.getSymptoms(patientId);
        List<PatientMedicineResponse> medicines = patientService.getMedicines(patientId);

        return DoctorFullDataResponse.builder()
                .patientId(patientId)
                .email(patient.getEmail())
                .profile(profile)
                .symptoms(symptoms)
                .medicines(medicines)
                .build();
    }

    private String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}