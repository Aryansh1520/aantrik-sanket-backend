package com.aantriksanket.backend.util.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class PasswordHasher {

    private static final int STRENGTH = 12;
    private static final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder(STRENGTH);

    private PasswordHasher() {
        // prevent instantiation
    }

    public static String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }
}
