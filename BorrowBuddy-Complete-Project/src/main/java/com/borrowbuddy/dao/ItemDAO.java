package com.borrowbuddy.dao;

import com.borrowbuddy.config.DatabaseConnection;
import com.borrowbuddy.model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO for the ITEMS table. Joins USERS and CATEGORIES for display fields. */
public class ItemDAO {

    private static final String BASE_SELECT =
        "SELECT i.*, u.name AS owner_name, c.category_name " +
        "FROM items i " +
        "JOIN users u ON i.owner_id = u.user_id " +
        "JOIN categories c ON i.category_id = c.category_id ";

    private Item mapRow(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setItemId(rs.getInt("item_id"));
        item.setOwnerId(rs.getInt("owner_id"));
        item.setCategoryId(rs.getInt("category_id"));
        item.setItemName(rs.getString("item_name"));
        item.setDescription(rs.getString("description"));
        item.setItemCondition(rs.getString("item_condition"));
        item.setImageUrl(rs.getString("image_url"));
        item.setAvailabilityStatus(rs.getString("availability_status"));
        item.setAllowBorrowing(rs.getBoolean("allow_borrowing"));
        item.setAllowTemporaryExchange(rs.getBoolean("allow_temporary_exchange"));
        item.setAllowPermanentExchange(rs.getBoolean("allow_permanent_exchange"));
        item.setCreatedAt(rs.getTimestamp("created_at"));
        item.setUpdatedAt(rs.getTimestamp("updated_at"));
        item.setOwnerName(rs.getString("owner_name"));
        item.setCategoryName(rs.getString("category_name"));
        return item;
    }

    public Optional<Item> findById(int itemId) {
        String sql = BASE_SELECT + "WHERE i.item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("ItemDAO.findById: " + e.getMessage()); }
        return Optional.empty();
    }

    public List<Item> findAll() {
        List<Item> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY i.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("ItemDAO.findAll: " + e.getMessage()); }
        return list;
    }

    public List<Item> findByOwner(int ownerId) {
        List<Item> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE i.owner_id = ? ORDER BY i.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("ItemDAO.findByOwner: " + e.getMessage()); }
        return list;
    }

    public List<Item> findAvailable() {
        List<Item> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE i.availability_status = 'AVAILABLE' ORDER BY i.item_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("ItemDAO.findAvailable: " + e.getMessage()); }
        return list;
    }

    /**
     * Dynamic search with optional filters.
     * All parameters are optional (null means no filter).
     */
    public List<Item> search(String keyword, Integer categoryId, String condition, String availability) {
        List<Item> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (i.item_name LIKE ? OR i.description LIKE ?) ");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (categoryId != null && categoryId > 0) {
            sql.append("AND i.category_id = ? ");
            params.add(categoryId);
        }
        if (condition != null && !condition.isBlank() && !condition.equals("All")) {
            sql.append("AND i.item_condition = ? ");
            params.add(condition);
        }
        if (availability != null && !availability.isBlank() && !availability.equals("All")) {
            sql.append("AND i.availability_status = ? ");
            params.add(availability);
        }
        sql.append("ORDER BY i.item_name");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("ItemDAO.search: " + e.getMessage()); }
        return list;
    }

    public int save(Item item) {
        String sql = "INSERT INTO items (owner_id, category_id, item_name, description, item_condition, " +
                     "image_url, availability_status, allow_borrowing, allow_temporary_exchange, allow_permanent_exchange) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getOwnerId());
            ps.setInt(2, item.getCategoryId());
            ps.setString(3, item.getItemName());
            ps.setString(4, item.getDescription());
            ps.setString(5, item.getItemCondition());
            ps.setString(6, item.getImageUrl());
            ps.setString(7, item.getAvailabilityStatus() != null ? item.getAvailabilityStatus() : "AVAILABLE");
            ps.setBoolean(8, item.isAllowBorrowing());
            ps.setBoolean(9, item.isAllowTemporaryExchange());
            ps.setBoolean(10, item.isAllowPermanentExchange());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) { System.err.println("ItemDAO.save: " + e.getMessage()); }
        return -1;
    }

    public boolean update(Item item) {
        String sql = "UPDATE items SET category_id=?, item_name=?, description=?, item_condition=?, " +
                     "image_url=?, availability_status=?, allow_borrowing=?, allow_temporary_exchange=?, " +
                     "allow_permanent_exchange=? WHERE item_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getCategoryId());
            ps.setString(2, item.getItemName());
            ps.setString(3, item.getDescription());
            ps.setString(4, item.getItemCondition());
            ps.setString(5, item.getImageUrl());
            ps.setString(6, item.getAvailabilityStatus());
            ps.setBoolean(7, item.isAllowBorrowing());
            ps.setBoolean(8, item.isAllowTemporaryExchange());
            ps.setBoolean(9, item.isAllowPermanentExchange());
            ps.setInt(10, item.getItemId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("ItemDAO.update: " + e.getMessage()); }
        return false;
    }

    public boolean updateAvailabilityStatus(int itemId, String status) {
        String sql = "UPDATE items SET availability_status=? WHERE item_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("ItemDAO.updateAvailability: " + e.getMessage()); }
        return false;
    }

    /** Updates item owner_id — used during permanent exchange. Requires existing conn for atomicity. */
    public boolean updateOwner(Connection conn, int itemId, int newOwnerId) throws SQLException {
        String sql = "UPDATE items SET owner_id=? WHERE item_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newOwnerId);
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int itemId) {
        String sql = "DELETE FROM items WHERE item_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("ItemDAO.delete: " + e.getMessage()); }
        return false;
    }

    public int countAll() {
        return countWhere("");
    }

    public int countAvailable() {
        return countWhere("WHERE availability_status = 'AVAILABLE'");
    }

    private int countWhere(String where) {
        String sql = "SELECT COUNT(*) FROM items " + where;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("ItemDAO.count: " + e.getMessage()); }
        return 0;
    }

    /**
     * Returns true if the item is currently involved in a PENDING, ACCEPTED, or ACTIVE transaction.
     */
    public boolean isInActiveTransaction(int itemId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE " +
                     "(requested_item_id = ? OR offered_item_id = ?) " +
                     "AND status IN ('PENDING','ACCEPTED','ACTIVE')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { System.err.println("ItemDAO.isInActiveTransaction: " + e.getMessage()); }
        return false;
    }
}
