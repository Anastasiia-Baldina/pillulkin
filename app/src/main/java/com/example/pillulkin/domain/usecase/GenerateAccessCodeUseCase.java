package com.example.pillulkin.domain.usecase;

import com.example.pillulkin.data.local.entity.DoctorAccessCodeEntity;
import com.example.pillulkin.data.repository.DoctorAccessCodeRepository;
import com.example.pillulkin.utils.CodeGenerator;

public class GenerateAccessCodeUseCase {
    private final DoctorAccessCodeRepository repository;

    public GenerateAccessCodeUseCase(DoctorAccessCodeRepository repository) {
        this.repository = repository;
    }

    public String execute() {
        String code = CodeGenerator.generateCode();
        long now = System.currentTimeMillis();
        long expiresAt = now + (60 * 60 * 1000);

        DoctorAccessCodeEntity codeEntity = new DoctorAccessCodeEntity(code, now, expiresAt, "ACTIVE");
        repository.insert(codeEntity);

        return code;
    }
}
