package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import bean.User;

public class UserDao {
    
    public boolean checkUserExists(Connection conn, String userId) throws SQLException {
        String sql = "SELECT id FROM user WHERE id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    public static User getUserById(Connection conn, String userId) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setAvatarUrl(rs.getString("avatar_url"));
                    user.setPhone(rs.getString("phone"));
                    user.setPhoneVerified(rs.getBoolean("phone_verified"));
                    user.setEmailVerified(rs.getBoolean("email_verified"));
                    user.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                    user.setUpdatedAt(new Date(rs.getTimestamp("updated_at").getTime()));
                    return user;
                }
            }
        }
        return null;
    }
    
    public static User getUserByUsernameOrEmail(Connection conn, String usernameOrEmail) throws SQLException {
        String sql = "SELECT * FROM user WHERE username = ? OR email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, usernameOrEmail);
            pstmt.setString(2, usernameOrEmail);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getString("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setPassword(rs.getString("password"));
                    user.setAvatarUrl(rs.getString("avatar_url"));
                    user.setPhone(rs.getString("phone"));
                    user.setPhoneVerified(rs.getBoolean("phone_verified"));
                    user.setEmailVerified(rs.getBoolean("email_verified"));
                    user.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
                    user.setUpdatedAt(new Date(rs.getTimestamp("updated_at").getTime()));
                    return user;
                }
            }
        }
        return null;
    }
    
    public static User getUserByUsername(Connection conn, String username) throws SQLException {
        String sql = "SELECT * FROM user WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }
    
    public static User getUserByPhone(Connection conn, String phone) throws SQLException {
        String sql = "SELECT * FROM user WHERE phone = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }
    
    public static boolean updateUser(Connection conn, User user) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE user SET ");
        if (user.getUsername() != null) sql.append("username = ?, ");
        if (user.getEmail() != null) sql.append("email = ?, ");
        if (user.getPhone() != null) sql.append("phone = ?, ");
        if (user.getAvatarUrl() != null) sql.append("avatar_url = ?, ");
        if (user.getPassword() != null) sql.append("password = ?, ");
        sql.append("updated_at = ? WHERE id = ?");

        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int index = 1;
            if (user.getUsername() != null) pstmt.setString(index++, user.getUsername());
            if (user.getEmail() != null) pstmt.setString(index++, user.getEmail());
            if (user.getPhone() != null) pstmt.setString(index++, user.getPhone());
            if (user.getAvatarUrl() != null) pstmt.setString(index++, user.getAvatarUrl());
            if (user.getPassword() != null) pstmt.setString(index++, user.getPassword());
            pstmt.setTimestamp(index++, new Timestamp(user.getUpdatedAt().getTime()));
            pstmt.setString(index++, user.getId());

            return pstmt.executeUpdate() > 0;
        }
    }
    
    private static User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        user.setPhone(rs.getString("phone"));
        user.setPhoneVerified(rs.getBoolean("phone_verified"));
        user.setEmailVerified(rs.getBoolean("email_verified"));
        user.setCreatedAt(new Date(rs.getTimestamp("created_at").getTime()));
        user.setUpdatedAt(new Date(rs.getTimestamp("updated_at").getTime()));
        return user;
    }
}