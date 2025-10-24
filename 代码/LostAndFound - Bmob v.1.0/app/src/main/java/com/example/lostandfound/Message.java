package com.example.lostandfound;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

public class Message extends BmobObject {
    private User sender;
    private User receiver;
    private String content;
    private boolean isRead;
    private String conversationId;

    // 构造函数
    public Message() {}

    public Message(User sender, User receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.isRead = false;
    }

    // Getters 和 Setters
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationId() {
        return conversationId;
    }
}