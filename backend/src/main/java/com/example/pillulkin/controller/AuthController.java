package com.example.pillulkin.controller;

import com.example.pillulkin.dto.*;
import com.example.pillulkin.service.AuthService;
import com.example.pillulkin.service.DoctorAccessService;
import com.example.pillulkin.service.GoogleAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final DoctorAccessService doctorAccessService;
    private final GoogleAuthService googleAuthService;

    @PostMapping("/patient/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleAuthRequest request) {
        var userInfo = googleAuthService.verifyToken(request.getIdToken());
        var patient = googleAuthService.findOrCreatePatient(userInfo.email(), userInfo.name());
        String token = authService.generateToken(patient.getId());
        var response = AuthResponse.builder()
                .patientId(patient.getId())
                .email(patient.getEmail())
                .token(token)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/doctor/generate-code")
    public ResponseEntity<DoctorCodeResponse> generateCode(@Valid @RequestBody DoctorGenerateCodeRequest request) {
        String code = doctorAccessService.generateCode(request.getPatientId(), request.getExpiresInMinutes());
        var response = DoctorCodeResponse.builder()
                .code(code)
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(
                        request.getExpiresInMinutes() != null ? request.getExpiresInMinutes() : 60
                ).toString())
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/doctor/login")
    public ResponseEntity<AuthResponse> doctorLogin(@Valid @RequestBody DoctorLoginRequest request) {
        var result = doctorAccessService.loginWithCode(request.getCode());
        var response = AuthResponse.builder()
                .patientId(result.patientId())
                .token(result.token())
                .build();
        return ResponseEntity.ok(response);
    }
}
