package com.example.lostandfound.network;

public class UpdateStatusRequest {
    private String itemId;
    private boolean isFound;

    public UpdateStatusRequest(String itemId, boolean isFound) {
        this.itemId = itemId;
        this.isFound = isFound;
    }

    // Getters and Setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public boolean isFound() {
        return isFound;
    }

    public void setFound(boolean found) {
        isFound = found;
    }
}