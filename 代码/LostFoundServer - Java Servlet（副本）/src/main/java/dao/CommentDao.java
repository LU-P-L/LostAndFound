package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import bean.Comment;
import bean.User;
import utils.DBUtil;

public class CommentDao {
    
    // 插入评论
    public boolean insertComment(Connection conn, Comment comment) throws SQLException {
        String sql = "INSERT INTO comment (id, user_id, lost_item_id, content, reply_to_id, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // 生成UUID作为ID
            String id = UUID.randomUUID().toString().replace("-", "");
            
            pstmt.setString(1, id);
            pstmt.setString(2, comment.getUserId());
            pstmt.setString(3, comment.getLostItemId());
            pstmt.setString(4, comment.getContent());
            pstmt.setString(5, comment.getReplyToId());
            pstmt.setTimestamp(6, new Timestamp(new Date().getTime()));
            pstmt.setTimestamp(7, new Timestamp(new Date().getTime()));
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                comment.setId(id);
                return true;
            }
            return false;
        }
    }
    
    // 根据失物ID查询评论列表
    public List<Comment> getCommentsByLostItemId(String lostItemId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            // 修改SQL查询，包含用户头像URL
            String sql = "SELECT c.*, u.username AS author_name, u.avatar_url AS avatar_url, " +
                         "ru.username AS reply_to_author_name, ru.avatar_url AS reply_to_avatar_url " +
                         "FROM comment c " +
                         "LEFT JOIN user u ON c.user_id = u.id " +
                         "LEFT JOIN comment rc ON c.reply_to_id = rc.id " +
                         "LEFT JOIN user ru ON rc.user_id = ru.id " +
                         "WHERE c.lost_item_id = ? " +
                         "ORDER BY c.created_at ASC";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, lostItemId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getString("id"));
                comment.setUserId(rs.getString("user_id"));
                comment.setLostItemId(rs.getString("lost_item_id"));
                comment.setContent(rs.getString("content"));
                comment.setReplyToId(rs.getString("reply_to_id"));
                comment.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                comment.setUpdatedAt(new Date(rs.getTimestamp("updated_at").getTime()));
                
                // 设置作者信息（包含头像）
                User author = new User();
                author.setId(rs.getString("user_id"));
                author.setUsername(rs.getString("author_name"));
                author.setAvatarUrl(rs.getString("avatar_url")); // 设置头像URL
                comment.setAuthor(author);
                comment.setAuthorName(rs.getString("author_name"));
                comment.setAvatarUrl(rs.getString("avatar_url")); // 设置评论头像URL
                
                // 如果有回复对象，设置回复对象信息（包含头像）
                if (rs.getString("reply_to_id") != null) {
                    Comment replyTo = new Comment();
                    replyTo.setId(rs.getString("reply_to_id"));
                    
                    User replyToAuthor = new User();
                    replyToAuthor.setUsername(rs.getString("reply_to_author_name"));
                    replyToAuthor.setAvatarUrl(rs.getString("reply_to_avatar_url")); // 设置回复对象的头像URL
                    
                    replyTo.setAuthor(replyToAuthor);
                    comment.setReplyTo(replyTo);
                }
                
                comments.add(comment);
            }
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closeStatement(pstmt);
            DBUtil.closeConnection(conn);
        }
        
        return comments;
    }
}