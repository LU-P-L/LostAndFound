package bean;

import java.util.Date;

public class Comment {
    private String id;
    private String userId;
    private String lostItemId;
    private String content;
    private String replyToId;
    private Date createdAt;
    private Date updatedAt;
    
    // 非数据库字段，用于关联查询
    private User author;
    private Comment replyTo;
    private String authorName;
    private String avatarUrl;
    
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
    public String getLostItemId() {
        return lostItemId;
    }
    public void setLostItemId(String lostItemId) {
        this.lostItemId = lostItemId;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getReplyToId() {
        return replyToId;
    }
    public void setReplyToId(String replyToId) {
        this.replyToId = replyToId;
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
    public User getAuthor() {
        return author;
    }
    public void setAuthor(User author) {
        this.author = author;
    }
    public Comment getReplyTo() {
        return replyTo;
    }
    public void setReplyTo(Comment replyTo) {
        this.replyTo = replyTo;
    }
    public String getAuthorName() {
        return authorName;
    }
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    public String getAvatarUrl() { 
    	return avatarUrl; 
    }
    public void setAvatarUrl(String avatarUrl) { 
    	this.avatarUrl = avatarUrl;
    }
}