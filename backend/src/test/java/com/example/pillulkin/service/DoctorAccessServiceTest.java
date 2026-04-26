package com.example.pillulkin.service;

import com.example.pillulkin.dto.DoctorFullDataResponse;
import com.example.pillulkin.entity.*;
import com.example.pillulkin.repository.*;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DoctorAccessServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorAccessCodeRepository doctorAccessCodeRepository;

    @Mock
    private DoctorAccessSessionRepository doctorAccessSessionRepository;

    @Mock
    private PatientService patientService;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private DoctorAccessService doctorAccessService;

    private Patient testPatient;
    private DoctorAccessCode testAccessCode;
    private DoctorAccessSession testSession;

    @BeforeEach
    void setUp() {
        testPatient = Patient.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hash")
                .createdAt(LocalDateTime.now())
                .build();

        testAccessCode = DoctorAccessCode.builder()
                .id(1L)
                .patient(testPatient)
                .codeHash("hashedCode")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .status("ACTIVE")
                .build();

        testSession = DoctorAccessSession.builder()
                .id(1L)
                .doctorAccessCode(testAccessCode)
                .token("testToken123")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldGenerateCode() {
        when(patientRepository.getReferenceById(1L))
                .thenReturn(testPatient)
                .thenReturn(testPatient);
        when(passwordService.hash(anyString())).thenReturn("hashedCode");
        when(doctorAccessCodeRepository.save(any(DoctorAccessCode.class))).thenReturn(testAccessCode);

        String code = doctorAccessService.generateCode(1L, 60);

        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d+"));
        verify(patientRepository, times(2)).getReferenceById(1L);
    }

    @Test
    void shouldLoginWithCode() {
        when(passwordService.matches("123456", "hashedCode")).thenReturn(true);
        when(doctorAccessCodeRepository.findActiveCodes(any(LocalDateTime.class)))
                .thenReturn(java.util.Collections.singletonList(testAccessCode));
        when(doctorAccessSessionRepository.save(any(DoctorAccessSession.class)))
                .thenReturn(testSession);

        var result = doctorAccessService.loginWithCode("123456");

        assertNotNull(result);
        assertNotNull(result.token());
        assertEquals(1L, result.patientId());
        verify(doctorAccessCodeRepository).markAsUsed(1L);
    }

    @Test
    void shouldThrowExceptionWithInvalidCode() {
        when(passwordService.matches("000000", "hashedCode")).thenReturn(false);
        when(doctorAccessCodeRepository.findActiveCodes(any(LocalDateTime.class)))
                .thenReturn(java.util.Collections.singletonList(testAccessCode));

        assertThrows(IllegalArgumentException.class,
                () -> doctorAccessService.loginWithCode("000000"));
    }

    @Test
    void shouldValidateToken() {
        when(doctorAccessSessionRepository.findValidSession(eq("testToken123"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(testSession));

        boolean valid = doctorAccessService.validateToken("testToken123");

        assertTrue(valid);
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        when(doctorAccessSessionRepository.findValidSession(eq("invalidToken"), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        boolean valid = doctorAccessService.validateToken("invalidToken");

        assertFalse(valid);
    }

    @Test
    void shouldGetPatientFullData() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(patientService.getProfile(1L)).thenReturn(null);
        when(patientService.getSymptoms(1L)).thenReturn(java.util.Collections.emptyList());
        when(patientService.getMedicines(1L)).thenReturn(java.util.Collections.emptyList());

        DoctorFullDataResponse result = doctorAccessService.getPatientFullData(1L);

        assertNotNull(result);
        assertEquals(1L, result.getPatientId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenPatientNotFound() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> doctorAccessService.getPatientFullData(999L));
    }
}