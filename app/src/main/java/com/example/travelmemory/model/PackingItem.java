package com.example.travelmemory.model;

public class PackingItem {
    private int id;
    private int tripId;
    private String category;
    private String itemName;
    private int isSelected;
    private int isPacked;
    private int isCustom;
    private String createdAt;

    public PackingItem() {}

    public PackingItem(int id, int tripId, String category, String itemName, int isChecked, int isCustom, String createdAt) {
        this(id, tripId, category, itemName, 1, isChecked, isCustom, createdAt);
    }

    public PackingItem(int id, int tripId, String category, String itemName, int isSelected, int isPacked, int isCustom, String createdAt) {
        this.id = id;
        this.tripId = tripId;
        this.category = category;
        this.itemName = itemName;
        this.isSelected = isSelected;
        this.isPacked = isPacked;
        this.isCustom = isCustom;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public int getIsSelected() { return isSelected; }
    public void setIsSelected(int isSelected) { this.isSelected = isSelected; }
    public int getIsPacked() { return isPacked; }
    public void setIsPacked(int isPacked) { this.isPacked = isPacked; }
    public int getIsChecked() { return isPacked; }
    public void setIsChecked(int isChecked) { this.isPacked = isChecked; }
    public int getIsCustom() { return isCustom; }
    public void setIsCustom(int isCustom) { this.isCustom = isCustom; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
