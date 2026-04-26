package com.example.pillulkin.service;

import com.example.pillulkin.dto.PrescriptionRequest;
import com.example.pillulkin.dto.PrescriptionResponse;
import com.example.pillulkin.entity.Patient;
import com.example.pillulkin.entity.Prescription;
import com.example.pillulkin.entity.ReferenceMedicine;
import com.example.pillulkin.repository.PatientRepository;
import com.example.pillulkin.repository.PrescriptionRepository;
import com.example.pillulkin.repository.ReferenceMedicineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private ReferenceMedicineRepository referenceMedicineRepository;
    @InjectMocks
    private PrescriptionService service;

    @Test
    void shouldCreatePrescriptionWithCustomInstructions() {
        Patient patient = Patient.builder().id(1L).build();
        ReferenceMedicine med = ReferenceMedicine.builder().id(10L).name("Парацетамол").dosage("500 мг").build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(referenceMedicineRepository.findById(10L)).thenReturn(Optional.of(med));
        when(prescriptionRepository.save(any())).thenAnswer(inv -> {
            Prescription p = inv.getArgument(0);
            p.setId(100L);
            return p;
        });

        PrescriptionRequest request = PrescriptionRequest.builder()
                .medicineId(10L)
                .customInstructions("2 раза в день после еды")
                .build();

        PrescriptionResponse response = service.createPrescription(1L, request);

        assertEquals(100L, response.getId());
        assertEquals("2 раза в день после еды", response.getDosageInstructions());
        assertEquals("ACTIVE", response.getStatus());
    }

    @Test
    void shouldCreatePrescriptionWithStructuredDosage() {
        Patient patient = Patient.builder().id(1L).build();
        ReferenceMedicine med = ReferenceMedicine.builder().id(10L).name("Ибупрофен").build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(referenceMedicineRepository.findById(10L)).thenReturn(Optional.of(med));
        when(prescriptionRepository.save(any())).thenAnswer(inv -> {
            Prescription p = inv.getArgument(0);
            p.setId(101L);
            return p;
        });

        PrescriptionRequest request = PrescriptionRequest.builder()
                .medicineId(10L)
                .frequency(3)
                .mealTiming("до еды")
                .timeOffset("за 30 минут")
                .build();

        PrescriptionResponse response = service.createPrescription(1L, request);

        assertTrue(response.getDosageInstructions().contains("3"));
        assertTrue(response.getDosageInstructions().contains("до еды"));
        assertTrue(response.getDosageInstructions().contains("за 30 минут"));
    }

    @Test
    void shouldThrowWhenPatientNotFound() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        PrescriptionRequest request = PrescriptionRequest.builder().medicineId(1L).build();
        assertThrows(IllegalArgumentException.class, () -> service.createPrescription(999L, request));
    }

    @Test
    void shouldMoveToCabinet() {
        Patient patient = Patient.builder().id(1L).build();
        ReferenceMedicine med = ReferenceMedicine.builder().id(10L).build();
        Prescription p = Prescription.builder().id(50L).patient(patient).referenceMedicine(med)
                .prescribedAt(java.time.LocalDateTime.now()).status("ACTIVE").build();

        when(prescriptionRepository.findById(50L)).thenReturn(Optional.of(p));
        when(prescriptionRepository.save(any())).thenReturn(p);

        service.moveToCabinet(1L, 50L);

        verify(prescriptionRepository).save(argThat(saved -> "MOVED".equals(saved.getStatus())));
    }

    @Test
    void shouldThrowWhenMovingOtherPatientPrescription() {
        Patient patient1 = Patient.builder().id(1L).build();
        Patient patient2 = Patient.builder().id(2L).build();
        ReferenceMedicine med = ReferenceMedicine.builder().id(10L).build();
        Prescription p = Prescription.builder().id(50L).patient(patient2).referenceMedicine(med)
                .prescribedAt(java.time.LocalDateTime.now()).status("ACTIVE").build();

        when(prescriptionRepository.findById(50L)).thenReturn(Optional.of(p));

        assertThrows(IllegalArgumentException.class, () -> service.moveToCabinet(1L, 50L));
    }

    @Test
    void shouldGetActivePrescriptions() {
        Patient patient = Patient.builder().id(1L).build();
        ReferenceMedicine med = ReferenceMedicine.builder().id(10L).name("Аспирин").dosage("100 мг").build();
        Prescription p = Prescription.builder().id(1L).patient(patient).referenceMedicine(med)
                .prescribedAt(java.time.LocalDateTime.now()).status("ACTIVE").build();

        when(prescriptionRepository.findByPatientIdAndStatusOrderByPrescribedAtDesc(1L, "ACTIVE"))
                .thenReturn(List.of(p));

        List<PrescriptionResponse> result = service.getActivePrescriptions(1L);
        assertEquals(1, result.size());
        assertEquals("Аспирин", result.get(0).getMedicineName());
    }
}
