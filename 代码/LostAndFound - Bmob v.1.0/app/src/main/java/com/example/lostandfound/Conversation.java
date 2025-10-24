package com.example.lostandfound;

public class Conversation {
    private String userId;
    private String username;
    private String lastMessage;
    private int unreadCount;
    private String avatarUrl;
    private boolean manuallyMarkedUnread;

    // getter方法
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getLastMessage() { return lastMessage; }
    public int getUnreadCount() { return unreadCount; }
    public String getAvatarUrl() { return avatarUrl; }
    public boolean isManuallyMarkedUnread() {
        return manuallyMarkedUnread;
    }

    // setter方法
    public void setUserId(String userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setManuallyMarkedUnread(boolean manuallyMarkedUnread) {
        this.manuallyMarkedUnread = manuallyMarkedUnread;
    }
}