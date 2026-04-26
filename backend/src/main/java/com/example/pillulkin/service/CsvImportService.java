package com.example.pillulkin.service;

import com.example.pillulkin.entity.ReferenceMedicine;
import com.example.pillulkin.repository.ReferenceMedicineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvImportService {

    private final ReferenceMedicineRepository referenceMedicineRepository;

    @Transactional
    public int importMedicines() {
        if (referenceMedicineRepository.count() > 0) {
            log.info("Medicines already imported, skipping CSV import");
            return 0;
        }

        log.info("Starting CSV import...");
        List<ReferenceMedicine> medicines = new ArrayList<>();

        try {
            ClassPathResource resource = new ClassPathResource("data/Pillulkin_medicines.csv");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            CSVParser csvParser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            for (CSVRecord record : csvParser) {
                ReferenceMedicine medicine = ReferenceMedicine.builder()
                        .name(getValue(record, "name"))
                        .dosage(getValue(record, "dosage"))
                        .form(getValue(record, "form"))
                        .activeSubstance(getValue(record, "active_substance"))
                        .indications(getValue(record, "indications"))
                        .contraindications(getValue(record, "contraindications"))
                        .category(getValue(record, "category"))
                        .build();
                medicines.add(medicine);
            }

            referenceMedicineRepository.saveAll(medicines);
            log.info("Successfully imported {} medicines from CSV", medicines.size());

        } catch (Exception e) {
            log.error("Error importing CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to import CSV", e);
        }

        return medicines.size();
    }

    private String getValue(CSVRecord record, String header) {
        try {
            return record.isMapped(header) ? record.get(header) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}