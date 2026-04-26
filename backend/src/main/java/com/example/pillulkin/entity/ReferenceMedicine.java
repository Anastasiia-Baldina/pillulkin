package com.example.pillulkin.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reference_medicine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferenceMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "dosage", length = 255)
    private String dosage;

    @Column(name = "form", length = 100)
    private String form;

    @Column(name = "active_substance", columnDefinition = "TEXT")
    private String activeSubstance;

    @Column(name = "indications", columnDefinition = "TEXT")
    private String indications;

    @Column(name = "contraindications", columnDefinition = "TEXT")
    private String contraindications;

    @Column(name = "category", length = 255)
    private String category;
}