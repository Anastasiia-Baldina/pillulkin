package com.example.pillulkin.service;

import com.example.pillulkin.entity.Patient;
import com.example.pillulkin.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterNewPatient() {
        String email = "test@example.com";
        String password = "password123";
        Patient savedPatient = Patient.builder()
                .id(1L)
                .email(email)
                .passwordHash("hashedPassword")
                .createdAt(LocalDateTime.now())
                .build();

        when(patientRepository.existsByEmail(email)).thenReturn(false);
        when(passwordService.hash(password)).thenReturn("hashedPassword");
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        Patient result = authService.register(email, password);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        String email = "existing@example.com";
        String password = "password123";

        when(patientRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(email, password));
    }

    @Test
    void shouldLoginWithValidCredentials() {
        String email = "test@example.com";
        String password = "password123";
        Patient patient = Patient.builder()
                .id(1L)
                .email(email)
                .passwordHash("hashedPassword")
                .createdAt(LocalDateTime.now())
                .build();

        when(patientRepository.findByEmail(email)).thenReturn(Optional.of(patient));
        when(passwordService.matches(password, "hashedPassword")).thenReturn(true);

        Patient result = authService.login(email, password);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void shouldThrowExceptionWithInvalidCredentials() {
        String email = "test@example.com";
        String password = "wrongPassword";
        Patient patient = Patient.builder()
                .id(1L)
                .email(email)
                .passwordHash("hashedPassword")
                .createdAt(LocalDateTime.now())
                .build();

        when(patientRepository.findByEmail(email)).thenReturn(Optional.of(patient));
        when(passwordService.matches(password, "hashedPassword")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.login(email, password));
    }

    @Test
    void shouldGenerateToken() {
        String token = authService.generateToken(1L);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }
}