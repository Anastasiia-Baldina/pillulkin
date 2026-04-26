package com.example.pillulkin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorFullDataResponse {

    private Long patientId;
    private String email;
    private PatientProfileResponse profile;
    private List<PatientSymptomResponse> symptoms;
    private List<PatientMedicineResponse> medicines;
}