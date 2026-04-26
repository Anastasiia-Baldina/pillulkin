package com.example.pillulkin.controller;

import com.example.pillulkin.dto.ReferenceMedicineResponse;
import com.example.pillulkin.service.ReferenceMedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final ReferenceMedicineService referenceMedicineService;

    @GetMapping
    public ResponseEntity<List<ReferenceMedicineResponse>> getAllMedicines(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(referenceMedicineService.getMedicinesByCategory(category));
        }
        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(referenceMedicineService.searchMedicines(search));
        }
        return ResponseEntity.ok(referenceMedicineService.getAllMedicines());
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<ReferenceMedicineResponse>> getRecommendations(
            @RequestParam List<String> symptoms) {
        return ResponseEntity.ok(referenceMedicineService.getRecommendations(symptoms));
    }
}