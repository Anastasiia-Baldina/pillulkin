package com.example.pillulkin.controller;

import com.example.pillulkin.entity.Patient;
import com.example.pillulkin.repository.DoctorAccessCodeRepository;
import com.example.pillulkin.repository.DoctorAccessSessionRepository;
import com.example.pillulkin.repository.PatientMedicineRepository;
import com.example.pillulkin.repository.PatientRepository;
import com.example.pillulkin.repository.PatientSymptomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DoctorControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorAccessCodeRepository doctorAccessCodeRepository;
    @Autowired private DoctorAccessSessionRepository doctorAccessSessionRepository;
    @Autowired private PatientMedicineRepository patientMedicineRepository;
    @Autowired private PatientSymptomRepository patientSymptomRepository;
    @Autowired private ObjectMapper objectMapper;

    private Long testPatientId;

    @BeforeEach
    void setUp() {
        doctorAccessSessionRepository.deleteAll();
        doctorAccessCodeRepository.deleteAll();
        patientMedicineRepository.deleteAll();
        patientSymptomRepository.deleteAll();
        patientRepository.deleteAll();

        Patient patient = Patient.builder()
                .email("doctor-test@test.com")
                .passwordHash("hash")
                .createdAt(LocalDateTime.now())
                .build();
        testPatientId = patientRepository.save(patient).getId();
    }

    @Test
    void shouldRejectFullDataWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/doctor/patients/{patientId}/full-data", testPatientId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldRejectFullDataWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/doctor/patients/{patientId}/full-data", testPatientId)
                        .header("X-Doctor-Token", "invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldValidateInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/doctor/validate")
                        .header("X-Doctor-Token", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }
}
