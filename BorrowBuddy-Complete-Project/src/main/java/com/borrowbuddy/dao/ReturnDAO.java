package com.borrowbuddy.dao;

import com.borrowbuddy.config.DatabaseConnection;
import com.borrowbuddy.model.TransactionReturn;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO for the TRANSACTION_RETURNS table. */
public class ReturnDAO {

    private TransactionReturn mapRow(ResultSet rs) throws SQLException {
        TransactionReturn r = new TransactionReturn();
        r.setReturnId(rs.getInt("return_id"));
        r.setTransactionId(rs.getInt("transaction_id"));
        r.setItemId(rs.getInt("item_id"));
        r.setReturnedBy(rs.getInt("returned_by"));
        int confBy = rs.getInt("confirmed_by");
        r.setConfirmedBy(rs.wasNull() ? null : confBy);
        r.setReturnedAt(rs.getTimestamp("returned_at"));
        r.setStatus(rs.getString("status"));
        try { r.setItemName(rs.getString("item_name")); } catch (SQLException ignored) {}
        return r;
    }

    public Optional<TransactionReturn> findById(int id) {
        String sql = "SELECT tr.*, i.item_name FROM transaction_returns tr " +
                     "JOIN items i ON tr.item_id = i.item_id WHERE tr.return_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("ReturnDAO.findById: " + e.getMessage()); }
        return Optional.empty();
    }

    public List<TransactionReturn> findByTransaction(int transactionId) {
        List<TransactionReturn> list = new ArrayList<>();
        String sql = "SELECT tr.*, i.item_name FROM transaction_returns tr " +
                     "JOIN items i ON tr.item_id = i.item_id WHERE tr.transaction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("ReturnDAO.findByTransaction: " + e.getMessage()); }
        return list;
    }

    public Optional<TransactionReturn> findByTransactionAndItem(int transactionId, int itemId) {
        String sql = "SELECT tr.*, i.item_name FROM transaction_returns tr " +
                     "JOIN items i ON tr.item_id = i.item_id " +
                     "WHERE tr.transaction_id = ? AND tr.item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            ps.setInt(2, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("ReturnDAO.findByTxnAndItem: " + e.getMessage()); }
        return Optional.empty();
    }

    public int save(TransactionReturn ret) {
        String sql = "INSERT INTO transaction_returns (transaction_id, item_id, returned_by, status) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, ret.getTransactionId());
            ps.setInt(2, ret.getItemId());
            ps.setInt(3, ret.getReturnedBy());
            ps.setString(4, "PENDING");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) { System.err.println("ReturnDAO.save: " + e.getMessage()); }
        return -1;
    }

    public boolean confirm(int returnId, int confirmedBy) {
        String sql = "UPDATE transaction_returns SET status='RETURNED', confirmed_by=?, returned_at=COALESCE(returned_at, CURRENT_TIMESTAMP) WHERE return_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, confirmedBy);
            ps.setInt(2, returnId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("ReturnDAO.confirm: " + e.getMessage()); }
        return false;
    }

    /** Returns true if all return records for the transaction are confirmed/returned. */
    public boolean allReturned(int transactionId) {
        String sql = "SELECT COUNT(*) total, SUM(status IN ('CONFIRMED','RETURNED')) confirmed " +
                     "FROM transaction_returns WHERE transaction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int confirmed = rs.getInt("confirmed");
                    return total > 0 && total == confirmed;
                }
            }
        } catch (SQLException e) { System.err.println("ReturnDAO.allReturned: " + e.getMessage()); }
        return false;
    }
}
