package com.example.pillulkin.domain.usecase;

import com.example.pillulkin.data.local.entity.DoctorAccessCodeEntity;
import com.example.pillulkin.data.repository.DoctorAccessCodeRepository;

public class ValidateAccessCodeUseCase {
    private final DoctorAccessCodeRepository repository;

    public ValidateAccessCodeUseCase(DoctorAccessCodeRepository repository) {
        this.repository = repository;
    }

    public ValidationResult execute(String code) {
        if (code == null || code.trim().isEmpty()) {
            return new ValidationResult(false, "Код не может быть пустым");
        }

        DoctorAccessCodeEntity codeEntity = repository.validateCode(code.trim().toUpperCase());

        if (codeEntity == null) {
            DoctorAccessCodeEntity expiredCode = repository.getActiveCode();
            if (expiredCode != null && expiredCode.getCode().equals(code.trim().toUpperCase())) {
                return new ValidationResult(false, "Код истёк");
            }
            return new ValidationResult(false, "Неверный код");
        }

        repository.markCodeAsUsed(code.trim().toUpperCase());
        return new ValidationResult(true, "Доступ предоставлен");
    }

    public static class ValidationResult {
        private final boolean isValid;
        private final String message;

        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }

        public boolean isValid() { return isValid; }
        public String getMessage() { return message; }
    }
}
