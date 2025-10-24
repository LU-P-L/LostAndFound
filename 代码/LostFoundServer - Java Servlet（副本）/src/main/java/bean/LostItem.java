package bean;

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
    private Date createdAt;
    private Date updatedAt;
    private User owner;
    
    // Getters and Setters
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
    public String getLostLocation() {
        return lostLocation;
    }
    public void setLostLocation(String lostLocation) {
        this.lostLocation = lostLocation;
    }
    public String getLostTime() {
        return lostTime;
    }
    public void setLostTime(String lostTime) {
        this.lostTime = lostTime;
    }
    public String getContactInfo() {
        return contactInfo;
    }
    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
    public String getPictureUrl() {
        return pictureUrl;
    }
    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
    public boolean isUrgent() {
        return urgent;
    }
    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }
    public boolean isFound() {
        return found;
    }
    public void setFound(boolean found) {
        this.found = found;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public Date getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    public User getOwner() {
        return owner;
    }
    public void setOwner(User owner) {
        this.owner = owner;
    }
}