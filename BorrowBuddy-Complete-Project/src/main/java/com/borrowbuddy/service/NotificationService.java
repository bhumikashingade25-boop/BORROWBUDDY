package com.borrowbuddy.service;

import com.borrowbuddy.dao.NotificationDAO;
import com.borrowbuddy.model.Item;
import com.borrowbuddy.model.Notification;
import com.borrowbuddy.model.Transaction;

import java.util.List;

/**
 * Service for creating and managing in-app user notifications.
 */
public class NotificationService {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    public void createNotification(int userId, Integer transactionId, String message, String type) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTransactionId(transactionId);
        n.setMessage(message);
        n.setType(type);
        n.setRead(false);
        notificationDAO.save(n);
    }

    public void notifyRequestSubmitted(Transaction t, Item requestedItem) {
        String msg = "New " + t.getTransactionType().replace("_", " ") + " request received for your item: " + requestedItem.getItemName();
        createNotification(t.getResponderId(), t.getTransactionId(), msg, "REQUEST_SUBMITTED");
    }

    public void notifyRequestAccepted(Transaction t) {
        String msg = "Your " + t.getTransactionType().replace("_", " ") + " request #" + t.getTransactionId() + " has been accepted!";
        createNotification(t.getRequesterId(), t.getTransactionId(), msg, "REQUEST_ACCEPTED");
    }

    public void notifyRequestRejected(Transaction t) {
        String msg = "Your " + t.getTransactionType().replace("_", " ") + " request #" + t.getTransactionId() + " was declined.";
        createNotification(t.getRequesterId(), t.getTransactionId(), msg, "REQUEST_REJECTED");
    }

    public void notifyTransactionActivated(Transaction t) {
        String msg = "Transaction #" + t.getTransactionId() + " (" + t.getTransactionType().replace("_", " ") + ") is now active.";
        createNotification(t.getRequesterId(), t.getTransactionId(), msg, "ACTIVATED");
        createNotification(t.getResponderId(), t.getTransactionId(), msg, "ACTIVATED");
    }

    public void notifyReturnRecorded(Transaction t, Item item, int ownerId) {
        String msg = "Return marked for '" + item.getItemName() + "' in Transaction #" + t.getTransactionId() + ". Please inspect and confirm receipt.";
        createNotification(ownerId, t.getTransactionId(), msg, "RETURN_RECORDED");
    }

    public void notifyReturnConfirmed(Transaction t, Item item) {
        String msg = "Return confirmed for '" + item.getItemName() + "' in Transaction #" + t.getTransactionId() + ".";
        createNotification(t.getRequesterId(), t.getTransactionId(), msg, "RETURN_CONFIRMED");
        createNotification(t.getResponderId(), t.getTransactionId(), msg, "RETURN_CONFIRMED");
    }

    public void notifyOwnershipTransferred(Transaction t, int userId, String itemName) {
        String msg = "Permanent Exchange complete! You are now the official owner of '" + itemName + "' (Transaction #" + t.getTransactionId() + ").";
        createNotification(userId, t.getTransactionId(), msg, "OWNERSHIP_TRANSFERRED");
    }

    public List<Notification> getNotifications(int userId) {
        return notificationDAO.findByUser(userId);
    }

    public List<Notification> getUnreadNotifications(int userId) {
        return notificationDAO.findUnreadByUser(userId);
    }

    public boolean markAsRead(int notificationId) {
        return notificationDAO.markAsRead(notificationId);
    }

    public boolean markAllAsRead(int userId) {
        return notificationDAO.markAllAsRead(userId);
    }

    public int getUnreadCount(int userId) {
        return notificationDAO.countUnread(userId);
    }
}
