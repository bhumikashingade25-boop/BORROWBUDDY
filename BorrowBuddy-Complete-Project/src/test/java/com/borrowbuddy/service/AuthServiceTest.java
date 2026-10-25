package com.borrowbuddy.service;

import com.borrowbuddy.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Authentication Service Tests")
public class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = AuthService.getInstance();
        authService.logout();
    }

    @Test
    @DisplayName("Should successfully authenticate existing seed student")
    void testSuccessfulLogin() throws Exception {
        User user = authService.login("aarav@campus.edu", "Password@123");
        assertNotNull(user);
        assertEquals("aarav@campus.edu", user.getEmail());
        assertTrue(authService.isLoggedIn());
        assertEquals(user.getUserId(), authService.getCurrentUser().getUserId());
    }

    @Test
    @DisplayName("Should fail authentication with invalid credentials")
    void testFailedLogin() {
        assertThrows(Exception.class, () -> authService.login("aarav@campus.edu", "WrongPassword!999"));
        assertThrows(Exception.class, () -> authService.login("nonexistent@campus.edu", "Password@123"));
        assertFalse(authService.isLoggedIn());
    }

    @Test
    @DisplayName("Should reject registration with weak password")
    void testWeakPasswordRegistration() {
        User u = new User();
        u.setName("Test Student");
        u.setEmail("teststudent@campus.edu");
        u.setRole("STUDENT");

        // Too short
        assertThrows(IllegalArgumentException.class, () -> authService.register(u, "short1!"));
        // Missing special char
        assertThrows(IllegalArgumentException.class, () -> authService.register(u, "NoSpecialChar123"));
        // Missing digit
        assertThrows(IllegalArgumentException.class, () -> authService.register(u, "NoDigitPassword!"));
    }

    @Test
    @DisplayName("Should reject registration with invalid email format")
    void testInvalidEmailRegistration() {
        User u = new User();
        u.setName("Test Student");
        u.setEmail("invalid-email-address");
        u.setRole("STUDENT");

        assertThrows(IllegalArgumentException.class, () -> authService.register(u, "ValidPass@123"));
    }

    @Test
    @DisplayName("Should reject registration with duplicate email")
    void testDuplicateEmailRegistration() {
        User u = new User();
        u.setName("Aarav Clone");
        u.setEmail("aarav@campus.edu"); // already exists
        u.setRole("STUDENT");

        assertThrows(Exception.class, () -> authService.register(u, "SecurePass@123"));
    }

    @Test
    @DisplayName("Should clear session on logout")
    void testLogout() throws Exception {
        authService.login("admin@campus.edu", "Admin@123");
        assertTrue(authService.isLoggedIn());
        assertTrue(authService.isAdmin());

        authService.logout();
        assertFalse(authService.isLoggedIn());
        assertNull(authService.getCurrentUser());
    }
}
