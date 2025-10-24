package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import bean.LostItem;
import utils.DBUtil;

public class LostItemDao {
    
    public boolean insertLostItem(Connection conn, LostItem lostItem) throws SQLException {
        String sql = "INSERT INTO lostitem (id, user_id, item_name, description, category, " +
                     "lost_location, lost_time, contact_info, picture_url, is_urgent, is_found, " +
                     "created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, lostItem.getId());
            pstmt.setString(2, lostItem.getUserId());
            pstmt.setString(3, lostItem.getItemName());
            pstmt.setString(4, lostItem.getDescription());
            pstmt.setString(5, lostItem.getCategory());
            pstmt.setString(6, lostItem.getLostLocation());
            pstmt.setString(7, lostItem.getLostTime());
            pstmt.setString(8, lostItem.getContactInfo());
            pstmt.setString(9, lostItem.getPictureUrl());
            pstmt.setBoolean(10, lostItem.isUrgent());
            pstmt.setBoolean(11, lostItem.isFound());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public LostItem getLostItemById(String id) throws SQLException {
        Connection conn = DBUtil.getConnection();
        String sql = "SELECT * FROM lostitem WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                LostItem item = new LostItem();
                item.setId(rs.getString("id"));
                item.setUserId(rs.getString("user_id"));
                item.setItemName(rs.getString("item_name"));
                item.setDescription(rs.getString("description"));
                item.setCategory(rs.getString("category"));
                item.setLostLocation(rs.getString("lost_location"));
                item.setLostTime(rs.getString("lost_time"));
                item.setContactInfo(rs.getString("contact_info"));
                item.setPictureUrl(rs.getString("picture_url"));
                item.setUrgent(rs.getBoolean("is_urgent"));
                item.setFound(rs.getBoolean("is_found"));
                item.setCreatedAt(rs.getTimestamp("created_at"));
                item.setUpdatedAt(rs.getTimestamp("updated_at"));
                return item;
            }
            return null;
        }
    }
    
    public List<LostItem> getAllLostItems(Connection conn) throws SQLException {
        String sql = "SELECT * FROM lostitem WHERE is_found = false ORDER BY created_at DESC";
        try (var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {
            
            List<LostItem> items = new ArrayList<>();
            while (rs.next()) {
                LostItem item = new LostItem();
                item.setId(rs.getString("id"));
                item.setUserId(rs.getString("user_id"));
                item.setItemName(rs.getString("item_name"));
                item.setDescription(rs.getString("description"));
                item.setCategory(rs.getString("category"));
                item.setLostLocation(rs.getString("lost_location"));
                item.setLostTime(rs.getString("lost_time"));
                item.setContactInfo(rs.getString("contact_info"));
                item.setPictureUrl(rs.getString("picture_url"));
                item.setUrgent(rs.getBoolean("is_urgent"));
                item.setFound(rs.getBoolean("is_found"));
                items.add(item);
            }
            return items;
        }
    }
    
    public List<LostItem> searchLostItems(Connection conn, String keyword) throws SQLException {
        String sql = "SELECT * FROM lostitem WHERE is_found = false AND " +
                     "(item_name LIKE ? OR description LIKE ? OR lost_location LIKE ?) " +
                     "ORDER BY created_at DESC";
        
        try (var pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            var rs = pstmt.executeQuery();
            List<LostItem> items = new ArrayList<>();
            while (rs.next()) {
                LostItem item = new LostItem();
                item.setId(rs.getString("id"));
                item.setUserId(rs.getString("user_id"));
                item.setItemName(rs.getString("item_name"));
                item.setDescription(rs.getString("description"));
                item.setCategory(rs.getString("category"));
                item.setLostLocation(rs.getString("lost_location"));
                item.setLostTime(rs.getString("lost_time"));
                item.setContactInfo(rs.getString("contact_info"));
                item.setPictureUrl(rs.getString("picture_url"));
                item.setUrgent(rs.getBoolean("is_urgent"));
                item.setFound(rs.getBoolean("is_found"));
                items.add(item);
            }
            return items;
        }
    }

}