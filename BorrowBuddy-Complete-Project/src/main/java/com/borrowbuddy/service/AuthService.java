package com.borrowbuddy.service;

import com.borrowbuddy.dao.UserDAO;
import com.borrowbuddy.model.User;
import com.borrowbuddy.util.PasswordUtil;

import java.util.Optional;

/**
 * Service for managing user authentication and active session state.
 * Implemented as a thread-safe singleton.
 */
public class AuthService {

    private static volatile AuthService instance;
    private final UserDAO userDAO = new UserDAO();
    private User currentUser;

    private AuthService() {}

    public static AuthService getInstance() {
        if (instance == null) {
            synchronized (AuthService.class) {
                if (instance == null) {
                    instance = new AuthService();
                }
            }
        }
        return instance;
    }

    /**
     * Authenticates a user by email and password.
     * Updates the current session user upon success.
     */
    public synchronized User login(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Email and password cannot be empty.");
        }

        String cleanEmail = email.trim().toLowerCase();
        Optional<User> userOpt = userDAO.findByEmail(cleanEmail);

        if (userOpt.isEmpty()) {
            throw new Exception("Invalid email or password.");
        }

        User user = userOpt.get();
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            throw new Exception("Invalid email or password.");
        }

        this.currentUser = user;
        return user;
    }

    /**
     * Registers a new user with validation and hashes their password.
     */
    public synchronized User register(User user, String plainPassword) throws Exception {
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (user.getEmail() == null || !user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (plainPassword == null || plainPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
        boolean hasDigit = plainPassword.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = plainPassword.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);
        if (!hasDigit || !hasSpecial) {
            throw new IllegalArgumentException("Password must contain at least one digit and one special character.");
        }

        String cleanEmail = user.getEmail().trim().toLowerCase();
        if (userDAO.findByEmail(cleanEmail).isPresent()) {
            throw new Exception("An account with this email address already exists.");
        }

        user.setEmail(cleanEmail);
        user.setPasswordHash(PasswordUtil.hashPassword(plainPassword));
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("STUDENT");
        }

        int generatedId = userDAO.save(user);
        if (generatedId <= 0) {
            throw new Exception("Failed to register user. Database error occurred.");
        }

        user.setUserId(generatedId);
        return user;
    }

    public synchronized void logout() {
        this.currentUser = null;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public synchronized boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }
}
