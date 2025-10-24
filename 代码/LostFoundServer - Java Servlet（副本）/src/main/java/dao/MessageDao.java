package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import bean.Conversation;
import bean.Message;
import utils.DBUtil;

public class MessageDao {
    public boolean insertMessage(Message message) throws SQLException {
        String sql = "INSERT INTO message (id, sender_id, receiver_id, content, conversation_id, is_read, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String id = UUID.randomUUID().toString().replaceAll("-", "");
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            stmt.setString(1, id);
            stmt.setString(2, message.getSenderId());
            stmt.setString(3, message.getReceiverId());
            stmt.setString(4, message.getContent());
            stmt.setString(5, message.getConversationId());
            stmt.setBoolean(6, false);
            stmt.setString(7, now);
            stmt.setString(8, now);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                message.setId(id);
                message.setCreatedAt(now);
                message.setUpdatedAt(now);
                return true;
            }
            return false;
        }
    }
    
    public List<Message> getMessagesByConversation(String conversationId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u1.avatar_url as sender_avatar, u2.avatar_url as receiver_avatar " +
                     "FROM message m " +
                     "LEFT JOIN user u1 ON m.sender_id = u1.id " +
                     "LEFT JOIN user u2 ON m.receiver_id = u2.id " +
                     "WHERE m.conversation_id = ? ORDER BY m.created_at ASC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, conversationId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Message msg = new Message();
                msg.setId(rs.getString("id"));
                msg.setSenderId(rs.getString("sender_id"));
                msg.setReceiverId(rs.getString("receiver_id"));
                msg.setContent(rs.getString("content"));
                msg.setConversationId(rs.getString("conversation_id"));
                msg.setIsRead(rs.getBoolean("is_read"));
                msg.setCreatedAt(rs.getString("created_at"));
                msg.setUpdatedAt(rs.getString("updated_at"));
                msg.setSenderAvatar(rs.getString("sender_avatar")); // 设置发送者头像
                msg.setReceiverAvatar(rs.getString("receiver_avatar")); // 设置接收者头像
                
                messages.add(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return messages;
    }
    
    public List<Conversation> getConversationsForUser(String userId) {
        List<Conversation> conversations = new ArrayList<>();
        String sql = """
            SELECT m.*, u.username AS other_username, u.avatar_url AS other_avatar
            FROM (
                SELECT *,
                ROW_NUMBER() OVER (PARTITION BY conversation_id ORDER BY updated_at DESC) AS rn
                FROM message
                WHERE sender_id = ? OR receiver_id = ?
            ) m
            JOIN user u ON u.id = CASE
                WHEN m.sender_id = ? THEN m.receiver_id
                ELSE m.sender_id
            END
            WHERE m.rn = 1
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, userId);
            stmt.setString(3, userId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Conversation conv = new Conversation();
                String convId = rs.getString("conversation_id");
                conv.setConversationId(convId);
                
                // 查询未读消息数
                String countSql = "SELECT COUNT(*) FROM message WHERE conversation_id = ? AND receiver_id = ? AND is_read = false";
                try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                    countStmt.setString(1, convId);
                    countStmt.setString(2, userId);
                    try (ResultSet countRs = countStmt.executeQuery()) {
                        if (countRs.next()) {
                            conv.setUnreadCount(countRs.getInt(1));
                        }
                    }
                }
                
                conv.setLastMessage(rs.getString("content"));
                conv.setUpdatedAt(rs.getString("updated_at"));
                String sender = rs.getString("sender_id");
                String receiver = rs.getString("receiver_id");
                String otherUserId = userId.equals(sender) ? receiver : sender;
                
                conv.setSenderId(sender);
                conv.setOtherUserId(otherUserId);
                conv.setOtherUsername(rs.getString("other_username"));
                conv.setAvatarUrl(rs.getString("other_avatar")); // 设置头像URL
                
                conversations.add(conv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conversations;
    }

    
    public boolean markConversationRead(String conversationId, String userId) {
        String sql = "UPDATE message SET is_read = true WHERE conversation_id = ? AND receiver_id = ? AND is_read = false";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, conversationId);
            stmt.setString(2, userId);
            int rows = stmt.executeUpdate();
            
            return rows >= 0; // 即使没有需要更新的记录也返回true
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean markAllMessagesRead(String userId) {
        String sql = "UPDATE message SET is_read = true WHERE receiver_id = ? AND is_read = false";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, userId);
            int rows = stmt.executeUpdate();
            
            return rows >= 0; // 即使没有需要更新的记录也返回true
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
