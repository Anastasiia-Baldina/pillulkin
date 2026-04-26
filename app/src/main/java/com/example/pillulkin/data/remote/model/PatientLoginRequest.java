package com.example.pillulkin.data.remote.model;

public class PatientLoginRequest {
    private String email;
    private String password;

    public PatientLoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
