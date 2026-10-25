package com.borrowbuddy.model;

import java.sql.Timestamp;

/** In-app notification for a user about a transaction event. */
public class Notification {
    private int notificationId;
    private int userId;
    private Integer transactionId;   // nullable
    private String message;
    private String type;
    private boolean isRead;
    private Timestamp createdAt;

    public Notification() {}

    public int getNotificationId()      { return notificationId; }
    public int getUserId()              { return userId; }
    public Integer getTransactionId()   { return transactionId; }
    public String getMessage()          { return message; }
    public String getType()             { return type; }
    public boolean isRead()             { return isRead; }
    public Timestamp getCreatedAt()     { return createdAt; }

    public void setNotificationId(int id)       { this.notificationId = id; }
    public void setUserId(int id)               { this.userId = id; }
    public void setTransactionId(Integer id)    { this.transactionId = id; }
    public void setMessage(String message)      { this.message = message; }
    public void setType(String type)            { this.type = type; }
    public void setRead(boolean read)           { this.isRead = read; }
    public void setCreatedAt(Timestamp t)       { this.createdAt = t; }
}
