package com.example.pillulkin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_medicine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reference_medicine_id")
    private ReferenceMedicine referenceMedicine;

    @Column(name = "medicine_name", length = 500)
    private String medicineName;

    @Column(name = "medicine_dosage", length = 255)
    private String medicineDosage;

    @Column(name = "medicine_form", length = 100)
    private String medicineForm;

    @Column(name = "medicine_active_substance", columnDefinition = "TEXT")
    private String medicineActiveSubstance;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "quantity")
    private String quantity;
}