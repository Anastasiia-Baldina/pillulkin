package com.example.pillulkin.data.remote.model;

public class PatientRegisterRequest {
    private String email;
    private String password;

    public PatientRegisterRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
