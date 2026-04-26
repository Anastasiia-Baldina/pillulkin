package com.example.pillulkin.repository;

import com.example.pillulkin.entity.PatientSymptom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientSymptomRepository extends JpaRepository<PatientSymptom, Long> {

    List<PatientSymptom> findByPatientIdOrderByTimestampDesc(Long patientId);

    void deleteByPatientId(Long patientId);
}