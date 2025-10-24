package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.google.gson.JsonObject;

import bean.User;
import dao.UserDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.DBUtil;

@WebServlet("/api/user")
public class UserServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userId = req.getParameter("userId");
        if (userId == null) {
            sendErrorResponse(resp, 400, "Missing userId parameter");
            return;
        }
        
        try (Connection conn = DBUtil.getConnection()) {
            User user = UserDao.getUserById(conn, userId);
            if (user == null) {
                sendErrorResponse(resp, 404, "User not found");
                return;
            }

            // 构建 data 对象
            JsonObject data = new JsonObject();
            data.addProperty("id", user.getId());
            data.addProperty("username", user.getUsername());
            data.addProperty("email", user.getEmail());
            
            // 处理头像URL - 如果存在则添加，否则不添加该字段
            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            	System.out.println("个人页面头像路径: " + user.getAvatarUrl());
                data.addProperty("avatarUrl", user.getAvatarUrl());
            }
            
            data.addProperty("phone", user.getPhone());
            data.addProperty("phoneVerified", user.getPhoneVerified());
            data.addProperty("emailVerified", user.getEmailVerified());
            
            // 封装为标准响应结构
            JsonObject result = new JsonObject();
            result.addProperty("code", 200);
            result.addProperty("msg", "查询成功");
            result.add("data", data);
            
            sendJsonResponse(resp, result);
        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(resp, 500, "Database error");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject json = parseJsonRequest(req);
        String action = json.has("action") ? json.get("action").getAsString() : null;
        
        if ("logout".equals(action)) {
            // 构造登出成功的响应结构
            JsonObject result = new JsonObject();
            result.addProperty("code", 200);
            result.addProperty("msg", "登出成功");
            result.add("data", new JsonObject());  // 空对象

            sendJsonResponse(resp, result);
            return;
        }
        
        sendErrorResponse(resp, 400, "Invalid action");
    }
}