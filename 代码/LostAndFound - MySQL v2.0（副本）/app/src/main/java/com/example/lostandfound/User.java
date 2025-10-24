package com.example.lostandfound;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    private String id;
    private String username;
    private String email;
    private String password;
    private String avatarUrl;
    private String phone;
    private boolean phoneVerified;
    private boolean emailVerified;
    private String createdAt;
    private String updatedAt;

    // 客户端专属字段（不会从后端返回）
    private int unreadCount;

    // 必须的空构造函数
    public User() {}

    public User(String id, String username) {
        this.id = id;
        this.username = username;
    }

    // -------- Getters & Setters --------
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username != null ? username : "未知用户";
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void incrementUnreadCount() {
        if (unreadCount < 99) {
            unreadCount++;
        }
    }

    public void resetUnreadCount() {
        unreadCount = 0;
    }
}
