package com.borrowbuddy.model;

import java.sql.Timestamp;

/** Audit trail record for permanent ownership transfers. */
public class ItemOwnershipHistory {
    private int historyId;
    private int itemId;
    private int previousOwnerId;
    private int newOwnerId;
    private int transactionId;
    private Timestamp transferDate;

    // Transient display fields
    private String itemName;
    private String previousOwnerName;
    private String newOwnerName;

    public ItemOwnershipHistory() {}

    public int getHistoryId()           { return historyId; }
    public int getItemId()              { return itemId; }
    public int getPreviousOwnerId()     { return previousOwnerId; }
    public int getNewOwnerId()          { return newOwnerId; }
    public int getTransactionId()       { return transactionId; }
    public Timestamp getTransferDate()  { return transferDate; }
    public String getItemName()         { return itemName; }
    public String getPreviousOwnerName(){ return previousOwnerName; }
    public String getNewOwnerName()     { return newOwnerName; }

    public void setHistoryId(int id)              { this.historyId = id; }
    public void setItemId(int id)                 { this.itemId = id; }
    public void setPreviousOwnerId(int id)        { this.previousOwnerId = id; }
    public void setNewOwnerId(int id)             { this.newOwnerId = id; }
    public void setTransactionId(int id)          { this.transactionId = id; }
    public void setTransferDate(Timestamp t)      { this.transferDate = t; }
    public void setItemName(String name)          { this.itemName = name; }
    public void setPreviousOwnerName(String name) { this.previousOwnerName = name; }
    public void setNewOwnerName(String name)      { this.newOwnerName = name; }
}
