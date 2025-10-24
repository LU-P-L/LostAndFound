package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.DBUtil;
import utils.TokenUtil;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // 密码加密方法（与注册时保持一致）
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
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");

        // 校验空值
        if (phone == null || password == null || 
            phone.trim().isEmpty() || password.trim().isEmpty()) {
            out.write("{\"status\":\"error\", \"message\":\"手机号和密码不能为空\"}");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            // 查询用户信息
            String sql = "SELECT id, username, password, phone, avatar_url FROM User WHERE phone = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, phone);
                
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    // 用户存在，验证密码
                    String storedHashedPassword = rs.getString("password");
                    String inputHashedPassword = hashPassword(password);
                    
                    if (storedHashedPassword.equals(inputHashedPassword)) {
                    	// 生成 JWT Token
                        String userId = rs.getString("id");
                        String token = TokenUtil.generateToken(userId);
                        
                     // 返回用户信息 + token
                        out.write("{\"status\":\"success\", \"message\":\"登录成功\", " +
                                "\"token\":\"" + token + "\", " +
                                "\"user\":{" +
                                "\"id\":\"" + userId + "\", " +
                                "\"username\":\"" + rs.getString("username") + "\", " +
                                "\"phone\":\"" + rs.getString("phone") + "\", " +
                                "\"avatar_url\":\"" + (rs.getString("avatar_url") != null ? rs.getString("avatar_url") : "") + "\"" +
                                "}}");
                    } else {
                        // 密码不匹配
                        out.write("{\"status\":\"error\", \"message\":\"手机号或密码错误\"}");
                    }
                } else {
                    // 用户不存在
                    out.write("{\"status\":\"error\", \"message\":\"该用户未注册\"}");
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