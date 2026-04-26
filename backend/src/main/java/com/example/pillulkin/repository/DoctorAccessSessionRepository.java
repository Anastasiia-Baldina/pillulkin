package com.example.pillulkin.repository;

import com.example.pillulkin.entity.DoctorAccessSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DoctorAccessSessionRepository extends JpaRepository<DoctorAccessSession, Long> {

    @Query("SELECT s FROM DoctorAccessSession s WHERE s.token = :token AND s.expiresAt > :now")
    Optional<DoctorAccessSession> findValidSession(@Param("token") String token, @Param("now") LocalDateTime now);

    void deleteByDoctorAccessCodeId(Long doctorAccessCodeId);
}