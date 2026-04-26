package com.example.pillulkin.service;

import com.example.pillulkin.entity.Patient;
import com.example.pillulkin.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PatientRepository patientRepository;
    private final PasswordService passwordService;

    @Transactional
    public Patient register(String email, String password) {
        if (patientRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        Patient patient = Patient.builder()
                .email(email)
                .passwordHash(passwordService.hash(password))
                .createdAt(LocalDateTime.now())
                .build();
        return patientRepository.save(patient);
    }

    public Patient login(String email, String password) {
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordService.matches(password, patient.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return patient;
    }

    public String generateToken(Long patientId) {
        return UUID.randomUUID().toString();
    }
}