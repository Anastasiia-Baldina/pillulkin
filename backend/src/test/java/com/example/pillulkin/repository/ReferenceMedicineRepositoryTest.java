package com.example.pillulkin.repository;

import com.example.pillulkin.entity.ReferenceMedicine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ReferenceMedicineRepositoryTest {

    @Autowired
    private ReferenceMedicineRepository referenceMedicineRepository;

    @Test
    void shouldSaveAndFindMedicine() {
        ReferenceMedicine medicine = ReferenceMedicine.builder()
                .name("Aspirin")
                .dosage("100mg")
                .form("tablet")
                .activeSubstance("acetylsalicylic acid")
                .indications("pain, fever")
                .category("Pain Relief")
                .build();

        ReferenceMedicine saved = referenceMedicineRepository.save(medicine);

        assertNotNull(saved.getId());
        assertEquals("Aspirin", saved.getName());
    }

    @Test
    void shouldFindByCategory() {
        ReferenceMedicine medicine = ReferenceMedicine.builder()
                .name("Test Medicine")
                .category("Test Category")
                .build();

        referenceMedicineRepository.save(medicine);

        List<ReferenceMedicine> found = referenceMedicineRepository.findByCategory("Test Category");

        assertFalse(found.isEmpty());
        assertTrue(found.stream().anyMatch(m -> m.getName().equals("Test Medicine")));
    }

    @Test
    void shouldSearchMedicines() {
        ReferenceMedicine medicine = ReferenceMedicine.builder()
                .name("Test Medicine")
                .indications("headache, fever")
                .build();

        referenceMedicineRepository.save(medicine);

        List<ReferenceMedicine> found = referenceMedicineRepository.searchMedicines("headache");

        assertFalse(found.isEmpty());
    }

    @Test
    void shouldCheckIfExistsByName() {
        ReferenceMedicine medicine = ReferenceMedicine.builder()
                .name("Unique Medicine Name")
                .build();

        referenceMedicineRepository.save(medicine);

        assertTrue(referenceMedicineRepository.existsByName("Unique Medicine Name"));
        assertFalse(referenceMedicineRepository.existsByName("Non-existent Name"));
    }
}