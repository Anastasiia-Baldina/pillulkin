package com.example.pillulkin.repository;

import com.example.pillulkin.entity.DoctorAccessCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorAccessCodeRepository extends JpaRepository<DoctorAccessCode, Long> {

    List<DoctorAccessCode> findByPatientId(Long patientId);

    @Query("SELECT c FROM DoctorAccessCode c WHERE c.status = 'ACTIVE' AND c.expiresAt > :now")
    List<DoctorAccessCode> findActiveCodes(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE DoctorAccessCode c SET c.status = 'USED' WHERE c.id = :id")
    void markAsUsed(@Param("id") Long id);
}