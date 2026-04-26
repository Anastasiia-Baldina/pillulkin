package com.example.pillulkin.data.remote.model;

import java.util.List;

public class DoctorFullDataResponse {
    private Long patientId;
    private String email;
    private PatientProfileResponse profile;
    private List<PatientSymptomResponse> symptoms;
    private List<PatientMedicineResponse> medicines;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PatientProfileResponse getProfile() {
        return profile;
    }

    public void setProfile(PatientProfileResponse profile) {
        this.profile = profile;
    }

    public List<PatientSymptomResponse> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<PatientSymptomResponse> symptoms) {
        this.symptoms = symptoms;
    }

    public List<PatientMedicineResponse> getMedicines() {
        return medicines;
    }

    public void setMedicines(List<PatientMedicineResponse> medicines) {
        this.medicines = medicines;
    }
}
