package com.borrowbuddy.dao;

import com.borrowbuddy.config.DatabaseConnection;
import com.borrowbuddy.model.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO for the REVIEWS table. */
public class ReviewDAO {

    private static final String BASE_SELECT =
        "SELECT r.*, u.name AS user_name, i.item_name FROM reviews r " +
        "JOIN users u ON r.user_id = u.user_id " +
        "JOIN items i ON r.item_id = i.item_id ";

    private Review mapRow(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setReviewId(rs.getInt("review_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setItemId(rs.getInt("item_id"));
        r.setTransactionId(rs.getInt("transaction_id"));
        r.setRating(rs.getInt("rating"));
        r.setComments(rs.getString("comments"));
        r.setReviewDate(rs.getTimestamp("review_date"));
        try {
            r.setUserName(rs.getString("user_name"));
            r.setItemName(rs.getString("item_name"));
        } catch (SQLException ignored) {}
        return r;
    }

    public Optional<Review> findById(int id) {
        String sql = BASE_SELECT + "WHERE r.review_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("ReviewDAO.findById: " + e.getMessage()); }
        return Optional.empty();
    }

    public List<Review> findByItem(int itemId) {
        List<Review> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE r.item_id = ? ORDER BY r.review_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("ReviewDAO.findByItem: " + e.getMessage()); }
        return list;
    }

    public List<Review> findByUser(int userId) {
        List<Review> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE r.user_id = ? ORDER BY r.review_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("ReviewDAO.findByUser: " + e.getMessage()); }
        return list;
    }

    /** Reviews for items owned by the given user. */
    public List<Review> findForOwnerItems(int ownerId) {
        List<Review> list = new ArrayList<>();
        String sql = BASE_SELECT + "JOIN items owned ON r.item_id = owned.item_id " +
                     "WHERE owned.owner_id = ? ORDER BY r.review_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("ReviewDAO.findForOwnerItems: " + e.getMessage()); }
        return list;
    }

    public Optional<Review> findByTransaction(int transactionId) {
        String sql = BASE_SELECT + "WHERE r.transaction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("ReviewDAO.findByTransaction: " + e.getMessage()); }
        return Optional.empty();
    }

    public int save(Review review) {
        String sql = "INSERT INTO reviews (user_id, item_id, transaction_id, rating, comments) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, review.getUserId());
            ps.setInt(2, review.getItemId());
            ps.setInt(3, review.getTransactionId());
            ps.setInt(4, review.getRating());
            ps.setString(5, review.getComments());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) { System.err.println("ReviewDAO.save: " + e.getMessage()); }
        return -1;
    }

    public boolean hasReview(int transactionId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE transaction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { System.err.println("ReviewDAO.hasReview: " + e.getMessage()); }
        return false;
    }

    public double getAverageRatingForItem(int itemId) {
        String sql = "SELECT AVG(rating) FROM reviews WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) { System.err.println("ReviewDAO.getAvgRating: " + e.getMessage()); }
        return 0.0;
    }
}
