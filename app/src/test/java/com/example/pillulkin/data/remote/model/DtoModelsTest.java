package com.example.pillulkin.data.remote.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class DtoModelsTest {

    @Test
    public void testAuthResponse() {
        AuthResponse response = new AuthResponse();
        response.setPatientId(1L);
        response.setEmail("test@test.com");
        response.setToken("abc123");

        assertEquals(Long.valueOf(1L), response.getPatientId());
        assertEquals("test@test.com", response.getEmail());
        assertEquals("abc123", response.getToken());
    }

    @Test
    public void testPatientProfileResponse() {
        PatientProfileResponse response = new PatientProfileResponse();
        response.setId(1L);
        response.setPatientId(10L);
        response.setName("Ivan");
        response.setAge(30);
        response.setAllergies("penicillin");
        response.setContraindications("none");
        response.setNotes("healthy");

        assertEquals(Long.valueOf(1L), response.getId());
        assertEquals(Long.valueOf(10L), response.getPatientId());
        assertEquals("Ivan", response.getName());
        assertEquals(Integer.valueOf(30), response.getAge());
        assertEquals("penicillin", response.getAllergies());
        assertEquals("none", response.getContraindications());
        assertEquals("healthy", response.getNotes());
    }

    @Test
    public void testPatientSymptomResponse() {
        PatientSymptomResponse response = new PatientSymptomResponse();
        response.setId(1L);
        response.setPatientId(10L);
        response.setSymptom("headache");
        response.setTimestamp("2024-01-15T10:30:00");

        assertEquals(Long.valueOf(1L), response.getId());
        assertEquals("headache", response.getSymptom());
        assertEquals("2024-01-15T10:30:00", response.getTimestamp());
    }

    @Test
    public void testPatientMedicineResponse() {
        PatientMedicineResponse response = new PatientMedicineResponse();
        response.setId(1L);
        response.setPatientId(10L);
        response.setMedicineId(5L);
        response.setMedicineName("Aspirin");
        response.setDosage("500mg");
        response.setForm("tablet");
        response.setAddedAt("2024-01-15T10:30:00");

        assertEquals(Long.valueOf(5L), response.getMedicineId());
        assertEquals("Aspirin", response.getMedicineName());
        assertEquals("500mg", response.getDosage());
        assertEquals("tablet", response.getForm());
    }

    @Test
    public void testReferenceMedicineResponse() {
        ReferenceMedicineResponse response = new ReferenceMedicineResponse();
        response.setId(1L);
        response.setName("Aspirin");
        response.setDosage("500mg");
        response.setForm("tablet");
        response.setActiveSubstance("acetylsalicylic acid");
        response.setIndications("headache, fever");
        response.setContraindications("ulcer");
        response.setCategory("painkiller");

        assertEquals("Aspirin", response.getName());
        assertEquals("acetylsalicylic acid", response.getActiveSubstance());
        assertEquals("painkiller", response.getCategory());
    }

    @Test
    public void testDoctorCodeResponse() {
        DoctorCodeResponse response = new DoctorCodeResponse();
        response.setCode("123456");
        response.setExpiresAt("2024-01-15T11:30:00");

        assertEquals("123456", response.getCode());
        assertEquals("2024-01-15T11:30:00", response.getExpiresAt());
    }

    @Test
    public void testDoctorFullDataResponse() {
        DoctorFullDataResponse response = new DoctorFullDataResponse();
        response.setPatientId(1L);
        response.setEmail("test@test.com");

        assertNull(response.getProfile());
        assertNull(response.getSymptoms());
        assertNull(response.getMedicines());

        PatientProfileResponse profile = new PatientProfileResponse();
        response.setProfile(profile);
        assertEquals(profile, response.getProfile());
    }

    @Test
    public void testPatientProfileRequest() {
        PatientProfileRequest request = new PatientProfileRequest("Ivan", 30, "none", "none", "ok");
        assertEquals("Ivan", request.getName());
        assertEquals(Integer.valueOf(30), request.getAge());

        PatientProfileRequest empty = new PatientProfileRequest();
        empty.setName("Petr");
        assertEquals("Petr", empty.getName());
    }

    @Test
    public void testPatientSymptomRequest() {
        PatientSymptomRequest request = new PatientSymptomRequest("headache");
        assertNotNull(request);
    }

    @Test
    public void testPatientMedicineRequest() {
        PatientMedicineRequest request = new PatientMedicineRequest(5L);
        assertNotNull(request);
    }

    @Test
    public void testDoctorLoginRequest() {
        DoctorLoginRequest request = new DoctorLoginRequest("123456");
        assertNotNull(request);
    }

    @Test
    public void testPatientLoginRequest() {
        PatientLoginRequest request = new PatientLoginRequest("test@test.com", "pass");
        assertNotNull(request);
    }

    @Test
    public void testPatientRegisterRequest() {
        PatientRegisterRequest request = new PatientRegisterRequest("test@test.com", "pass");
        assertNotNull(request);
    }

    @Test
    public void testGenerateCodeRequest() {
        GenerateCodeRequest request = new GenerateCodeRequest(1L, 60);
        assertNotNull(request);
    }
}
