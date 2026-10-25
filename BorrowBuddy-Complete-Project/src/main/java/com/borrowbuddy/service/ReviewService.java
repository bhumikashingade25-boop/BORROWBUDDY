package com.borrowbuddy.service;

import com.borrowbuddy.dao.ReviewDAO;
import com.borrowbuddy.dao.TransactionDAO;
import com.borrowbuddy.model.Review;
import com.borrowbuddy.model.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing post-transaction user reviews and ratings.
 * Ensures ratings are within 1-5 and prevents duplicate reviews.
 */
public class ReviewService {

    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public Review submitReview(int userId, int transactionId, int rating, String comments) throws Exception {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars.");
        }
        if (comments == null || comments.trim().isEmpty()) {
            throw new IllegalArgumentException("Review comment cannot be empty.");
        }

        Transaction t = transactionDAO.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));

        if (!"COMPLETED".equalsIgnoreCase(t.getStatus())) {
            throw new IllegalStateException("Reviews can only be submitted for COMPLETED transactions.");
        }

        if (t.getRequesterId() != userId && t.getResponderId() != userId) {
            throw new SecurityException("Only participants in this transaction may submit a review.");
        }

        if (reviewDAO.hasReview(transactionId)) {
            throw new IllegalStateException("A review has already been submitted for this transaction.");
        }

        // Target item is the requested item
        int targetItemId = t.getRequestedItemId();

        Review r = new Review();
        r.setUserId(userId);
        r.setItemId(targetItemId);
        r.setTransactionId(transactionId);
        r.setRating(rating);
        r.setComments(comments.trim());

        int revId = reviewDAO.save(r);
        if (revId <= 0) {
            throw new Exception("Failed to record review in database.");
        }
        r.setReviewId(revId);
        return r;
    }

    public List<Review> getReviewsForItem(int itemId) {
        return reviewDAO.findByItem(itemId);
    }

    public List<Review> getReviewsByUser(int userId) {
        return reviewDAO.findByUser(userId);
    }

    public List<Review> getReviewsForOwner(int ownerId) {
        return reviewDAO.findForOwnerItems(ownerId);
    }

    public boolean hasReview(int transactionId) {
        return reviewDAO.hasReview(transactionId);
    }

    public double getAverageRating(int itemId) {
        return reviewDAO.getAverageRatingForItem(itemId);
    }
}
