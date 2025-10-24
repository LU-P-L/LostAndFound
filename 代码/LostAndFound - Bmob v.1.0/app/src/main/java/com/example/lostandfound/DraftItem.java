package com.example.lostandfound;

import java.util.List;

import cn.bmob.v3.BmobObject;

public class DraftItem extends BmobObject {
    private String itemName;
    private String category;
    private String lostTime;
    private String lostLocation;
    private String description;
    private Boolean isUrgent;
    private String contact;
    private List<String> imagePaths; // 本地图片路径

    // 无参构造方法（Bmob需要）
    public DraftItem() {
    }

    // 全参构造方法（可选）
    public DraftItem(String itemName, String category, String lostTime,
                     String lostLocation, String description, Boolean isUrgent,
                     List<String> imagePaths, String contact) {
        this.itemName = itemName;
        this.category = category;
        this.lostTime = lostTime;
        this.lostLocation = lostLocation;
        this.description = description;
        this.isUrgent = isUrgent;
        this.imagePaths = imagePaths;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getUrgent() {
        return isUrgent != null ? isUrgent : false;
    }

    public void setUrgent(Boolean urgent) {
        isUrgent = urgent;
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    // 可选：toString方法，便于调试
    @Override
    public String toString() {
        return "DraftItem{" +
                "itemName='" + itemName + '\'' +
                ", category='" + category + '\'' +
                ", lostTime='" + lostTime + '\'' +
                ", lostLocation='" + lostLocation + '\'' +
                ", description='" + description + '\'' +
                ", isUrgent=" + isUrgent +
                ", imagePaths=" + imagePaths +
                '}';
    }
}