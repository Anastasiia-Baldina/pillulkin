package com.example.pillulkin.repository;

import com.example.pillulkin.entity.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    @Test
    void shouldSaveAndFindPatient() {
        Patient patient = Patient.builder()
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .createdAt(LocalDateTime.now())
                .build();

        Patient saved = patientRepository.save(patient);

        assertNotNull(saved.getId());
        assertEquals("test@example.com", saved.getEmail());
    }

    @Test
    void shouldFindByEmail() {
        Patient patient = Patient.builder()
                .email("findme@example.com")
                .passwordHash("hashedPassword")
                .createdAt(LocalDateTime.now())
                .build();

        patientRepository.save(patient);

        Optional<Patient> found = patientRepository.findByEmail("findme@example.com");

        assertTrue(found.isPresent());
        assertEquals("findme@example.com", found.get().getEmail());
    }

    @Test
    void shouldReturnEmptyForNonExistentEmail() {
        Optional<Patient> found = patientRepository.findByEmail("nonexistent@example.com");

        assertTrue(found.isEmpty());
    }

    @Test
    void shouldCheckEmailExists() {
        Patient patient = Patient.builder()
                .email("exists@example.com")
                .passwordHash("hashedPassword")
                .createdAt(LocalDateTime.now())
                .build();

        patientRepository.save(patient);

        assertTrue(patientRepository.existsByEmail("exists@example.com"));
        assertFalse(patientRepository.existsByEmail("notexists@example.com"));
    }
}