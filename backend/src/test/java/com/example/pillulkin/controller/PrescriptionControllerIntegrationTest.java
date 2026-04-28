package com.example.pillulkin.controller;

import com.example.pillulkin.dto.PrescriptionRequest;
import com.example.pillulkin.entity.Patient;
import com.example.pillulkin.entity.ReferenceMedicine;
import com.example.pillulkin.repository.PatientMedicineRepository;
import com.example.pillulkin.repository.PatientRepository;
import com.example.pillulkin.repository.PatientSymptomRepository;
import com.example.pillulkin.repository.PrescriptionRepository;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PrescriptionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ReferenceMedicineRepository referenceMedicineRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PatientMedicineRepository patientMedicineRepository;

    @Autowired
    private PatientSymptomRepository patientSymptomRepository;

    private Long testPatientId;
    private Long testMedicineId;

    @BeforeEach
    void setUp() {
        prescriptionRepository.deleteAll();
        patientMedicineRepository.deleteAll();
        patientSymptomRepository.deleteAll();
        referenceMedicineRepository.deleteAll();
        patientRepository.deleteAll();

        Patient patient = Patient.builder()
                .email("test-prescription@test.com")
                .passwordHash("$2a$10$hash")
                .createdAt(LocalDateTime.now())
                .build();
        testPatientId = patientRepository.save(patient).getId();

        ReferenceMedicine medicine = ReferenceMedicine.builder()
                .name("Test Aspirin")
                .dosage("500mg")
                .form("tablet")
                .activeSubstance("acetylsalicylic acid")
                .indications("headache, fever")
                .contraindications("ulcer")
                .category("painkiller")
                .build();
        testMedicineId = referenceMedicineRepository.save(medicine).getId();
    }

    @Test
    void shouldCreatePrescription() throws Exception {
        PrescriptionRequest request = PrescriptionRequest.builder()
                .medicineId(testMedicineId)
                .frequency(2)
                .mealTiming("after meal")
                .timeOffset("08:00, 20:00")
                .customInstructions("Take with water")
                .build();

        mockMvc.perform(post("/api/v1/patients/{patientId}/prescriptions", testPatientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicineName").value("Test Aspirin"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.patientId").value(testPatientId))
                .andExpect(jsonPath("$.frequency").value(2));
    }

    @Test
    void shouldRejectCreateForMissingPatient() throws Exception {
        PrescriptionRequest request = PrescriptionRequest.builder()
                .medicineId(testMedicineId)
                .build();

        mockMvc.perform(post("/api/v1/patients/{patientId}/prescriptions", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateForMissingMedicine() throws Exception {
        PrescriptionRequest request = PrescriptionRequest.builder()
                .medicineId(99999L)
                .build();

        mockMvc.perform(post("/api/v1/patients/{patientId}/prescriptions", testPatientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetActivePrescriptions() throws Exception {
        createTestPrescription();

        mockMvc.perform(get("/api/v1/patients/{patientId}/prescriptions", testPatientId)
                        .param("status", "active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldGetAllPrescriptions() throws Exception {
        createTestPrescription();

        mockMvc.perform(get("/api/v1/patients/{patientId}/prescriptions", testPatientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldReturnEmptyListWhenNoPrescriptions() throws Exception {
        mockMvc.perform(get("/api/v1/patients/{patientId}/prescriptions", testPatientId)
                        .param("status", "active"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void shouldMovePrescriptionToCabinet() throws Exception {
        Long prescriptionId = createTestPrescription();

        mockMvc.perform(put("/api/v1/patients/{patientId}/prescriptions/{prescriptionId}/move-to-cabinet",
                        testPatientId, prescriptionId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/patients/{patientId}/prescriptions", testPatientId)
                        .param("status", "active"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));

        mockMvc.perform(get("/api/v1/patients/{patientId}/prescriptions", testPatientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("MOVED"));
    }

    @Test
    void shouldRejectMoveForMissingPrescription() throws Exception {
        mockMvc.perform(put("/api/v1/patients/{patientId}/prescriptions/{prescriptionId}/move-to-cabinet",
                        testPatientId, 99999L))
                .andExpect(status().isBadRequest());
    }

    private Long createTestPrescription() throws Exception {
        PrescriptionRequest request = PrescriptionRequest.builder()
                .medicineId(testMedicineId)
                .frequency(2)
                .mealTiming("after meal")
                .build();

        String response = mockMvc.perform(post("/api/v1/patients/{patientId}/prescriptions", testPatientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }
}
