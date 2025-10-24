package com.example.lostandfound;

import cn.bmob.v3.BmobUser;

public class User extends BmobUser {
    private String avatar; // 头像URL
    private int unreadCount; // 未读消息数量

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getUnreadCount() { return unreadCount; }

    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    // 增加未读消息计数
    public void incrementUnreadCount() {
        if (unreadCount < 99) {
            unreadCount++;
        }
    }

    // 重置未读消息计数
    public void resetUnreadCount() {
        unreadCount = 0;
    }
}