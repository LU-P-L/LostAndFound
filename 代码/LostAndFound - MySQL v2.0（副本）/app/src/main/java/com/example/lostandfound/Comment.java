package com.example.lostandfound;

import com.google.gson.annotations.SerializedName;

public class Comment {
    @SerializedName("id")
    private String id;

    @SerializedName("content")
    private String content;

    @SerializedName("author")
    private User author;

    @SerializedName("lostItemId")
    private String lostItemId;

    @SerializedName("replyTo")
    private Comment replyTo;

    @SerializedName("createdAt")
    private String createdAt;

    private String userId;
    private String replyToId;
    private String avatarUrl;

    // getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getLostItemId() {
        return lostItemId;
    }

    public void setLostItemId(String lostItemId) {
        this.lostItemId = lostItemId;
    }

    public Comment getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Comment replyTo) {
        this.replyTo = replyTo;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(String replyToId) {
        this.replyToId = replyToId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}