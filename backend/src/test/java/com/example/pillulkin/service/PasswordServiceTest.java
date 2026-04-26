package com.example.pillulkin.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    private final PasswordService passwordService = new PasswordService();

    @Test
    void shouldHashPassword() {
        String rawPassword = "testPassword123";

        String hashed = passwordService.hash(rawPassword);

        assertNotNull(hashed);
        assertNotEquals(rawPassword, hashed);
        assertTrue(hashed.startsWith("$2a$"));
    }

    @Test
    void shouldMatchHashedPassword() {
        String rawPassword = "testPassword123";
        String hashed = passwordService.hash(rawPassword);

        boolean matches = passwordService.matches(rawPassword, hashed);

        assertTrue(matches);
    }

    @Test
    void shouldNotMatchWrongPassword() {
        String rawPassword = "testPassword123";
        String hashed = passwordService.hash(rawPassword);

        boolean matches = passwordService.matches("wrongPassword", hashed);

        assertFalse(matches);
    }
}