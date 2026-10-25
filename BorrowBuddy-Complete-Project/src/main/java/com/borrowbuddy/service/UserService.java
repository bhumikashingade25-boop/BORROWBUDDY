package com.borrowbuddy.service;

import com.borrowbuddy.dao.UserDAO;
import com.borrowbuddy.model.User;
import com.borrowbuddy.util.PasswordUtil;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for User operations.
 * Handles authentication, registration, and profile management.
 */
public class UserService {
    private final UserDAO userDAO = new UserDAO();

    /**
     * Authenticates a user by email and password.
     * @return Optional<User> if credentials are valid, empty otherwise.
     */
    public Optional<User> login(String email, String password) {
        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            return Optional.empty();
        }
        return userDAO.findByEmail(email.trim().toLowerCase()).filter(
            u -> PasswordUtil.verifyPassword(password, u.getPasswordHash())
        );
    }

    /**
     * Registers a new user. Returns the generated user_id, or -1 if registration fails.
     * Validates email uniqueness and password strength before inserting.
     */
    public int register(String name, String email, String password, String role,
                        String department, String phone) throws IllegalArgumentException {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be empty.");
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("Invalid email format.");
        if (password == null || password.length() < 8)
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        if (userDAO.findByEmail(email.trim().toLowerCase()).isPresent())
            throw new IllegalArgumentException("Email is already registered.");

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setRole(role != null ? role : "STUDENT");
        user.setDepartment(department);
        user.setPhone(phone);
        return userDAO.save(user);
    }

    public Optional<User> findById(int userId) {
        return userDAO.findById(userId);
    }

    public Optional<User> findByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    public List<User> findAll() {
        return userDAO.findAll();
    }

    public List<User> findByRole(String role) {
        return userDAO.findByRole(role);
    }

    public boolean updateProfile(User user) {
        if (user.getName() == null || user.getName().isBlank())
            throw new IllegalArgumentException("Name cannot be empty.");
        return userDAO.update(user);
    }

    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) return false;
        User user = userOpt.get();
        if (!PasswordUtil.verifyPassword(currentPassword, user.getPasswordHash())) return false;
        if (newPassword == null || newPassword.length() < 8)
            throw new IllegalArgumentException("New password must be at least 8 characters.");
        return userDAO.updatePassword(userId, PasswordUtil.hashPassword(newPassword));
    }

    /** Admin-only: reset another user's password. */
    public boolean resetPassword(int userId, String newPassword) {
        if (newPassword == null || newPassword.length() < 8)
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        return userDAO.updatePassword(userId, PasswordUtil.hashPassword(newPassword));
    }

    public boolean updateRole(int userId, String newRole) {
        return userDAO.updateRole(userId, newRole);
    }

    public boolean deleteUser(int userId) {
        return userDAO.delete(userId);
    }

    public int countAll() {
        return userDAO.countAll();
    }
}
