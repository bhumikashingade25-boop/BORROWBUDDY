package com.borrowbuddy.model;

import java.sql.Date;
import java.sql.Timestamp;

/** Represents a borrowing or exchange transaction between two users. */
public class Transaction {
    private int transactionId;
    private String transactionType;   // BORROW | TEMPORARY_EXCHANGE | PERMANENT_EXCHANGE
    private int requesterId;
    private int responderId;
    private int requestedItemId;
    private Integer offeredItemId;    // nullable
    private Date startDate;           // nullable for PERMANENT_EXCHANGE
    private Date dueDate;             // nullable for PERMANENT_EXCHANGE
    private Timestamp requestedAt;
    private Timestamp completedAt;    // nullable
    private String status;            // PENDING|ACCEPTED|REJECTED|CANCELLED|ACTIVE|COMPLETED
    private String requestMessage;

    // Transient display fields
    private String requesterName;
    private String responderName;
    private String requestedItemName;
    private String offeredItemName;

    public Transaction() {}

    // Getters
    public int getTransactionId()       { return transactionId; }
    public String getTransactionType()  { return transactionType; }
    public int getRequesterId()         { return requesterId; }
    public int getResponderId()         { return responderId; }
    public int getRequestedItemId()     { return requestedItemId; }
    public Integer getOfferedItemId()   { return offeredItemId; }
    public Date getStartDate()          { return startDate; }
    public Date getDueDate()            { return dueDate; }
    public Timestamp getRequestedAt()   { return requestedAt; }
    public Timestamp getCompletedAt()   { return completedAt; }
    public String getStatus()           { return status; }
    public String getRequestMessage()   { return requestMessage; }
    public String getRequesterName()    { return requesterName; }
    public String getResponderName()    { return responderName; }
    public String getRequestedItemName(){ return requestedItemName; }
    public String getOfferedItemName()  { return offeredItemName; }

    // Setters
    public void setTransactionId(int id)            { this.transactionId = id; }
    public void setTransactionType(String type)     { this.transactionType = type; }
    public void setRequesterId(int id)              { this.requesterId = id; }
    public void setResponderId(int id)              { this.responderId = id; }
    public void setRequestedItemId(int id)          { this.requestedItemId = id; }
    public void setOfferedItemId(Integer id)        { this.offeredItemId = id; }
    public void setStartDate(Date d)                { this.startDate = d; }
    public void setDueDate(Date d)                  { this.dueDate = d; }
    public void setRequestedAt(Timestamp t)         { this.requestedAt = t; }
    public void setCompletedAt(Timestamp t)         { this.completedAt = t; }
    public void setStatus(String status)            { this.status = status; }
    public void setRequestMessage(String msg)       { this.requestMessage = msg; }
    public void setRequesterName(String n)          { this.requesterName = n; }
    public void setResponderName(String n)          { this.responderName = n; }
    public void setRequestedItemName(String n)      { this.requestedItemName = n; }
    public void setOfferedItemName(String n)        { this.offeredItemName = n; }

    // Convenience
    public boolean isPending()           { return "PENDING".equals(status); }
    public boolean isAccepted()          { return "ACCEPTED".equals(status); }
    public boolean isActive()            { return "ACTIVE".equals(status); }
    public boolean isCompleted()         { return "COMPLETED".equals(status); }
    public boolean isRejected()          { return "REJECTED".equals(status); }
    public boolean isCancelled()         { return "CANCELLED".equals(status); }
    public boolean isBorrow()            { return "BORROW".equals(transactionType); }
    public boolean isTemporaryExchange() { return "TEMPORARY_EXCHANGE".equals(transactionType); }
    public boolean isPermanentExchange() { return "PERMANENT_EXCHANGE".equals(transactionType); }

    @Override
    public String toString() {
        return "Transaction#" + transactionId + " [" + transactionType + "/" + status + "]";
    }
}
