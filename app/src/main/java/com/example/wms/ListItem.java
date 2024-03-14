package com.example.wms;

public class ListItem {

    private int itemID;
    private int listID;
    private String itemName;
    private String quantity;
    private String location;
    private String status;
    private String updatedBy;
    private String timestamp;

    // Constructor, getters, and setters as needed
    // ...

    public ListItem(int itemID, int listID, String itemName, String quantity, String location, String status, String updatedBy, String timestamp) {
        this.itemID = itemID;
        this.listID = listID;
        this.itemName = itemName;
        this.quantity = quantity;
        this.location = location;
        this.status = status;
        this.updatedBy = updatedBy;
        this.timestamp = timestamp;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getListID() {
        return listID;
    }

    public void setListID(int listID) {
        this.listID = listID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

