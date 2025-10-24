package servlet;

import utils.DBUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // 密码加密（使用 SHA-256）
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashed = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashed) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 设置请求编码和响应格式
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 接收客户端参数
        String username = request.getParameter("username");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");

        // 校验空值
        if (username == null || phone == null || password == null ||
            username.trim().isEmpty() || phone.trim().isEmpty() || password.trim().isEmpty()) {
            out.write("{\"status\":\"error\", \"message\":\"用户名、手机号和密码不能为空\"}");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            // 检查用户名是否已存在
            String checkUserSql = "SELECT COUNT(*) FROM User WHERE username = ?";
            try (PreparedStatement checkUserStmt = conn.prepareStatement(checkUserSql)) {
                checkUserStmt.setString(1, username);
                ResultSet rs = checkUserStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    out.write("{\"status\":\"error\", \"message\":\"用户名已被注册\"}");
                    return;
                }
            }

            // 检查手机号是否已存在
            String checkPhoneSql = "SELECT COUNT(*) FROM User WHERE phone = ?";
            try (PreparedStatement checkPhoneStmt = conn.prepareStatement(checkPhoneSql)) {
                checkPhoneStmt.setString(1, phone);
                ResultSet rs = checkPhoneStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    out.write("{\"status\":\"error\", \"message\":\"手机号已被注册\"}");
                    return;
                }
            }

            // 插入新用户
            String insertSql = "INSERT INTO User (id, username, password, phone, created_at, updated_at) " +
                               "VALUES (?, ?, ?, ?, NOW(), NOW())";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                String id = UUID.randomUUID().toString().replaceAll("-", "");
                String hashedPassword = hashPassword(password);

                insertStmt.setString(1, id);
                insertStmt.setString(2, username);
                insertStmt.setString(3, hashedPassword);
                insertStmt.setString(4, phone);

                int rows = insertStmt.executeUpdate();
                if (rows > 0) {
                    out.write("{\"status\":\"success\", \"message\":\"注册成功\"}");
                } else {
                    out.write("{\"status\":\"error\", \"message\":\"注册失败，请稍后重试\"}");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            out.write("{\"status\":\"error\", \"message\":\"数据库错误: " + e.getMessage() + "\"}");
        } catch (NoSuchAlgorithmException e) {
            out.write("{\"status\":\"error\", \"message\":\"密码加密失败\"}");
        }
    }
}
