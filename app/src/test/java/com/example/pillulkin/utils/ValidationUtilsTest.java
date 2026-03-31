package com.example.pillulkin.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValidationUtilsTest {
    @Test
    public void isValidName_withValidName_returnsTrue() {
        assertTrue(ValidationUtils.isValidName("Иван"));
    }

    @Test
    public void isValidName_withEmptyString_returnsFalse() {
        assertFalse(ValidationUtils.isValidName(""));
    }

    @Test
    public void isValidName_withNull_returnsFalse() {
        assertFalse(ValidationUtils.isValidName(null));
    }

    @Test
    public void isValidName_withWhitespace_returnsFalse() {
        assertFalse(ValidationUtils.isValidName("   "));
    }

    @Test
    public void isValidAge_withValidAge_returnsTrue() {
        assertTrue(ValidationUtils.isValidAge(25));
    }

    @Test
    public void isValidAge_withZero_returnsFalse() {
        assertFalse(ValidationUtils.isValidAge(0));
    }

    @Test
    public void isValidAge_withNegative_returnsFalse() {
        assertFalse(ValidationUtils.isValidAge(-5));
    }

    @Test
    public void isValidAge_withTooHigh_returnsFalse() {
        assertFalse(ValidationUtils.isValidAge(150));
    }

    @Test
    public void isValidDosage_withValidDosage_returnsTrue() {
        assertTrue(ValidationUtils.isValidDosage("500 мг"));
    }

    @Test
    public void isValidDosage_withEmpty_returnsFalse() {
        assertFalse(ValidationUtils.isValidDosage(""));
    }

    @Test
    public void isValidDosage_withNull_returnsFalse() {
        assertFalse(ValidationUtils.isValidDosage(null));
    }

    @Test
    public void isValidQuantity_withValidQuantity_returnsTrue() {
        assertTrue(ValidationUtils.isValidQuantity(10));
    }

    @Test
    public void isValidQuantity_withZero_returnsTrue() {
        assertTrue(ValidationUtils.isValidQuantity(0));
    }

    @Test
    public void isValidQuantity_withNegative_returnsFalse() {
        assertFalse(ValidationUtils.isValidQuantity(-1));
    }

    @Test
    public void isValidExpirationDate_withValidDate_returnsTrue() {
        assertTrue(ValidationUtils.isValidExpirationDate("12.12.2025"));
    }

    @Test
    public void isValidExpirationDate_withInvalidDate_returnsFalse() {
        assertFalse(ValidationUtils.isValidExpirationDate("invalid"));
    }

    @Test
    public void isValidExpirationDate_withEmpty_returnsFalse() {
        assertFalse(ValidationUtils.isValidExpirationDate(""));
    }

    @Test
    public void isValidExpirationDate_withNull_returnsFalse() {
        assertFalse(ValidationUtils.isValidExpirationDate(null));
    }
}
