package com.borrowbuddy.service;

import com.borrowbuddy.dao.CategoryDAO;
import com.borrowbuddy.dao.ItemDAO;
import com.borrowbuddy.dao.TransactionDAO;
import com.borrowbuddy.dao.UserDAO;
import com.borrowbuddy.model.Category;
import com.borrowbuddy.model.Transaction;
import com.borrowbuddy.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service providing administrative functions:
 * user management, category administration, item moderation, and system reports.
 */
public class AdminService {

    private final UserDAO userDAO = new UserDAO();
    private final ItemDAO itemDAO = new ItemDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    // User Administration
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public boolean updateUserRole(int userId, String newRole) {
        return userDAO.updateRole(userId, newRole);
    }

    public boolean deleteUser(int userId) throws Exception {
        // Prevent deleting users involved in active transactions
        List<Transaction> active = transactionDAO.findByUser(userId).stream()
                .filter(t -> "ACTIVE".equalsIgnoreCase(t.getStatus()) || "PENDING".equalsIgnoreCase(t.getStatus()))
                .toList();
        if (!active.isEmpty()) {
            throw new IllegalStateException("Cannot delete user while they have active or pending transactions.");
        }
        return userDAO.delete(userId);
    }

    // Category Administration
    public Category createCategory(String name, String description) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        if (categoryDAO.findByName(name.trim()).isPresent()) {
            throw new IllegalArgumentException("A category with this name already exists.");
        }
        Category cat = new Category();
        cat.setCategoryName(name.trim());
        cat.setDescription(description);
        int id = categoryDAO.save(cat);
        if (id <= 0) {
            throw new Exception("Failed to create category.");
        }
        cat.setCategoryId(id);
        return cat;
    }

    public boolean updateCategory(Category c) {
        return categoryDAO.update(c);
    }

    public boolean deleteCategory(int id) throws Exception {
        // Check if items belong to this category
        boolean hasItems = itemDAO.findAll().stream().anyMatch(i -> i.getCategoryId() == id);
        if (hasItems) {
            throw new IllegalStateException("Cannot delete category containing existing items. Reassign items first.");
        }
        return categoryDAO.delete(id);
    }

    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    // Item Moderation
    public boolean markItemUnavailable(int itemId) {
        return itemDAO.updateAvailabilityStatus(itemId, "UNAVAILABLE");
    }

    public boolean restoreItem(int itemId) {
        return itemDAO.updateAvailabilityStatus(itemId, "AVAILABLE");
    }

    // System Reports & Stats
    public List<Transaction> getOverdueReport() {
        return transactionDAO.findOverdue();
    }

    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userDAO.countAll());
        stats.put("totalItems", itemDAO.countAll());
        stats.put("availableItems", itemDAO.countAvailable());
        stats.put("pendingRequests", transactionDAO.countPending());
        stats.put("activeTransactions", transactionDAO.countActive());
        stats.put("overdueTransactions", transactionDAO.countOverdue());
        stats.put("completedTransactions", transactionDAO.countCompleted());
        return stats;
    }
}
