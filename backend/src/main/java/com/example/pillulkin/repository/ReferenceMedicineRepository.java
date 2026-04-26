package com.example.pillulkin.repository;

import com.example.pillulkin.entity.ReferenceMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferenceMedicineRepository extends JpaRepository<ReferenceMedicine, Long> {

    List<ReferenceMedicine> findByCategory(String category);

    @Query("SELECT m FROM ReferenceMedicine m WHERE LOWER(m.indications) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ReferenceMedicine> findByIndicationsContaining(@Param("keyword") String keyword);

    @Query("SELECT m FROM ReferenceMedicine m WHERE " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.activeSubstance) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.indications) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ReferenceMedicine> searchMedicines(@Param("keyword") String keyword);

    boolean existsByName(String name);
}