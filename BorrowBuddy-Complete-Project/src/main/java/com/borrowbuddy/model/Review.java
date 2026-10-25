package com.borrowbuddy.model;

import java.sql.Timestamp;

/** User review of an item after a completed transaction. */
public class Review {
    private int reviewId;
    private int userId;
    private int itemId;
    private int transactionId;
    private int rating;          // 1–5
    private String comments;
    private Timestamp reviewDate;

    // Transient display fields
    private String userName;
    private String itemName;

    public Review() {}

    public int getReviewId()        { return reviewId; }
    public int getUserId()          { return userId; }
    public int getItemId()          { return itemId; }
    public int getTransactionId()   { return transactionId; }
    public int getRating()          { return rating; }
    public String getComments()     { return comments; }
    public Timestamp getReviewDate(){ return reviewDate; }
    public String getUserName()     { return userName; }
    public String getItemName()     { return itemName; }

    public void setReviewId(int id)         { this.reviewId = id; }
    public void setUserId(int id)           { this.userId = id; }
    public void setItemId(int id)           { this.itemId = id; }
    public void setTransactionId(int id)    { this.transactionId = id; }
    public void setRating(int rating)       { this.rating = rating; }
    public void setComments(String c)       { this.comments = c; }
    public void setReviewDate(Timestamp t)  { this.reviewDate = t; }
    public void setUserName(String name)    { this.userName = name; }
    public void setItemName(String name)    { this.itemName = name; }
}
