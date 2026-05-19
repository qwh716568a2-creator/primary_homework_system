package com.primaryhomework.backend.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordSupportTest {

    @Test
    void shouldMatchEncodedPassword() {
        String encoded = PasswordSupport.encode("abc123");

        assertTrue(PasswordSupport.isEncoded(encoded));
        assertTrue(PasswordSupport.matches("abc123", encoded));
        assertFalse(PasswordSupport.needsUpgrade(encoded));
    }

    @Test
    void shouldSupportLegacyPlainTextPassword() {
        assertTrue(PasswordSupport.matches("123456", "123456"));
        assertTrue(PasswordSupport.needsUpgrade("123456"));
    }

    @Test
    void shouldRejectWrongPassword() {
        String encoded = PasswordSupport.encode("abc123");

        assertFalse(PasswordSupport.matches("abc124", encoded));
        assertFalse(PasswordSupport.matches("abc124", "abc123"));
    }
}
