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

    @Query(value = "SELECT * FROM reference_medicine WHERE similarity(indications, :keyword) > 0.2 ORDER BY similarity(indications, :keyword) DESC", nativeQuery = true)
    List<ReferenceMedicine> findByIndicationsContaining(@Param("keyword") String keyword);

    @Query(value = "SELECT * FROM reference_medicine WHERE " +
           "similarity(name, :keyword) > 0.2 OR " +
           "similarity(active_substance, :keyword) > 0.2 OR " +
           "similarity(indications, :keyword) > 0.2 " +
           "ORDER BY GREATEST(similarity(name, :keyword), similarity(active_substance, :keyword), similarity(indications, :keyword)) DESC", nativeQuery = true)
    List<ReferenceMedicine> searchMedicines(@Param("keyword") String keyword);

    boolean existsByName(String name);
}