package com.borrowbuddy.model;

/** Represents an item category (e.g. Books, Electronics). */
public class Category {
    private int categoryId;
    private String categoryName;
    private String description;

    public Category() {}

    public Category(int categoryId, String categoryName, String description) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
    }

    public int getCategoryId()       { return categoryId; }
    public String getCategoryName()  { return categoryName; }
    public String getDescription()   { return description; }

    public void setCategoryId(int id)          { this.categoryId = id; }
    public void setCategoryName(String name)   { this.categoryName = name; }
    public void setDescription(String desc)    { this.description = desc; }

    @Override public String toString() { return categoryName; }
}
