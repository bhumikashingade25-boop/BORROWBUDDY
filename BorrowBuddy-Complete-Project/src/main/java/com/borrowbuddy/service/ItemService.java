package com.borrowbuddy.service;

import com.borrowbuddy.dao.ItemDAO;
import com.borrowbuddy.dao.TransactionDAO;
import com.borrowbuddy.model.Item;
import com.borrowbuddy.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for item catalog management, listing CRUD operations,
 * search filters, and dashboard item statistics.
 */
public class ItemService {

    private final ItemDAO itemDAO = new ItemDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public Item createItem(Item item, User owner) throws Exception {
        if (owner == null) {
            throw new IllegalArgumentException("User must be logged in to create an item.");
        }
        if (item.getItemName() == null || item.getItemName().trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty.");
        }
        if (item.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Please select a valid item category.");
        }
        if (item.getItemCondition() == null || item.getItemCondition().trim().isEmpty()) {
            item.setItemCondition("GOOD");
        }
        if (!item.isAllowBorrowing() && !item.isAllowTemporaryExchange() && !item.isAllowPermanentExchange()) {
            throw new IllegalArgumentException("At least one transaction type (Borrow, Temp Exchange, or Perm Exchange) must be enabled.");
        }

        item.setOwnerId(owner.getUserId());
        item.setAvailabilityStatus("AVAILABLE");
        int generatedId = itemDAO.save(item);
        if (generatedId <= 0) {
            throw new Exception("Failed to save item to database.");
        }
        item.setItemId(generatedId);
        return item;
    }

    public boolean updateItem(Item item, User currentUser) throws Exception {
        Optional<Item> existingOpt = itemDAO.findById(item.getItemId());
        if (existingOpt.isEmpty()) {
            throw new Exception("Item not found.");
        }
        Item existing = existingOpt.get();

        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        if (existing.getOwnerId() != currentUser.getUserId() && !isAdmin) {
            throw new SecurityException("Unauthorized: You do not have permission to edit this item.");
        }

        if ("UNAVAILABLE".equalsIgnoreCase(item.getAvailabilityStatus()) &&
            itemDAO.isInActiveTransaction(item.getItemId())) {
            throw new IllegalStateException("Cannot mark item unavailable while it is involved in an active or pending transaction.");
        }

        return itemDAO.update(item);
    }

    public boolean deleteItem(int itemId, User currentUser) throws Exception {
        Optional<Item> existingOpt = itemDAO.findById(itemId);
        if (existingOpt.isEmpty()) {
            throw new Exception("Item not found.");
        }
        Item existing = existingOpt.get();

        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        if (existing.getOwnerId() != currentUser.getUserId() && !isAdmin) {
            throw new SecurityException("Unauthorized: You do not have permission to delete this item.");
        }

        if (itemDAO.isInActiveTransaction(itemId)) {
            throw new IllegalStateException("Cannot delete item while it is currently involved in a pending or active transaction.");
        }

        return itemDAO.delete(itemId);
    }

    public List<Item> searchItems(String keyword, Integer categoryId, String condition, String availability) {
        return itemDAO.search(keyword, categoryId, condition, availability);
    }

    public List<Item> getItemsByOwner(int ownerId) {
        return itemDAO.findByOwner(ownerId);
    }

    public List<Item> getAllItems() {
        return itemDAO.findAll();
    }

    public List<Item> getAvailableItems() {
        return itemDAO.findAvailable();
    }

    public Optional<Item> getItemById(int itemId) {
        return itemDAO.findById(itemId);
    }

    public boolean isAvailable(int itemId) {
        Optional<Item> opt = itemDAO.findById(itemId);
        return opt.isPresent() && "AVAILABLE".equalsIgnoreCase(opt.get().getAvailabilityStatus());
    }

    /**
     * Aggregates real-time counts from MySQL for dashboard presentation.
     */
    public Map<String, Integer> getDashboardStats() {
        return getDashboardStats(null);
    }

    /**
     * Aggregates real-time counts from MySQL.
     * When user is specified (non-admin), filters transactions to that user's view.
     */
    public Map<String, Integer> getDashboardStats(User user) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalItems", itemDAO.countAll());
        stats.put("availableItems", itemDAO.countAvailable());

        if (user != null && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            int uid = user.getUserId();
            stats.put("pendingRequests", transactionDAO.countPendingByUser(uid));
            stats.put("activeTransactions", transactionDAO.countActiveByUser(uid));
            stats.put("overdueTransactions", transactionDAO.countOverdueByUser(uid));
            stats.put("completedTransactions", transactionDAO.countCompletedByUser(uid));
            stats.put("tempExchanges", transactionDAO.countActiveTempExchangesByUser(uid));
            stats.put("permExchanges", transactionDAO.countPermExchangesByUser(uid));
        } else {
            stats.put("pendingRequests", transactionDAO.countPending());
            stats.put("activeTransactions", transactionDAO.countActive());
            stats.put("overdueTransactions", transactionDAO.countOverdue());
            stats.put("completedTransactions", transactionDAO.countCompleted());
            stats.put("tempExchanges", transactionDAO.countActiveTemporaryExchanges());
            stats.put("permExchanges", transactionDAO.countPendingPermanentExchanges());
        }
        return stats;
    }
}
