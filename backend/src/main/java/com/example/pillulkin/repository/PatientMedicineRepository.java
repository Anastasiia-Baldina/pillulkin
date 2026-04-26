package com.example.pillulkin.repository;

import com.example.pillulkin.entity.PatientMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientMedicineRepository extends JpaRepository<PatientMedicine, Long> {

    List<PatientMedicine> findByPatientId(Long patientId);

    Optional<PatientMedicine> findByPatientIdAndReferenceMedicineId(Long patientId, Long referenceMedicineId);

    boolean existsByPatientIdAndReferenceMedicineId(Long patientId, Long referenceMedicineId);

    void deleteByPatientId(Long patientId);
}