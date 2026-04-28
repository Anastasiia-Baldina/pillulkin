package com.example.pillulkin.controller;

import com.example.pillulkin.dto.*;
import com.example.pillulkin.entity.Patient;
import com.example.pillulkin.repository.DoctorAccessCodeRepository;
import com.example.pillulkin.repository.DoctorAccessSessionRepository;
import com.example.pillulkin.repository.PatientMedicineRepository;
import com.example.pillulkin.repository.PatientRepository;
import com.example.pillulkin.repository.PatientSymptomRepository;
import com.example.pillulkin.service.GoogleAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorAccessCodeRepository doctorAccessCodeRepository;
    @Autowired private DoctorAccessSessionRepository doctorAccessSessionRepository;
    @Autowired private PatientMedicineRepository patientMedicineRepository;
    @Autowired private PatientSymptomRepository patientSymptomRepository;

    @MockBean private GoogleAuthService googleAuthService;

    private Long testPatientId;

    @BeforeEach
    void setUp() {
        doctorAccessSessionRepository.deleteAll();
        doctorAccessCodeRepository.deleteAll();
        patientMedicineRepository.deleteAll();
        patientSymptomRepository.deleteAll();
        patientRepository.deleteAll();

        Patient patient = Patient.builder()
                .email("auth-test@test.com")
                .passwordHash("hash")
                .createdAt(LocalDateTime.now())
                .build();
        testPatientId = patientRepository.save(patient).getId();
    }

    @Test
    void shouldGoogleLoginExistingPatient() throws Exception {
        when(googleAuthService.verifyToken(anyString()))
                .thenReturn(new GoogleAuthService.GoogleUserInfo("auth-test@test.com", "Test User"));
        when(googleAuthService.findOrCreatePatient("auth-test@test.com", "Test User"))
                .thenAnswer(invocation -> patientRepository.findByEmail("auth-test@test.com").orElseThrow());

        GoogleAuthRequest request = new GoogleAuthRequest();
        request.setIdToken("valid-token");

        mockMvc.perform(post("/api/v1/auth/patient/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(testPatientId))
                .andExpect(jsonPath("$.email").value("auth-test@test.com"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldGoogleLoginNewPatient() throws Exception {
        when(googleAuthService.verifyToken(anyString()))
                .thenReturn(new GoogleAuthService.GoogleUserInfo("new@test.com", "New User"));
        when(googleAuthService.findOrCreatePatient("new@test.com", "New User"))
                .thenAnswer(invocation -> {
                    Patient p = Patient.builder()
                            .email("new@test.com")
                            .passwordHash("")
                            .createdAt(LocalDateTime.now())
                            .build();
                    return patientRepository.save(p);
                });

        GoogleAuthRequest request = new GoogleAuthRequest();
        request.setIdToken("valid-token");

        mockMvc.perform(post("/api/v1/auth/patient/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").isNumber())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldGenerateDoctorCode() throws Exception {
        DoctorGenerateCodeRequest request = new DoctorGenerateCodeRequest();
        request.setPatientId(testPatientId);
        request.setExpiresInMinutes(60);

        mockMvc.perform(post("/api/v1/auth/doctor/generate-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }

    @Test
    void shouldLoginDoctorWithCode() throws Exception {
        DoctorGenerateCodeRequest genRequest = new DoctorGenerateCodeRequest();
        genRequest.setPatientId(testPatientId);
        genRequest.setExpiresInMinutes(60);

        String genResponse = mockMvc.perform(post("/api/v1/auth/doctor/generate-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(genRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String code = objectMapper.readTree(genResponse).get("code").asText();

        DoctorLoginRequest loginRequest = new DoctorLoginRequest();
        loginRequest.setCode(code);

        mockMvc.perform(post("/api/v1/auth/doctor/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(testPatientId))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldRejectDoctorLoginWithInvalidCode() throws Exception {
        DoctorLoginRequest request = new DoctorLoginRequest();
        request.setCode("000000");

        mockMvc.perform(post("/api/v1/auth/doctor/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
