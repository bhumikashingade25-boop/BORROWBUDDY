package com.borrowbuddy.model;

import java.sql.Timestamp;

/** Records the physical return of an item during a temporary transaction. */
public class TransactionReturn {
    private int returnId;
    private int transactionId;
    private int itemId;
    private int returnedBy;
    private Integer confirmedBy;   // nullable
    private Timestamp returnedAt;
    private String status;         // PENDING | CONFIRMED

    // Transient display fields
    private String itemName;
    private String returnedByName;

    public TransactionReturn() {}

    public int getReturnId()            { return returnId; }
    public int getTransactionId()       { return transactionId; }
    public int getItemId()              { return itemId; }
    public int getReturnedBy()          { return returnedBy; }
    public Integer getConfirmedBy()     { return confirmedBy; }
    public Timestamp getReturnedAt()    { return returnedAt; }
    public String getStatus()           { return status; }
    public String getItemName()         { return itemName; }
    public String getReturnedByName()   { return returnedByName; }

    public void setReturnId(int id)             { this.returnId = id; }
    public void setTransactionId(int id)        { this.transactionId = id; }
    public void setItemId(int id)               { this.itemId = id; }
    public void setReturnedBy(int id)           { this.returnedBy = id; }
    public void setConfirmedBy(Integer id)      { this.confirmedBy = id; }
    public void setReturnedAt(Timestamp t)      { this.returnedAt = t; }
    public void setStatus(String status)        { this.status = status; }
    public void setItemName(String name)        { this.itemName = name; }
    public void setReturnedByName(String name)  { this.returnedByName = name; }

    public boolean isPending()   { return "PENDING".equalsIgnoreCase(status); }
    public boolean isConfirmed() { return "CONFIRMED".equalsIgnoreCase(status) || "RETURNED".equalsIgnoreCase(status); }
}
