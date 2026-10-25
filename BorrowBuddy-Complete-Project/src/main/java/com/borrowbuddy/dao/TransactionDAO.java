package com.borrowbuddy.dao;

import com.borrowbuddy.config.DatabaseConnection;
import com.borrowbuddy.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO for the TRANSACTIONS table. Joins USERS and ITEMS for display names. */
public class TransactionDAO {

    private static final String BASE_SELECT =
        "SELECT t.*, " +
        "  req.name AS requester_name, " +
        "  res.name AS responder_name, " +
        "  ri.item_name AS requested_item_name, " +
        "  oi.item_name AS offered_item_name " +
        "FROM transactions t " +
        "JOIN users req ON t.requester_id = req.user_id " +
        "JOIN users res ON t.responder_id = res.user_id " +
        "JOIN items ri  ON t.requested_item_id = ri.item_id " +
        "LEFT JOIN items oi ON t.offered_item_id = oi.item_id ";

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setTransactionType(rs.getString("transaction_type"));
        t.setRequesterId(rs.getInt("requester_id"));
        t.setResponderId(rs.getInt("responder_id"));
        t.setRequestedItemId(rs.getInt("requested_item_id"));
        int offId = rs.getInt("offered_item_id");
        t.setOfferedItemId(rs.wasNull() ? null : offId);
        t.setStartDate(rs.getDate("start_date"));
        t.setDueDate(rs.getDate("due_date"));
        t.setRequestedAt(rs.getTimestamp("requested_at"));
        t.setCompletedAt(rs.getTimestamp("completed_at"));
        t.setStatus(rs.getString("status"));
        t.setRequestMessage(rs.getString("request_message"));
        t.setRequesterName(rs.getString("requester_name"));
        t.setResponderName(rs.getString("responder_name"));
        t.setRequestedItemName(rs.getString("requested_item_name"));
        t.setOfferedItemName(rs.getString("offered_item_name"));
        return t;
    }

    public Optional<Transaction> findById(int id) {
        String sql = BASE_SELECT + "WHERE t.transaction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("TransactionDAO.findById: " + e.getMessage()); }
        return Optional.empty();
    }

    public List<Transaction> findByRequester(int requesterId) {
        return findWhere("WHERE t.requester_id = ? ORDER BY t.requested_at DESC", requesterId);
    }

    public List<Transaction> findByResponder(int responderId) {
        return findWhere("WHERE t.responder_id = ? ORDER BY t.requested_at DESC", responderId);
    }

    /** All transactions involving the user as either requester or responder. */
    public List<Transaction> findByUser(int userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = BASE_SELECT +
            "WHERE t.requester_id = ? OR t.responder_id = ? " +
            "ORDER BY t.requested_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("TransactionDAO.findByUser: " + e.getMessage()); }
        return list;
    }

    public List<Transaction> findByStatus(String status) {
        return findWhere("WHERE t.status = ? ORDER BY t.requested_at DESC", status);
    }

    public List<Transaction> findActive() {
        return findByStatus("ACTIVE");
    }

    public List<Transaction> findOverdue() {
        List<Transaction> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE t.status = 'ACTIVE' AND t.due_date < CURDATE() ORDER BY t.due_date";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("TransactionDAO.findOverdue: " + e.getMessage()); }
        return list;
    }

    public List<Transaction> findByItemId(int itemId) {
        List<Transaction> list = new ArrayList<>();
        String sql = BASE_SELECT +
            "WHERE t.requested_item_id = ? OR t.offered_item_id = ? ORDER BY t.requested_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("TransactionDAO.findByItemId: " + e.getMessage()); }
        return list;
    }

    public List<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY t.requested_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("TransactionDAO.findAll: " + e.getMessage()); }
        return list;
    }

    public List<Transaction> findRecentActivity(int limit) {
        List<Transaction> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY t.requested_at DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("TransactionDAO.findRecentActivity: " + e.getMessage()); }
        return list;
    }

    private List<Transaction> findWhere(String whereClause, Object param) {
        List<Transaction> list = new ArrayList<>();
        String sql = BASE_SELECT + whereClause;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("TransactionDAO.findWhere: " + e.getMessage()); }
        return list;
    }

    /** Inserts a new transaction and returns the generated ID. */
    public int save(Transaction t) {
        String sql = "INSERT INTO transactions (transaction_type, requester_id, responder_id, requested_item_id, " +
                     "offered_item_id, start_date, due_date, status, request_message) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getTransactionType());
            ps.setInt(2, t.getRequesterId());
            ps.setInt(3, t.getResponderId());
            ps.setInt(4, t.getRequestedItemId());
            if (t.getOfferedItemId() != null) ps.setInt(5, t.getOfferedItemId()); else ps.setNull(5, Types.INTEGER);
            ps.setDate(6, t.getStartDate());
            ps.setDate(7, t.getDueDate());
            ps.setString(8, t.getStatus() != null ? t.getStatus() : "PENDING");
            ps.setString(9, t.getRequestMessage());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) { System.err.println("TransactionDAO.save: " + e.getMessage()); }
        return -1;
    }

    /** Saves a transaction using an existing connection (for atomic operations). */
    public int save(Transaction t, Connection conn) throws SQLException {
        String sql = "INSERT INTO transactions (transaction_type, requester_id, responder_id, requested_item_id, " +
                     "offered_item_id, start_date, due_date, status, request_message) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getTransactionType());
            ps.setInt(2, t.getRequesterId());
            ps.setInt(3, t.getResponderId());
            ps.setInt(4, t.getRequestedItemId());
            if (t.getOfferedItemId() != null) ps.setInt(5, t.getOfferedItemId()); else ps.setNull(5, Types.INTEGER);
            ps.setDate(6, t.getStartDate());
            ps.setDate(7, t.getDueDate());
            ps.setString(8, t.getStatus() != null ? t.getStatus() : "PENDING");
            ps.setString(9, t.getRequestMessage());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateStatus(int transactionId, String status) {
        String sql = "UPDATE transactions SET status=? WHERE transaction_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, transactionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("TransactionDAO.updateStatus: " + e.getMessage()); }
        return false;
    }

    public boolean updateStatus(Connection conn, int transactionId, String status) throws SQLException {
        String sql = "UPDATE transactions SET status=? WHERE transaction_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, transactionId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatusWithCompletion(int transactionId, String status) {
        String sql = "UPDATE transactions SET status=?, completed_at=NOW() WHERE transaction_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, transactionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("TransactionDAO.updateStatusCompletion: " + e.getMessage()); }
        return false;
    }

    public boolean updateStatusWithCompletion(Connection conn, int transactionId, String status) throws SQLException {
        String sql = "UPDATE transactions SET status=?, completed_at=NOW() WHERE transaction_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, transactionId);
            return ps.executeUpdate() > 0;
        }
    }

    public int countPending() {
        return countByStatus("PENDING");
    }

    public int countActive() {
        return countByStatus("ACTIVE");
    }

    public int countCompleted() {
        return countByStatus("COMPLETED");
    }

    public int countByUserAndStatus(int userId, String status) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE (requester_id = ? OR responder_id = ?) AND status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setString(3, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { System.err.println("TransactionDAO.countByUserAndStatus: " + e.getMessage()); }
        return 0;
    }

    public int countPendingByUser(int userId) {
        return countByUserAndStatus(userId, "PENDING");
    }

    public int countActiveByUser(int userId) {
        return countByUserAndStatus(userId, "ACTIVE");
    }

    public int countCompletedByUser(int userId) {
        return countByUserAndStatus(userId, "COMPLETED");
    }

    public int countOverdueByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE (requester_id = ? OR responder_id = ?) AND status='ACTIVE' AND due_date < CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { System.err.println("TransactionDAO.countOverdueByUser: " + e.getMessage()); }
        return 0;
    }

    public int countActiveTempExchangesByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE (requester_id = ? OR responder_id = ?) AND status='ACTIVE' AND transaction_type='TEMPORARY_EXCHANGE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { System.err.println("TransactionDAO.countActiveTempExchangesByUser: " + e.getMessage()); }
        return 0;
    }

    public int countPermExchangesByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE (requester_id = ? OR responder_id = ?) AND transaction_type='PERMANENT_EXCHANGE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { System.err.println("TransactionDAO.countPermExchangesByUser: " + e.getMessage()); }
        return 0;
    }

    public int countOverdue() {
        String sql = "SELECT COUNT(*) FROM transactions WHERE status='ACTIVE' AND due_date < CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("TransactionDAO.countOverdue: " + e.getMessage()); }
        return 0;
    }

    public int countActiveTemporaryExchanges() {
        String sql = "SELECT COUNT(*) FROM transactions WHERE status='ACTIVE' AND transaction_type='TEMPORARY_EXCHANGE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("TransactionDAO.countTempExchanges: " + e.getMessage()); }
        return 0;
    }

    public int countPendingPermanentExchanges() {
        String sql = "SELECT COUNT(*) FROM transactions WHERE status IN ('PENDING','ACCEPTED') AND transaction_type='PERMANENT_EXCHANGE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("TransactionDAO.countPermExchanges: " + e.getMessage()); }
        return 0;
    }

    private int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE status=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { System.err.println("TransactionDAO.countByStatus: " + e.getMessage()); }
        return 0;
    }

    public boolean hasActiveTransactionForItem(int itemId) {
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
        } catch (SQLException e) { System.err.println("TransactionDAO.hasActive: " + e.getMessage()); }
        return false;
    }
}
