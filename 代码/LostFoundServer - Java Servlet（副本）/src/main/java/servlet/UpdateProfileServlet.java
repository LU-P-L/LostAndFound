package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.util.Date;

import com.google.gson.JsonObject;

import bean.User;
import dao.UserDao;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import utils.DBUtil;
import utils.FileUtil;

@WebServlet("/api/user/update")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
    maxFileSize = 1024 * 1024 * 10,      // 10 MB
    maxRequestSize = 1024 * 1024 * 100   // 100 MB
)
public class UpdateProfileServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;

	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 获取文本参数
            String userId = req.getParameter("userId");
            String username = req.getParameter("username");
            String email = req.getParameter("email");
            String phone = req.getParameter("phone");
            
            if (userId == null || username == null || email == null || phone == null) {
                sendErrorResponse(resp, 400, "Missing required parameters");
                return;
            }
            
            // 获取上传的头像文件
            Part filePart = req.getPart("avatar");
            String avatarUrl = null;
            
            if (filePart != null && filePart.getSize() > 0) {
                // 保存上传的文件并获取URL
                avatarUrl = FileUtil.saveUploadedFile(filePart, "lost_items");
                System.out.println("头像保存路径: " + avatarUrl);
            }
            
            try (Connection conn = DBUtil.getConnection()) {
                // 检查用户名是否已被使用
                User existingUser = UserDao.getUserByUsername(conn, username);
                if (existingUser != null && !existingUser.getId().equals(userId)) {
                    sendErrorResponse(resp, 400, "用户名已被使用");
                    return;
                }
                
                // 检查手机号是否已被使用
                existingUser = UserDao.getUserByPhone(conn, phone);
                if (existingUser != null && !existingUser.getId().equals(userId)) {
                    sendErrorResponse(resp, 400, "手机号已被注册");
                    return;
                }
                
                // 更新用户信息
                User user = new User();
                user.setId(userId);
                user.setUsername(username);
                user.setEmail(email);
                user.setPhone(phone);
                user.setUpdatedAt(new Date());
                
                if (avatarUrl != null) {
                    user.setAvatarUrl(avatarUrl);
                }
                
                System.out.println("返回给客户端的头像URL: " + user.getAvatarUrl());
                
                boolean success = UserDao.updateUser(conn, user);
                
                if (success) {
                    JsonObject response = new JsonObject();
                    response.addProperty("code", 200);
                    response.addProperty("message", "更新成功");
                    
                    JsonObject data = new JsonObject();
                    String fullAvatarUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + avatarUrl;

                    data.addProperty("avatarUrl", fullAvatarUrl);
                    response.add("data", data);
                    
                    sendJsonResponse(resp, response);
                } else {
                    sendErrorResponse(resp, 500, "更新失败");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(resp, 500, "服务器错误: " + e.getMessage());
        }
    }
}