package com.example.lostandfound;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;

public class Comment extends BmobObject {
    private String content; // 评论内容
    private User author; // 评论作者
    private LostItem lostItem; // 关联的失物信息
    private Comment replyTo; // 回复的评论

    // getters and setters
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

    public LostItem getLostItem() {
        return lostItem;
    }

    public void setLostItem(LostItem lostItem) {
        this.lostItem = lostItem;
    }

    public Comment getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Comment replyTo) {
        this.replyTo = replyTo;
    }
}