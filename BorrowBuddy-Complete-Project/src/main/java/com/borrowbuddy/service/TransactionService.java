package com.borrowbuddy.service;

import com.borrowbuddy.config.DatabaseConnection;
import com.borrowbuddy.dao.*;
import com.borrowbuddy.model.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Core transaction service for BorrowBuddy.
 * Manages Borrowing, Temporary Exchange, and Permanent Exchange workflows
 * with strict database transactions, locking, and atomic ownership transfers.
 */
public class TransactionService {

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ItemDAO itemDAO = new ItemDAO();
    private final ApprovalDAO approvalDAO = new ApprovalDAO();
    private final ReturnDAO returnDAO = new ReturnDAO();
    private final OwnershipHistoryDAO historyDAO = new OwnershipHistoryDAO();
    private final NotificationService notificationService = new NotificationService();

    // ==========================================
    // 1. REQUEST CREATION
    // ==========================================

    /**
     * Submits a temporary borrow request for an item.
     */
    public Transaction submitBorrowRequest(int requesterId, int requestedItemId,
                                          Date startDate, Date dueDate, String message) throws Exception {
        Item item = itemDAO.findById(requestedItemId)
                .orElseThrow(() -> new IllegalArgumentException("Requested item not found."));

        if (item.getOwnerId() == requesterId) {
            throw new IllegalArgumentException("You cannot borrow your own item.");
        }
        if (!item.isAllowBorrowing()) {
            throw new IllegalStateException("This item is not configured to allow borrowing.");
        }
        if (!item.isAvailable()) {
            throw new IllegalStateException("The item is currently " + item.getAvailabilityStatus() + " and cannot be requested.");
        }
        if (startDate == null || dueDate == null) {
            throw new IllegalArgumentException("Start date and due date are required for borrowing.");
        }
        if (dueDate.before(startDate)) {
            throw new IllegalArgumentException("Due date cannot be before the start date.");
        }
        if (transactionDAO.hasActiveTransactionForItem(requestedItemId)) {
            throw new IllegalStateException("Item currently has an active or pending transaction request.");
        }

        Transaction t = new Transaction();
        t.setTransactionType("BORROW");
        t.setRequesterId(requesterId);
        t.setResponderId(item.getOwnerId());
        t.setRequestedItemId(requestedItemId);
        t.setOfferedItemId(null);
        t.setStartDate(startDate);
        t.setDueDate(dueDate);
        t.setStatus("PENDING");
        t.setRequestMessage(message);

        int txnId = transactionDAO.save(t);
        if (txnId <= 0) {
            throw new Exception("Failed to persist transaction request in database.");
        }
        t.setTransactionId(txnId);

        // Create approval requirement for responder
        TransactionApproval approval = new TransactionApproval();
        approval.setTransactionId(txnId);
        approval.setUserId(item.getOwnerId());
        approval.setDecision("PENDING");
        approvalDAO.save(approval);

        notificationService.notifyRequestSubmitted(t, item);
        return t;
    }

    /**
     * Submits a temporary exchange request (both items reserved, returned independently).
     */
    public Transaction submitTemporaryExchangeRequest(int requesterId, int requestedItemId,
                                                     int offeredItemId, Date startDate, Date dueDate,
                                                     String message) throws Exception {
        if (requestedItemId == offeredItemId) {
            throw new IllegalArgumentException("Cannot exchange an item with itself.");
        }
        Item reqItem = itemDAO.findById(requestedItemId)
                .orElseThrow(() -> new IllegalArgumentException("Requested item not found."));
        Item offItem = itemDAO.findById(offeredItemId)
                .orElseThrow(() -> new IllegalArgumentException("Offered item not found."));

        if (reqItem.getOwnerId() == requesterId) {
            throw new IllegalArgumentException("You cannot request an exchange on your own item.");
        }
        if (offItem.getOwnerId() != requesterId) {
            throw new IllegalArgumentException("You can only offer items that you currently own.");
        }
        if (!reqItem.isAllowTemporaryExchange()) {
            throw new IllegalStateException("The requested item does not permit temporary exchange.");
        }
        if (!offItem.isAllowTemporaryExchange()) {
            throw new IllegalStateException("The offered item does not permit temporary exchange.");
        }
        if (!reqItem.isAvailable() || !offItem.isAvailable()) {
            throw new IllegalStateException("Both items must be currently AVAILABLE to initiate an exchange.");
        }
        if (startDate == null || dueDate == null || dueDate.before(startDate)) {
            throw new IllegalArgumentException("Valid start and due dates are required.");
        }
        if (transactionDAO.hasActiveTransactionForItem(requestedItemId) ||
            transactionDAO.hasActiveTransactionForItem(offeredItemId)) {
            throw new IllegalStateException("One or both items already have pending/active transactions.");
        }

        Transaction t = new Transaction();
        t.setTransactionType("TEMPORARY_EXCHANGE");
        t.setRequesterId(requesterId);
        t.setResponderId(reqItem.getOwnerId());
        t.setRequestedItemId(requestedItemId);
        t.setOfferedItemId(offeredItemId);
        t.setStartDate(startDate);
        t.setDueDate(dueDate);
        t.setStatus("PENDING");
        t.setRequestMessage(message);

        int txnId = transactionDAO.save(t);
        if (txnId <= 0) {
            throw new Exception("Failed to record temporary exchange transaction.");
        }
        t.setTransactionId(txnId);

        // Requester pre-approves, responder must approve
        TransactionApproval reqApproval = new TransactionApproval();
        reqApproval.setTransactionId(txnId);
        reqApproval.setUserId(requesterId);
        reqApproval.setDecision("ACCEPTED");
        approvalDAO.save(reqApproval);

        TransactionApproval resApproval = new TransactionApproval();
        resApproval.setTransactionId(txnId);
        resApproval.setUserId(reqItem.getOwnerId());
        resApproval.setDecision("PENDING");
        approvalDAO.save(resApproval);

        notificationService.notifyRequestSubmitted(t, reqItem);
        return t;
    }

    /**
     * Submits a permanent exchange request (ownership transfer upon mutual consent).
     */
    public Transaction submitPermanentExchangeRequest(int requesterId, int requestedItemId,
                                                     int offeredItemId, String message) throws Exception {
        if (requestedItemId == offeredItemId) {
            throw new IllegalArgumentException("Cannot exchange an item with itself.");
        }
        Item reqItem = itemDAO.findById(requestedItemId)
                .orElseThrow(() -> new IllegalArgumentException("Requested item not found."));
        Item offItem = itemDAO.findById(offeredItemId)
                .orElseThrow(() -> new IllegalArgumentException("Offered item not found."));

        if (reqItem.getOwnerId() == requesterId) {
            throw new IllegalArgumentException("You already own the requested item.");
        }
        if (offItem.getOwnerId() != requesterId) {
            throw new IllegalArgumentException("You can only offer items you own.");
        }
        if (!reqItem.isAllowPermanentExchange()) {
            throw new IllegalStateException("The requested item does not permit permanent exchange.");
        }
        if (!offItem.isAllowPermanentExchange()) {
            throw new IllegalStateException("The offered item does not permit permanent exchange.");
        }
        if (!reqItem.isAvailable() || !offItem.isAvailable()) {
            throw new IllegalStateException("Both items must be AVAILABLE for permanent exchange.");
        }
        if (transactionDAO.hasActiveTransactionForItem(requestedItemId) ||
            transactionDAO.hasActiveTransactionForItem(offeredItemId)) {
            throw new IllegalStateException("One or both items already have pending/active transactions.");
        }

        Transaction t = new Transaction();
        t.setTransactionType("PERMANENT_EXCHANGE");
        t.setRequesterId(requesterId);
        t.setResponderId(reqItem.getOwnerId());
        t.setRequestedItemId(requestedItemId);
        t.setOfferedItemId(offeredItemId);
        t.setStartDate(null); // Permanent exchange has no return dates
        t.setDueDate(null);
        t.setStatus("PENDING");
        t.setRequestMessage(message);

        int txnId = transactionDAO.save(t);
        if (txnId <= 0) {
            throw new Exception("Failed to record permanent exchange transaction.");
        }
        t.setTransactionId(txnId);

        TransactionApproval reqApproval = new TransactionApproval();
        reqApproval.setTransactionId(txnId);
        reqApproval.setUserId(requesterId);
        reqApproval.setDecision("ACCEPTED");
        approvalDAO.save(reqApproval);

        TransactionApproval resApproval = new TransactionApproval();
        resApproval.setTransactionId(txnId);
        resApproval.setUserId(reqItem.getOwnerId());
        resApproval.setDecision("PENDING");
        approvalDAO.save(resApproval);

        notificationService.notifyRequestSubmitted(t, reqItem);
        return t;
    }

    // ==========================================
    // 2. APPROVAL, REJECTION & CANCELLATION
    // ==========================================

    /**
     * Approves a transaction. When all parties have approved, triggers activation
     * or immediate atomic ownership transfer for permanent exchanges.
     */
    public void approveTransaction(int transactionId, int userId) throws Exception {
        Transaction t = transactionDAO.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));

        if (!"PENDING".equalsIgnoreCase(t.getStatus())) {
            throw new IllegalStateException("Transaction is not in PENDING status.");
        }

        Optional<TransactionApproval> approvalOpt = approvalDAO.findByTransactionAndUser(transactionId, userId);
        if (approvalOpt.isEmpty()) {
            throw new SecurityException("User has no pending approval record for this transaction.");
        }

        approvalDAO.updateDecision(approvalOpt.get().getApprovalId(), "ACCEPTED");

        if (approvalDAO.allAccepted(transactionId)) {
            if ("BORROW".equalsIgnoreCase(t.getTransactionType())) {
                activateBorrow(t);
            } else if ("TEMPORARY_EXCHANGE".equalsIgnoreCase(t.getTransactionType())) {
                activateTemporaryExchange(t);
            } else if ("PERMANENT_EXCHANGE".equalsIgnoreCase(t.getTransactionType())) {
                executePermanentExchange(t);
            }
        }
    }

    private void activateBorrow(Transaction t) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transactionDAO.updateStatus(conn, t.getTransactionId(), "ACTIVE");
                itemDAO.updateAvailabilityStatus(t.getRequestedItemId(), "BORROWED");
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        notificationService.notifyRequestAccepted(t);
        notificationService.notifyTransactionActivated(t);
    }

    private void activateTemporaryExchange(Transaction t) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transactionDAO.updateStatus(conn, t.getTransactionId(), "ACTIVE");
                itemDAO.updateAvailabilityStatus(t.getRequestedItemId(), "BORROWED");
                if (t.getOfferedItemId() != null) {
                    itemDAO.updateAvailabilityStatus(t.getOfferedItemId(), "BORROWED");
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        notificationService.notifyRequestAccepted(t);
        notificationService.notifyTransactionActivated(t);
    }

    /**
     * Executes atomic permanent exchange: swaps owners in MySQL and writes history audit logs.
     */
    private void executePermanentExchange(Transaction t) throws Exception {
        Item item1 = itemDAO.findById(t.getRequestedItemId())
                .orElseThrow(() -> new IllegalStateException("Requested item missing."));
        Item item2 = itemDAO.findById(t.getOfferedItemId())
                .orElseThrow(() -> new IllegalStateException("Offered item missing."));

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Re-verify ownership
                if (item1.getOwnerId() != t.getResponderId() || item2.getOwnerId() != t.getRequesterId()) {
                    throw new IllegalStateException("Item ownership changed prior to permanent exchange execution!");
                }

                // Swap owners
                itemDAO.updateOwner(conn, item1.getItemId(), t.getRequesterId());
                itemDAO.updateOwner(conn, item2.getItemId(), t.getResponderId());

                // Complete transaction
                transactionDAO.updateStatusWithCompletion(conn, t.getTransactionId(), "COMPLETED");

                // Audit history
                ItemOwnershipHistory h1 = new ItemOwnershipHistory();
                h1.setItemId(item1.getItemId());
                h1.setPreviousOwnerId(t.getResponderId());
                h1.setNewOwnerId(t.getRequesterId());
                h1.setTransactionId(t.getTransactionId());
                historyDAO.save(h1, conn);

                ItemOwnershipHistory h2 = new ItemOwnershipHistory();
                h2.setItemId(item2.getItemId());
                h2.setPreviousOwnerId(t.getRequesterId());
                h2.setNewOwnerId(t.getResponderId());
                h2.setTransactionId(t.getTransactionId());
                historyDAO.save(h2, conn);

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        notificationService.notifyOwnershipTransferred(t, t.getRequesterId(), item1.getItemName());
        notificationService.notifyOwnershipTransferred(t, t.getResponderId(), item2.getItemName());
    }

    public void rejectTransaction(int transactionId, int userId) throws Exception {
        Transaction t = transactionDAO.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));

        Optional<TransactionApproval> approvalOpt = approvalDAO.findByTransactionAndUser(transactionId, userId);
        if (approvalOpt.isPresent()) {
            approvalDAO.updateDecision(approvalOpt.get().getApprovalId(), "REJECTED");
        }

        transactionDAO.updateStatus(transactionId, "REJECTED");
        notificationService.notifyRequestRejected(t);
    }

    public void cancelTransaction(int transactionId, int userId) throws Exception {
        Transaction t = transactionDAO.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));

        if (!"PENDING".equalsIgnoreCase(t.getStatus())) {
            throw new IllegalStateException("Only PENDING requests can be cancelled.");
        }
        if (t.getRequesterId() != userId) {
            throw new SecurityException("Only the requester may cancel this request.");
        }

        transactionDAO.updateStatus(transactionId, "CANCELLED");
    }

    // ==========================================
    // 3. RETURNS AND COMPLETION
    // ==========================================

    /**
     * Marks an item as physically returned (by borrower/exchange partner).
     */
    public void recordReturn(int transactionId, int itemId, int returnedBy) throws Exception {
        Transaction t = transactionDAO.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));

        if (!"ACTIVE".equalsIgnoreCase(t.getStatus())) {
            throw new IllegalStateException("Returns can only be recorded for ACTIVE transactions.");
        }

        if (returnDAO.findByTransactionAndItem(transactionId, itemId).isPresent()) {
            throw new IllegalStateException("Return has already been recorded for this item.");
        }

        TransactionReturn ret = new TransactionReturn();
        ret.setTransactionId(transactionId);
        ret.setItemId(itemId);
        ret.setReturnedBy(returnedBy);
        returnDAO.save(ret);

        Item item = itemDAO.findById(itemId).orElse(null);
        if (item != null) {
            // Notify the owner of this item
            int ownerId = (item.getOwnerId() == returnedBy) ?
                    (t.getRequesterId() == returnedBy ? t.getResponderId() : t.getRequesterId()) : item.getOwnerId();
            notificationService.notifyReturnRecorded(t, item, ownerId);
        }
    }

    /**
     * Confirms receipt of the returned item by its owner. Restores availability and completes transaction when ready.
     */
    public void confirmReturn(int returnId, int confirmedBy) throws Exception {
        TransactionReturn ret = returnDAO.findById(returnId)
                .orElseThrow(() -> new IllegalArgumentException("Return record not found."));

        Item item = itemDAO.findById(ret.getItemId())
                .orElseThrow(() -> new IllegalStateException("Item not found."));

        Transaction t = transactionDAO.findById(ret.getTransactionId())
                .orElseThrow(() -> new IllegalStateException("Transaction not found."));

        // Validate that confirmedBy is authorized (owner of the item)
        if (item.getOwnerId() != confirmedBy) {
            throw new SecurityException("Only the owner of the item can confirm its return.");
        }

        returnDAO.confirm(returnId, confirmedBy);
        itemDAO.updateAvailabilityStatus(item.getItemId(), "AVAILABLE");
        notificationService.notifyReturnConfirmed(t, item);

        // Check completion condition
        if ("BORROW".equalsIgnoreCase(t.getTransactionType())) {
            transactionDAO.updateStatusWithCompletion(t.getTransactionId(), "COMPLETED");
        } else if ("TEMPORARY_EXCHANGE".equalsIgnoreCase(t.getTransactionType())) {
            // Need both items to have confirmed return records
            if (returnDAO.allReturned(t.getTransactionId())) {
                List<TransactionReturn> returns = returnDAO.findByTransaction(t.getTransactionId());
                if (returns.size() >= 2) {
                    transactionDAO.updateStatusWithCompletion(t.getTransactionId(), "COMPLETED");
                }
            }
        }
    }

    // ==========================================
    // 4. QUERIES
    // ==========================================

    public List<Transaction> getIncomingRequests(int userId) {
        return transactionDAO.findByResponder(userId).stream()
                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus()))
                .toList();
    }

    public List<Transaction> getOutgoingRequests(int userId) {
        return transactionDAO.findByRequester(userId).stream()
                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus()))
                .toList();
    }

    public List<Transaction> getActiveTransactions(int userId) {
        return transactionDAO.findByUser(userId).stream()
                .filter(t -> "ACTIVE".equalsIgnoreCase(t.getStatus()))
                .toList();
    }

    public List<Transaction> getTransactionHistory(int userId) {
        return transactionDAO.findByUser(userId).stream()
                .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()) ||
                             "REJECTED".equalsIgnoreCase(t.getStatus()) ||
                             "CANCELLED".equalsIgnoreCase(t.getStatus()))
                .toList();
    }

    public List<Transaction> getAllTransactions() {
        return transactionDAO.findAll();
    }

    public List<Transaction> getOverdueTransactions() {
        return transactionDAO.findOverdue();
    }

    public List<TransactionReturn> getReturnsForTransaction(int transactionId) {
        return returnDAO.findByTransaction(transactionId);
    }

    public Optional<Transaction> getTransactionById(int transactionId) {
        return transactionDAO.findById(transactionId);
    }
}
