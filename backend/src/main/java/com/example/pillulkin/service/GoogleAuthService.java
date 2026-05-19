package com.example.pillulkin.service;

import com.example.pillulkin.entity.Patient;
import com.example.pillulkin.repository.PatientRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final PatientRepository patientRepository;
    private final PasswordService passwordService;

    @Value("${google.client.id:}")
    private String googleClientId;

    public GoogleUserInfo verifyToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new IllegalArgumentException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            return new GoogleUserInfo(email, name);
        } catch (Exception e) {
            log.error("Google token verification failed", e);
            throw new IllegalArgumentException("Google token verification failed: " + (e.getMessage() != null ? e.getMessage() : "unknown error"));
        }
    }

    public Patient findOrCreatePatient(String email, String name) {
        return patientRepository.findByEmail(email)
                .orElseGet(() -> {
                    Patient patient = Patient.builder()
                            .email(email)
                            .passwordHash(passwordService.hash(java.util.UUID.randomUUID().toString()))
                            .createdAt(LocalDateTime.now())
                            .build();
                    return patientRepository.save(patient);
                });
    }

    public record GoogleUserInfo(String email, String name) {}
}
