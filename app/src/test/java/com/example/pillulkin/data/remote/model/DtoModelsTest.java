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
        PatientMedicineRequest request = new PatientMedicineRequest(5L, "2026-12-01", "10");
        assertNotNull(request);
    }

    @Test
    public void testDoctorLoginRequest() {
        DoctorLoginRequest request = new DoctorLoginRequest("123456");
        assertNotNull(request);
    }

    @Test
    public void testGenerateCodeRequest() {
        GenerateCodeRequest request = new GenerateCodeRequest(1L, 60);
        assertNotNull(request);
    }

    @Test
    public void testDiagnosisRequest() {
        DiagnosisRequest request = new DiagnosisRequest(
                java.util.Arrays.asList("headache", "fever"), "initial", null);
        assertEquals(2, request.getSymptoms().size());
        assertEquals("initial", request.getStep());
        assertNull(request.getAnswers());
    }

    @Test
    public void testDiagnosisResponse() throws Exception {
        DiagnosisResponse response = new DiagnosisResponse();
        java.lang.reflect.Field diagnosisField = DiagnosisResponse.class.getDeclaredField("diagnosis");
        diagnosisField.setAccessible(true);
        diagnosisField.set(response, "Flu");

        java.lang.reflect.Field confidenceField = DiagnosisResponse.class.getDeclaredField("confidence");
        confidenceField.setAccessible(true);
        confidenceField.set(response, 0.85);

        java.lang.reflect.Field questionsField = DiagnosisResponse.class.getDeclaredField("suggestedQuestions");
        questionsField.setAccessible(true);
        questionsField.set(response, java.util.Arrays.asList("Q1?", "Q2?"));

        java.lang.reflect.Field isFinalField = DiagnosisResponse.class.getDeclaredField("isFinal");
        isFinalField.setAccessible(true);
        isFinalField.set(response, true);

        assertEquals("Flu", response.getDiagnosis());
        assertEquals(0.85, response.getConfidence(), 0.001);
        assertEquals(2, response.getSuggestedQuestions().size());
        assertTrue(response.isFinal());
    }

    @Test
    public void testDiagnosisResponse_defaults() {
        DiagnosisResponse response = new DiagnosisResponse();
        assertNull(response.getDiagnosis());
        assertEquals(0.0, response.getConfidence(), 0.001);
        assertNull(response.getSuggestedQuestions());
        assertFalse(response.isFinal());
    }

    @Test
    public void testPrescriptionResponse() throws Exception {
        PrescriptionResponse response = new PrescriptionResponse();
        response.setId(1L);

        java.lang.reflect.Field f;
        f = PrescriptionResponse.class.getDeclaredField("patientId"); f.setAccessible(true); f.set(response, 10L);
        f = PrescriptionResponse.class.getDeclaredField("medicineId"); f.setAccessible(true); f.set(response, 5L);
        f = PrescriptionResponse.class.getDeclaredField("medicineName"); f.setAccessible(true); f.set(response, "Aspirin");
        f = PrescriptionResponse.class.getDeclaredField("dosage"); f.setAccessible(true); f.set(response, "500mg");
        f = PrescriptionResponse.class.getDeclaredField("form"); f.setAccessible(true); f.set(response, "tablet");
        f = PrescriptionResponse.class.getDeclaredField("activeSubstance"); f.setAccessible(true); f.set(response, "ASA");
        f = PrescriptionResponse.class.getDeclaredField("dosageInstructions"); f.setAccessible(true); f.set(response, "after meal");
        f = PrescriptionResponse.class.getDeclaredField("frequency"); f.setAccessible(true); f.set(response, 2);
        f = PrescriptionResponse.class.getDeclaredField("mealTiming"); f.setAccessible(true); f.set(response, "after");
        f = PrescriptionResponse.class.getDeclaredField("timeOffset"); f.setAccessible(true); f.set(response, "08:00");
        f = PrescriptionResponse.class.getDeclaredField("customInstructions"); f.setAccessible(true); f.set(response, "with water");
        f = PrescriptionResponse.class.getDeclaredField("prescribedAt"); f.setAccessible(true); f.set(response, "2024-01-15T10:00:00");
        f = PrescriptionResponse.class.getDeclaredField("status"); f.setAccessible(true); f.set(response, "ACTIVE");

        assertEquals(Long.valueOf(1L), response.getId());
        assertEquals(Long.valueOf(10L), response.getPatientId());
        assertEquals(Long.valueOf(5L), response.getMedicineId());
        assertEquals("Aspirin", response.getMedicineName());
        assertEquals("500mg", response.getDosage());
        assertEquals("tablet", response.getForm());
        assertEquals("ASA", response.getActiveSubstance());
        assertEquals("after meal", response.getDosageInstructions());
        assertEquals(Integer.valueOf(2), response.getFrequency());
        assertEquals("after", response.getMealTiming());
        assertEquals("08:00", response.getTimeOffset());
        assertEquals("with water", response.getCustomInstructions());
        assertEquals("2024-01-15T10:00:00", response.getPrescribedAt());
        assertEquals("ACTIVE", response.getStatus());
    }

    @Test
    public void testPrescriptionRequest() {
        PrescriptionRequest request = new PrescriptionRequest(5L, "after meal", 2, "after", "08:00", "with water");
        assertEquals(Long.valueOf(5L), request.getMedicineId());
        assertEquals("after meal", request.getDosageInstructions());
        assertEquals(Integer.valueOf(2), request.getFrequency());
        assertEquals("after", request.getMealTiming());
        assertEquals("08:00", request.getTimeOffset());
        assertEquals("with water", request.getCustomInstructions());
    }

    @Test
    public void testRecommendationItem_header() {
        RecommendationItem item = RecommendationItem.header("Test Header");
        assertEquals(RecommendationItem.TYPE_HEADER, item.getType());
        assertEquals("Test Header", item.getHeaderText());
        assertNull(item.getMedicine());
        assertFalse(item.isFromCabinet());
    }

    @Test
    public void testRecommendationItem_medicine() {
        ReferenceMedicineResponse med = new ReferenceMedicineResponse();
        med.setId(1L);
        med.setName("Test");
        RecommendationItem item = RecommendationItem.medicine(med, true);
        assertEquals(RecommendationItem.TYPE_MEDICINE, item.getType());
        assertNull(item.getHeaderText());
        assertEquals(med, item.getMedicine());
        assertTrue(item.isFromCabinet());
    }

    @Test
    public void testRecommendationItem_notFromCabinet() {
        ReferenceMedicineResponse med = new ReferenceMedicineResponse();
        RecommendationItem item = RecommendationItem.medicine(med, false);
        assertFalse(item.isFromCabinet());
    }

    @Test
    public void testGoogleAuthRequest() {
        GoogleAuthRequest request = new GoogleAuthRequest("token123");
        assertNotNull(request);
    }

    @Test
    public void testPatientProfileRequest_full() {
        PatientProfileRequest request = new PatientProfileRequest("Ana", 25, "none", "none", "ok");
        assertEquals("Ana", request.getName());
        assertEquals(Integer.valueOf(25), request.getAge());
        assertEquals("none", request.getAllergies());
        assertEquals("none", request.getContraindications());
        assertEquals("ok", request.getNotes());
    }

    @Test
    public void testPatientProfileRequest_setters() {
        PatientProfileRequest request = new PatientProfileRequest();
        request.setName("Test");
        request.setAge(40);
        request.setAllergies("aspirin");
        request.setContraindications("pregnancy");
        request.setNotes("notes");

        assertEquals("Test", request.getName());
        assertEquals(Integer.valueOf(40), request.getAge());
        assertEquals("aspirin", request.getAllergies());
        assertEquals("pregnancy", request.getContraindications());
        assertEquals("notes", request.getNotes());
    }

    @Test
    public void testPatientMedicineResponse_expirationAndQuantity() {
        PatientMedicineResponse response = new PatientMedicineResponse();
        response.setExpirationDate("2026-12-01");
        response.setQuantity("30");
        assertEquals("2026-12-01", response.getExpirationDate());
        assertEquals("30", response.getQuantity());
    }

    @Test
    public void testDoctorFullDataResponse_full() {
        DoctorFullDataResponse response = new DoctorFullDataResponse();
        response.setPatientId(1L);
        response.setEmail("doc@test.com");

        java.util.List<PatientSymptomResponse> symptoms = new java.util.ArrayList<>();
        symptoms.add(new PatientSymptomResponse());
        response.setSymptoms(symptoms);

        java.util.List<PatientMedicineResponse> meds = new java.util.ArrayList<>();
        meds.add(new PatientMedicineResponse());
        response.setMedicines(meds);

        assertEquals(Long.valueOf(1L), response.getPatientId());
        assertEquals("doc@test.com", response.getEmail());
        assertEquals(1, response.getSymptoms().size());
        assertEquals(1, response.getMedicines().size());
    }
}
