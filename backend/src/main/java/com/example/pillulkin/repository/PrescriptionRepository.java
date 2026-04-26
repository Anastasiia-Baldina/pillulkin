package com.example.pillulkin.repository;

import com.example.pillulkin.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByPatientIdAndStatusOrderByPrescribedAtDesc(Long patientId, String status);

    List<Prescription> findByPatientIdOrderByPrescribedAtDesc(Long patientId);
}
