package com.borrowbuddy.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password Utility & BCrypt Tests")
public class PasswordUtilTest {

    @Test
    @DisplayName("Should hash password and verify successfully")
    void testHashAndVerify() {
        String rawPassword = "StrongPassword@123";
        String hash = PasswordUtil.hashPassword(rawPassword);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
        assertTrue(PasswordUtil.verifyPassword(rawPassword, hash));
        assertFalse(PasswordUtil.verifyPassword("WrongPassword@123", hash));
    }

    @Test
    @DisplayName("Should generate different hashes for same password (salted)")
    void testSaltUniqueness() {
        String raw = "CampusSecret#99";
        String hash1 = PasswordUtil.hashPassword(raw);
        String hash2 = PasswordUtil.hashPassword(raw);

        assertNotEquals(hash1, hash2);
        assertTrue(PasswordUtil.verifyPassword(raw, hash1));
        assertTrue(PasswordUtil.verifyPassword(raw, hash2));
    }
}
