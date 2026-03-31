package com.example.pillulkin.domain.usecase;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValidateAccessCodeUseCaseTest {
    @Test
    public void ValidationResult_withValid_returnsTrue() {
        ValidateAccessCodeUseCase.ValidationResult result = 
            new ValidateAccessCodeUseCase.ValidationResult(true, "Доступ предоставлен");
        assertTrue(result.isValid());
        assertEquals("Доступ предоставлен", result.getMessage());
    }

    @Test
    public void ValidationResult_withInvalid_returnsFalse() {
        ValidateAccessCodeUseCase.ValidationResult result = 
            new ValidateAccessCodeUseCase.ValidationResult(false, "Неверный код");
        assertFalse(result.isValid());
        assertEquals("Неверный код", result.getMessage());
    }
}
