package com.borrowbuddy.dao;

import com.borrowbuddy.config.DatabaseConnection;
import com.borrowbuddy.model.ItemOwnershipHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO for the ITEM_OWNERSHIP_HISTORY table. */
public class OwnershipHistoryDAO {

    private ItemOwnershipHistory mapRow(ResultSet rs) throws SQLException {
        ItemOwnershipHistory h = new ItemOwnershipHistory();
        h.setHistoryId(rs.getInt("history_id"));
        h.setItemId(rs.getInt("item_id"));
        h.setPreviousOwnerId(rs.getInt("previous_owner_id"));
        h.setNewOwnerId(rs.getInt("new_owner_id"));
        h.setTransactionId(rs.getInt("transaction_id"));
        h.setTransferDate(rs.getTimestamp("transfer_date"));
        try {
            h.setItemName(rs.getString("item_name"));
            h.setPreviousOwnerName(rs.getString("prev_owner_name"));
            h.setNewOwnerName(rs.getString("new_owner_name"));
        } catch (SQLException ignored) {}
        return h;
    }

    public List<ItemOwnershipHistory> findByItem(int itemId) {
        List<ItemOwnershipHistory> list = new ArrayList<>();
        String sql = "SELECT h.*, i.item_name, pu.name AS prev_owner_name, nu.name AS new_owner_name " +
                     "FROM item_ownership_history h " +
                     "JOIN items i ON h.item_id = i.item_id " +
                     "JOIN users pu ON h.previous_owner_id = pu.user_id " +
                     "JOIN users nu ON h.new_owner_id = nu.user_id " +
                     "WHERE h.item_id = ? ORDER BY h.transfer_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("OwnershipHistoryDAO.findByItem: " + e.getMessage()); }
        return list;
    }

    public List<ItemOwnershipHistory> findByTransaction(int transactionId) {
        List<ItemOwnershipHistory> list = new ArrayList<>();
        String sql = "SELECT h.*, i.item_name, pu.name AS prev_owner_name, nu.name AS new_owner_name " +
                     "FROM item_ownership_history h " +
                     "JOIN items i ON h.item_id = i.item_id " +
                     "JOIN users pu ON h.previous_owner_id = pu.user_id " +
                     "JOIN users nu ON h.new_owner_id = nu.user_id " +
                     "WHERE h.transaction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("OwnershipHistoryDAO.findByTransaction: " + e.getMessage()); }
        return list;
    }

    public int save(ItemOwnershipHistory h) {
        String sql = "INSERT INTO item_ownership_history (item_id, previous_owner_id, new_owner_id, transaction_id) " +
                     "VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, h.getItemId());
            ps.setInt(2, h.getPreviousOwnerId());
            ps.setInt(3, h.getNewOwnerId());
            ps.setInt(4, h.getTransactionId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) { System.err.println("OwnershipHistoryDAO.save: " + e.getMessage()); }
        return -1;
    }

    /** Save using existing connection for atomicity. */
    public int save(ItemOwnershipHistory h, Connection conn) throws SQLException {
        String sql = "INSERT INTO item_ownership_history (item_id, previous_owner_id, new_owner_id, transaction_id) " +
                     "VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, h.getItemId());
            ps.setInt(2, h.getPreviousOwnerId());
            ps.setInt(3, h.getNewOwnerId());
            ps.setInt(4, h.getTransactionId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }
}
