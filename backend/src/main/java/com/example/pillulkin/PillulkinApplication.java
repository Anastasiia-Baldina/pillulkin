package com.example.pillulkin;

import com.example.pillulkin.service.CsvImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class PillulkinApplication {

    public static void main(String[] args) {
        SpringApplication.run(PillulkinApplication.class, args);
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner importMedicines(CsvImportService csvImportService) {
        return args -> {
            log.info("Starting CSV import on application startup...");
            int count = csvImportService.importMedicines();
            if (count > 0) {
                log.info("CSV import completed. Imported {} medicines.", count);
            }
        };
    }
}