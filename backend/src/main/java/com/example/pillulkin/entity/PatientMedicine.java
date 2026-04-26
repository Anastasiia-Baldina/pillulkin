package com.example.pillulkin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_medicine", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"patient_id", "reference_medicine_id"})
})
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
    @JoinColumn(name = "reference_medicine_id", nullable = false)
    private ReferenceMedicine referenceMedicine;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "quantity")
    private String quantity;
}