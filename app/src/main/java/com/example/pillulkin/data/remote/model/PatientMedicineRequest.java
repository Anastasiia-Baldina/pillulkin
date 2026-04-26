package com.example.pillulkin.data.remote.model;

public class PatientMedicineRequest {
    private Long medicineId;
    private String expirationDate;
    private String quantity;

    public PatientMedicineRequest(Long medicineId, String expirationDate, String quantity) {
        this.medicineId = medicineId;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
    }
}
