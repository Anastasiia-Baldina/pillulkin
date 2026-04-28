package com.example.pillulkin.controller;

import com.example.pillulkin.dto.PatientMedicineRequest;
import com.example.pillulkin.dto.PatientProfileRequest;
import com.example.pillulkin.dto.PatientSymptomRequest;
import com.example.pillulkin.entity.Patient;
import com.example.pillulkin.entity.ReferenceMedicine;
import com.example.pillulkin.repository.PatientMedicineRepository;
import com.example.pillulkin.repository.PatientRepository;
import com.example.pillulkin.repository.PatientSymptomRepository;
import com.example.pillulkin.repository.ReferenceMedicineRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PatientControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PatientRepository patientRepository;
    @Autowired private ReferenceMedicineRepository referenceMedicineRepository;
    @Autowired private PatientMedicineRepository patientMedicineRepository;
    @Autowired private PatientSymptomRepository patientSymptomRepository;

    private Long testPatientId;
    private Long testMedicineId;

    @BeforeEach
    void setUp() {
        patientMedicineRepository.deleteAll();
        patientSymptomRepository.deleteAll();
        referenceMedicineRepository.deleteAll();
        patientRepository.deleteAll();

        Patient patient = Patient.builder()
                .email("patient-test@test.com")
                .passwordHash("hash")
                .createdAt(LocalDateTime.now())
                .build();
        testPatientId = patientRepository.save(patient).getId();

        ReferenceMedicine medicine = ReferenceMedicine.builder()
                .name("Test Aspirin")
                .dosage("500mg")
                .form("tablet")
                .activeSubstance("ASA")
                .indications("headache")
                .contraindications("ulcer")
                .category("painkiller")
                .build();
        testMedicineId = referenceMedicineRepository.save(medicine).getId();
    }

    @Test
    void shouldGetProfile() throws Exception {
        mockMvc.perform(get("/api/v1/patients/{patientId}/profile", testPatientId))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateProfile() throws Exception {
        PatientProfileRequest request = new PatientProfileRequest();
        request.setName("Ivan");
        request.setAge(30);
        request.setAllergies("penicillin");
        request.setContraindications("none");
        request.setNotes("healthy");

        mockMvc.perform(put("/api/v1/patients/{patientId}/profile", testPatientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.allergies").value("penicillin"));
    }

    @Test
    void shouldGetEmptySymptoms() throws Exception {
        mockMvc.perform(get("/api/v1/patients/{patientId}/symptoms", testPatientId))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void shouldAddSymptom() throws Exception {
        PatientSymptomRequest request = new PatientSymptomRequest();
        request.setSymptom("headache");

        mockMvc.perform(post("/api/v1/patients/{patientId}/symptoms", testPatientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symptom").value("headache"));
    }

    @Test
    void shouldRejectEmptySymptom() throws Exception {
        PatientSymptomRequest request = new PatientSymptomRequest();
        request.setSymptom("");

        mockMvc.perform(post("/api/v1/patients/{patientId}/symptoms", testPatientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAddAndRemoveSymptom() throws Exception {
        PatientSymptomRequest request = new PatientSymptomRequest();
        request.setSymptom("fever");

        String response = mockMvc.perform(post("/api/v1/patients/{patientId}/symptoms", testPatientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long symptomId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/v1/patients/{patientId}/symptoms/{symptomId}", testPatientId, symptomId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRenewSymptom() throws Exception {
        PatientSymptomRequest request = new PatientSymptomRequest();
        request.setSymptom("cough");

        String response = mockMvc.perform(post("/api/v1/patients/{patientId}/symptoms", testPatientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long symptomId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(put("/api/v1/patients/{patientId}/symptoms/{symptomId}/renew", testPatientId, symptomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(symptomId));
    }

    @Test
    void shouldGetEmptyMedicines() throws Exception {
        mockMvc.perform(get("/api/v1/patients/{patientId}/medicines", testPatientId))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void shouldAddMedicine() throws Exception {
        PatientMedicineRequest request = new PatientMedicineRequest();
        request.setMedicineId(testMedicineId);
        request.setExpirationDate(LocalDate.of(2026, 12, 1));
        request.setQuantity("30");

        mockMvc.perform(post("/api/v1/patients/{patientId}/medicines", testPatientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicineName").value("Test Aspirin"))
                .andExpect(jsonPath("$.quantity").value("30"));
    }

    @Test
    void shouldAddAndRemoveMedicine() throws Exception {
        PatientMedicineRequest request = new PatientMedicineRequest();
        request.setMedicineId(testMedicineId);
        request.setExpirationDate(LocalDate.of(2026, 12, 1));
        request.setQuantity("10");

        String response = mockMvc.perform(post("/api/v1/patients/{patientId}/medicines", testPatientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long medicineId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/v1/patients/{patientId}/medicines/{medicineId}", testPatientId, medicineId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldUpdateMedicine() throws Exception {
        PatientMedicineRequest addRequest = new PatientMedicineRequest();
        addRequest.setMedicineId(testMedicineId);
        addRequest.setExpirationDate(LocalDate.of(2026, 12, 1));
        addRequest.setQuantity("10");

        String addResponse = mockMvc.perform(post("/api/v1/patients/{patientId}/medicines", testPatientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long patientMedicineId = objectMapper.readTree(addResponse).get("id").asLong();

        PatientMedicineRequest updateRequest = new PatientMedicineRequest();
        updateRequest.setMedicineId(testMedicineId);
        updateRequest.setExpirationDate(LocalDate.of(2027, 6, 1));
        updateRequest.setQuantity("20");

        mockMvc.perform(put("/api/v1/patients/{patientId}/medicines/{patientMedicineId}", testPatientId, patientMedicineId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value("20"));
    }
}
