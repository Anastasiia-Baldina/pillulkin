package com.example.pillulkin.service;

import com.example.pillulkin.entity.Patient;
import com.example.pillulkin.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private GoogleAuthService googleAuthService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(googleAuthService, "googleClientId", "test-client-id.apps.googleusercontent.com");
    }

    @Test
    void shouldFindExistingPatientByEmail() {
        String email = "user@gmail.com";
        Patient existing = Patient.builder()
                .id(1L)
                .email(email)
                .passwordHash("hash")
                .createdAt(LocalDateTime.now())
                .build();

        when(patientRepository.findByEmail(email)).thenReturn(Optional.of(existing));

        Patient result = googleAuthService.findOrCreatePatient(email, "User Name");

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(1L, result.getId());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void shouldCreateNewPatientWhenNotFound() {
        String email = "new@gmail.com";
        Patient saved = Patient.builder()
                .id(2L)
                .email(email)
                .passwordHash("randomHash")
                .createdAt(LocalDateTime.now())
                .build();

        when(patientRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordService.hash(anyString())).thenReturn("randomHash");
        when(patientRepository.save(any(Patient.class))).thenReturn(saved);

        Patient result = googleAuthService.findOrCreatePatient(email, "New User");

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertThrows(IllegalArgumentException.class, () -> googleAuthService.verifyToken("invalid-token"));
    }

    @Test
    void shouldRejectEmptyToken() {
        assertThrows(IllegalArgumentException.class, () -> googleAuthService.verifyToken(""));
    }

    @Test
    void shouldRejectNullToken() {
        assertThrows(IllegalArgumentException.class, () -> googleAuthService.verifyToken(null));
    }
}
