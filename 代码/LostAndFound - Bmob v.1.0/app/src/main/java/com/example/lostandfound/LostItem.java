package com.example.lostandfound;

import androidx.annotation.NonNull;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class LostItem extends BmobObject {
    private String itemName;
    private String category;
    private String lostTime;
    private String lostLocation;
    private User owner;
    private String contact;
    private Boolean isUrgent;
    private BmobFile itemImage;
    private String description;
    private Boolean isFound;

    // 无参构造方法（Bmob需要）
    public LostItem() {
    }

    // 全参构造方法（可选）
    public LostItem(String itemName, String category, String lostTime, String lostLocation,
                    User owner, Boolean urgency, String description, String contact) {
        this.itemName = itemName;
        this.category = category;
        this.lostTime = lostTime;
        this.lostLocation = lostLocation;
        this.owner = owner;
        this.isUrgent = urgency;
        this.description = description;
        this.contact = contact;
    }

    // Getter 和 Setter 方法
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Boolean getUrgent() {
        return isUrgent != null ? isUrgent : false;
    }

    public void setUrgent(Boolean urgent) {
        isUrgent = urgent;
    }

    public BmobFile getItemImage() {
        return itemImage;
    }

    public void setItemImage(BmobFile itemImage) {
        this.itemImage = itemImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Boolean getFound() {
        return isFound != null ? isFound : false;
    }

    public void setFound(Boolean found) {
        isFound = found;
    }


    // 可选：toString方法，便于调试
    @NonNull
    @Override
    public String toString() {
        return "LostItem{" +
                "itemName='" + itemName + '\'' +
                ", category='" + category + '\'' +
                ", lostTime='" + lostTime + '\'' +
                ", lostLocation='" + lostLocation + '\'' +
                ", owner='" + owner + '\'' +
                ", urgency=" + isUrgent +
                '}';
    }


}