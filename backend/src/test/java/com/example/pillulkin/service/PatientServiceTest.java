package com.example.pillulkin.service;

import com.example.pillulkin.dto.*;
import com.example.pillulkin.entity.*;
import com.example.pillulkin.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private PatientSymptomRepository patientSymptomRepository;

    @Mock
    private PatientMedicineRepository patientMedicineRepository;

    @Mock
    private ReferenceMedicineRepository referenceMedicineRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient testPatient;
    private PatientProfile testProfile;
    private ReferenceMedicine testMedicine;

    @BeforeEach
    void setUp() {
        testPatient = Patient.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("hash")
                .createdAt(LocalDateTime.now())
                .build();

        testProfile = PatientProfile.builder()
                .id(1L)
                .patient(testPatient)
                .name("John")
                .age(30)
                .allergies("peanuts")
                .contraindications("none")
                .notes("test notes")
                .build();

        testMedicine = ReferenceMedicine.builder()
                .id(1L)
                .name("Aspirin")
                .dosage("100mg")
                .form("tablet")
                .build();
    }

    @Test
    void shouldGetProfile() {
        when(patientProfileRepository.findByPatientId(1L)).thenReturn(Optional.of(testProfile));

        PatientProfileResponse result = patientService.getProfile(1L);

        assertNotNull(result);
        assertEquals("John", result.getName());
        assertEquals(30, result.getAge());
    }

    @Test
    void shouldUpdateProfile() {
        PatientProfileRequest request = new PatientProfileRequest();
        request.setName("Jane");
        request.setAge(25);

        when(patientRepository.getReferenceById(1L)).thenReturn(testPatient);
        when(patientProfileRepository.findByPatientId(1L)).thenReturn(Optional.of(testProfile));
        when(patientProfileRepository.save(any(PatientProfile.class))).thenReturn(testProfile);

        PatientProfileResponse result = patientService.updateProfile(1L, request);

        assertNotNull(result);
    }

    @Test
    void shouldGetSymptoms() {
        PatientSymptom symptom = PatientSymptom.builder()
                .id(1L)
                .patient(testPatient)
                .symptom("headache")
                .timestamp(LocalDateTime.now())
                .build();

        when(patientSymptomRepository.findByPatientIdOrderByTimestampDesc(1L))
                .thenReturn(Arrays.asList(symptom));

        var result = patientService.getSymptoms(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("headache", result.get(0).getSymptom());
    }

    @Test
    void shouldAddSymptom() {
        PatientSymptomRequest request = new PatientSymptomRequest();
        request.setSymptom("fever");

        when(patientRepository.getReferenceById(1L)).thenReturn(testPatient);
        when(patientSymptomRepository.save(any(PatientSymptom.class)))
                .thenAnswer(invocation -> {
                    PatientSymptom s = invocation.getArgument(0);
                    s.setId(1L);
                    return s;
                });

        PatientSymptomResponse result = patientService.addSymptom(1L, "fever");

        assertNotNull(result);
        assertEquals("fever", result.getSymptom());
    }

    @Test
    void shouldGetMedicines() {
        PatientMedicine patientMedicine = PatientMedicine.builder()
                .id(1L)
                .patient(testPatient)
                .referenceMedicine(testMedicine)
                .addedAt(LocalDateTime.now())
                .build();

        when(patientMedicineRepository.findByPatientId(1L))
                .thenReturn(Arrays.asList(patientMedicine));

        var result = patientService.getMedicines(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Aspirin", result.get(0).getMedicineName());
    }

    @Test
    void shouldAddMedicine() {
        PatientMedicineRequest request = new PatientMedicineRequest();
        request.setMedicineId(1L);

        when(patientMedicineRepository.existsByPatientIdAndReferenceMedicineId(1L, 1L))
                .thenReturn(false);
        when(patientRepository.getReferenceById(1L)).thenReturn(testPatient);
        when(referenceMedicineRepository.getReferenceById(1L)).thenReturn(testMedicine);
        when(patientMedicineRepository.save(any(PatientMedicine.class)))
                .thenAnswer(invocation -> {
                    PatientMedicine pm = invocation.getArgument(0);
                    pm.setId(1L);
                    return pm;
                });

        PatientMedicineResponse result = patientService.addMedicine(1L, createMedicineRequest(1L));

        assertNotNull(result);
    }

    @Test
    void shouldThrowExceptionWhenMedicineAlreadyExists() {
        when(patientMedicineRepository.existsByPatientIdAndReferenceMedicineId(1L, 1L))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> patientService.addMedicine(1L, createMedicineRequest(1L)));
    }

    @Test
    void shouldRemoveMedicine() {
        PatientMedicine patientMedicine = PatientMedicine.builder()
                .id(1L)
                .patient(testPatient)
                .referenceMedicine(testMedicine)
                .addedAt(LocalDateTime.now())
                .build();

        when(patientMedicineRepository.findById(1L))
                .thenReturn(Optional.of(patientMedicine));

        patientService.removeMedicine(1L, 1L);

        verify(patientMedicineRepository).delete(patientMedicine);
    }

    @Test
    void shouldRemoveSymptom() {
        PatientSymptom symptom = PatientSymptom.builder()
                .id(5L)
                .patient(testPatient)
                .symptom("headache")
                .timestamp(LocalDateTime.now())
                .build();

        when(patientSymptomRepository.findById(5L)).thenReturn(Optional.of(symptom));

        patientService.removeSymptom(1L, 5L);

        verify(patientSymptomRepository).delete(symptom);
    }

    @Test
    void shouldThrowWhenRemovingSymptomOfOtherPatient() {
        Patient otherPatient = Patient.builder().id(2L).build();
        PatientSymptom symptom = PatientSymptom.builder()
                .id(5L)
                .patient(otherPatient)
                .symptom("headache")
                .timestamp(LocalDateTime.now())
                .build();

        when(patientSymptomRepository.findById(5L)).thenReturn(Optional.of(symptom));

        assertThrows(IllegalArgumentException.class,
                () -> patientService.removeSymptom(1L, 5L));
    }

    @Test
    void shouldThrowWhenRemovingNonexistentSymptom() {
        when(patientSymptomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> patientService.removeSymptom(1L, 999L));
    }

    @Test
    void shouldGetEmptyProfile() {
        when(patientProfileRepository.findByPatientId(1L)).thenReturn(Optional.empty());
        when(patientRepository.getReferenceById(1L)).thenReturn(testPatient);

        PatientProfileResponse result = patientService.getProfile(1L);

        assertNotNull(result);
        assertNull(result.getName());
    }

    @Test
    void shouldRenewSymptom() {
        PatientSymptom symptom = PatientSymptom.builder()
                .id(5L)
                .patient(testPatient)
                .symptom("headache")
                .timestamp(LocalDateTime.now().minusDays(10))
                .build();

        when(patientSymptomRepository.findById(5L)).thenReturn(Optional.of(symptom));
        when(patientSymptomRepository.save(any(PatientSymptom.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PatientSymptomResponse result = patientService.renewSymptom(1L, 5L);

        assertNotNull(result);
        assertEquals("headache", result.getSymptom());
        verify(patientSymptomRepository).save(symptom);
    }

    @Test
    void shouldThrowWhenRenewingSymptomOfOtherPatient() {
        Patient otherPatient = Patient.builder().id(2L).build();
        PatientSymptom symptom = PatientSymptom.builder()
                .id(5L)
                .patient(otherPatient)
                .symptom("headache")
                .timestamp(LocalDateTime.now())
                .build();

        when(patientSymptomRepository.findById(5L)).thenReturn(Optional.of(symptom));

        assertThrows(IllegalArgumentException.class,
                () -> patientService.renewSymptom(1L, 5L));
    }

    @Test
    void shouldThrowWhenRenewingNonexistentSymptom() {
        when(patientSymptomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> patientService.renewSymptom(1L, 999L));
    }

    private PatientMedicineRequest createMedicineRequest(long medicineId) {
        PatientMedicineRequest req = new PatientMedicineRequest();
        req.setMedicineId(medicineId);
        return req;
    }
}