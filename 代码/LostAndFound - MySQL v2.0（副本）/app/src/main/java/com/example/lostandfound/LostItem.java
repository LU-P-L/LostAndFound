package com.example.lostandfound;

import androidx.annotation.NonNull;

import java.util.Date;

public class LostItem {
    private String id;
    private String userId;
    private String itemName;
    private String description;
    private String category;
    private String lostLocation;
    private String lostTime;
    private String contactInfo;
    private String pictureUrl;
    private boolean urgent;
    private boolean found;
    private String createdAt;
    private User owner;

    // 无参构造方法
    public LostItem() {
    }

    // 全参构造方法（可选）
    public LostItem(String id,String userId, String itemName, String description, String category, String lostTime,
                    String lostLocation, boolean found, Boolean urgent, String contactInfo, String createdAt, String pictureUrl) {
        this.id = id;
        this.userId = userId;
        this.itemName = itemName;
        this.description = description;
        this.category = category;
        this.lostTime = lostTime;
        this.lostLocation = lostLocation;
        this.found = found;
        this.urgent = urgent;
        this.contactInfo = contactInfo;
        this.createdAt = createdAt;
        this.pictureUrl = pictureUrl;
    }

    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLostTime() {
        return lostTime;
    }

    public void setLostTime(String lostTime) {
        this.lostTime = lostTime;
    }

    public String getLostLocation() {
        return lostLocation;
    }

    public void setLostLocation(String lostLocation) {
        this.lostLocation = lostLocation;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Boolean getUrgent() {
        return urgent;
    }

    public void setUrgent(Boolean urgent) {
        this.urgent = urgent;
    }

    public String getImageUrl() {
        return pictureUrl != null ?
                "http://172.20.10.6:8080" + pictureUrl :
                null;
    }

    public Boolean getFound() {
        return found;
    }

    public void setFound(Boolean found) {
        this.found = found;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getCreatedAt(){
        return createdAt;
    }

    public void setCreatedAt(String createdAt){
        this.createdAt = createdAt;
    }
}