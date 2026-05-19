package com.primaryhomework.backend.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

public final class PasswordSupport {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordSupport() {
    }

    public static String encode(String rawPassword) {
        String password = text(rawPassword);
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("password cannot be blank");
        }
        return ENCODER.encode(password);
    }

    public static boolean matches(String rawPassword, String storedPassword) {
        String password = text(rawPassword);
        String savedPassword = text(storedPassword);
        if (!StringUtils.hasText(password) || !StringUtils.hasText(savedPassword)) {
            return false;
        }
        if (isEncoded(savedPassword)) {
            return ENCODER.matches(password, savedPassword);
        }
        return password.equals(savedPassword);
    }

    public static boolean needsUpgrade(String storedPassword) {
        return StringUtils.hasText(text(storedPassword)) && !isEncoded(storedPassword);
    }

    public static boolean isEncoded(String storedPassword) {
        String savedPassword = text(storedPassword);
        return savedPassword.startsWith("$2a$")
                || savedPassword.startsWith("$2b$")
                || savedPassword.startsWith("$2y$");
    }

    private static String text(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }
}
