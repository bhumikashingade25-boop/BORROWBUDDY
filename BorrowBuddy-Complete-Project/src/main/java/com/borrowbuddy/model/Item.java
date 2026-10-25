package com.borrowbuddy.model;

import java.sql.Timestamp;

/** Represents a physical item listed on the BorrowBuddy platform. */
public class Item {
    private int itemId;
    private int ownerId;
    private int categoryId;
    private String itemName;
    private String description;
    private String itemCondition;   // NEW | GOOD | FAIR | POOR
    private String imageUrl;
    private String availabilityStatus; // AVAILABLE | RESERVED | UNAVAILABLE
    private boolean allowBorrowing;
    private boolean allowTemporaryExchange;
    private boolean allowPermanentExchange;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Transient display fields (not stored in DB)
    private String ownerName;
    private String categoryName;

    public Item() {}

    public Item(int itemId, int ownerId, int categoryId, String itemName, String description,
                String itemCondition, String imageUrl, String availabilityStatus,
                boolean allowBorrowing, boolean allowTemporaryExchange, boolean allowPermanentExchange,
                Timestamp createdAt, Timestamp updatedAt) {
        this.itemId = itemId;
        this.ownerId = ownerId;
        this.categoryId = categoryId;
        this.itemName = itemName;
        this.description = description;
        this.itemCondition = itemCondition;
        this.imageUrl = imageUrl;
        this.availabilityStatus = availabilityStatus;
        this.allowBorrowing = allowBorrowing;
        this.allowTemporaryExchange = allowTemporaryExchange;
        this.allowPermanentExchange = allowPermanentExchange;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getItemId()                  { return itemId; }
    public int getOwnerId()                 { return ownerId; }
    public int getCategoryId()              { return categoryId; }
    public String getItemName()             { return itemName; }
    public String getDescription()          { return description; }
    public String getItemCondition()        { return itemCondition; }
    public String getImageUrl()             { return imageUrl; }
    public String getAvailabilityStatus()   { return availabilityStatus; }
    public boolean isAllowBorrowing()       { return allowBorrowing; }
    public boolean isAllowTemporaryExchange() { return allowTemporaryExchange; }
    public boolean isAllowPermanentExchange() { return allowPermanentExchange; }
    public Timestamp getCreatedAt()         { return createdAt; }
    public Timestamp getUpdatedAt()         { return updatedAt; }
    public String getOwnerName()            { return ownerName; }
    public String getCategoryName()         { return categoryName; }

    // Setters
    public void setItemId(int itemId)                         { this.itemId = itemId; }
    public void setOwnerId(int ownerId)                       { this.ownerId = ownerId; }
    public void setCategoryId(int categoryId)                 { this.categoryId = categoryId; }
    public void setItemName(String itemName)                  { this.itemName = itemName; }
    public void setDescription(String description)            { this.description = description; }
    public void setItemCondition(String itemCondition)        { this.itemCondition = itemCondition; }
    public void setImageUrl(String imageUrl)                  { this.imageUrl = imageUrl; }
    public void setAvailabilityStatus(String status)          { this.availabilityStatus = status; }
    public void setAllowBorrowing(boolean allow)              { this.allowBorrowing = allow; }
    public void setAllowTemporaryExchange(boolean allow)      { this.allowTemporaryExchange = allow; }
    public void setAllowPermanentExchange(boolean allow)      { this.allowPermanentExchange = allow; }
    public void setCreatedAt(Timestamp createdAt)             { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt)             { this.updatedAt = updatedAt; }
    public void setOwnerName(String ownerName)                { this.ownerName = ownerName; }
    public void setCategoryName(String categoryName)          { this.categoryName = categoryName; }

    /** Convenience: returns true if availability status is AVAILABLE. */
    public boolean isAvailable() { return "AVAILABLE".equals(availabilityStatus); }

    @Override
    public String toString() { return itemName + " [" + itemCondition + "]"; }
}
