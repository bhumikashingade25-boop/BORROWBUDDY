package com.borrowbuddy.dao;

import com.borrowbuddy.config.DatabaseConnection;
import com.borrowbuddy.model.TransactionApproval;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO for the TRANSACTION_APPROVALS table. */
public class ApprovalDAO {

    private TransactionApproval mapRow(ResultSet rs) throws SQLException {
        TransactionApproval a = new TransactionApproval();
        a.setApprovalId(rs.getInt("approval_id"));
        a.setTransactionId(rs.getInt("transaction_id"));
        a.setUserId(rs.getInt("user_id"));
        a.setDecision(rs.getString("decision"));
        a.setDecisionDate(rs.getTimestamp("decision_date"));
        try { a.setUserName(rs.getString("name")); } catch (SQLException ignored) {}
        return a;
    }

    public Optional<TransactionApproval> findById(int id) {
        String sql = "SELECT ta.*, u.name FROM transaction_approvals ta " +
                     "JOIN users u ON ta.user_id = u.user_id WHERE ta.approval_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("ApprovalDAO.findById: " + e.getMessage()); }
        return Optional.empty();
    }

    public List<TransactionApproval> findByTransaction(int transactionId) {
        List<TransactionApproval> list = new ArrayList<>();
        String sql = "SELECT ta.*, u.name FROM transaction_approvals ta " +
                     "JOIN users u ON ta.user_id = u.user_id WHERE ta.transaction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("ApprovalDAO.findByTransaction: " + e.getMessage()); }
        return list;
    }

    public Optional<TransactionApproval> findByTransactionAndUser(int transactionId, int userId) {
        String sql = "SELECT ta.*, u.name FROM transaction_approvals ta " +
                     "JOIN users u ON ta.user_id = u.user_id " +
                     "WHERE ta.transaction_id = ? AND ta.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("ApprovalDAO.findByTxnAndUser: " + e.getMessage()); }
        return Optional.empty();
    }

    public int save(TransactionApproval approval) {
        String sql = "INSERT INTO transaction_approvals (transaction_id, user_id, decision) VALUES (?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, approval.getTransactionId());
            ps.setInt(2, approval.getUserId());
            ps.setString(3, approval.getDecision() != null ? approval.getDecision() : "PENDING");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) { System.err.println("ApprovalDAO.save: " + e.getMessage()); }
        return -1;
    }

    /** Save using an existing connection for atomicity. */
    public int save(TransactionApproval approval, Connection conn) throws SQLException {
        String sql = "INSERT INTO transaction_approvals (transaction_id, user_id, decision) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, approval.getTransactionId());
            ps.setInt(2, approval.getUserId());
            ps.setString(3, approval.getDecision() != null ? approval.getDecision() : "PENDING");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateDecision(int approvalId, String decision) {
        String sql = "UPDATE transaction_approvals SET decision=?, decision_date=NOW() WHERE approval_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, decision);
            ps.setInt(2, approvalId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("ApprovalDAO.updateDecision: " + e.getMessage()); }
        return false;
    }

    public boolean updateDecision(Connection conn, int approvalId, String decision) throws SQLException {
        String sql = "UPDATE transaction_approvals SET decision=?, decision_date=NOW() WHERE approval_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, decision);
            ps.setInt(2, approvalId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Returns true if all approval records for a transaction are ACCEPTED. */
    public boolean allAccepted(int transactionId) {
        String sql = "SELECT COUNT(*) FROM transaction_approvals " +
                     "WHERE transaction_id = ? AND decision != 'ACCEPTED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) == 0;
            }
        } catch (SQLException e) { System.err.println("ApprovalDAO.allAccepted: " + e.getMessage()); }
        return false;
    }

    public boolean allAccepted(Connection conn, int transactionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM transaction_approvals " +
                     "WHERE transaction_id = ? AND decision != 'ACCEPTED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) == 0;
            }
        }
        return false;
    }
}
