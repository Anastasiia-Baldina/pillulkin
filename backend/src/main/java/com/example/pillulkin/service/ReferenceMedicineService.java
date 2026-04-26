package com.example.pillulkin.service;

import com.example.pillulkin.dto.ReferenceMedicineResponse;
import com.example.pillulkin.entity.ReferenceMedicine;
import com.example.pillulkin.repository.ReferenceMedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferenceMedicineService {

    private final ReferenceMedicineRepository referenceMedicineRepository;

    @Transactional(readOnly = true)
    public List<ReferenceMedicineResponse> getAllMedicines() {
        return referenceMedicineRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReferenceMedicineResponse> getMedicinesByCategory(String category) {
        return referenceMedicineRepository.findByCategory(category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReferenceMedicineResponse> searchMedicines(String keyword) {
        return referenceMedicineRepository.searchMedicines(keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReferenceMedicineResponse> getRecommendations(List<String> symptoms) {
        if (symptoms == null || symptoms.isEmpty()) {
            return List.of();
        }

        String searchTerm = String.join(" ", symptoms);
        List<ReferenceMedicine> medicines = referenceMedicineRepository.searchMedicines(searchTerm);

        return medicines.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public long count() {
        return referenceMedicineRepository.count();
    }

    private ReferenceMedicineResponse mapToResponse(ReferenceMedicine medicine) {
        return ReferenceMedicineResponse.builder()
                .id(medicine.getId())
                .name(medicine.getName())
                .dosage(medicine.getDosage())
                .form(medicine.getForm())
                .activeSubstance(medicine.getActiveSubstance())
                .indications(medicine.getIndications())
                .contraindications(medicine.getContraindications())
                .category(medicine.getCategory())
                .build();
    }
}