package servlet;

import java.io.IOException;
import java.sql.Connection;

import bean.User;
import dao.UserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.DBUtil;
import utils.SHA256Util;

@WebServlet("/api/user/resetPassword")
public class ResetPasswordServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 获取请求参数
        String phone = request.getParameter("phone");
        String captcha = request.getParameter("captcha");
        String newPassword = request.getParameter("newPassword");
        
        // 验证session中的验证码
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("captchaCode") == null) {
            sendError(response, "验证码已过期，请重新获取");
            return;
        }
        
        String sessionCaptcha = (String) session.getAttribute("captchaCode");
        if (!sessionCaptcha.equalsIgnoreCase(captcha)) {
            sendError(response, "验证码错误");
            return;
        }
        
        // 验证新密码复杂度
        if (!isPasswordValid(newPassword)) {
            sendError(response, "密码需包含数字、小写字母、大写字母、特殊字符(，。？！)中的至少三种，长度8-20位");
            return;
        }
        
        // 更新数据库
        try (Connection conn = DBUtil.getConnection()) {
            User user = UserDao.getUserByPhone(conn, phone);
            if (user == null) {
                sendError(response, "该手机号未注册");
                return;
            }
            
            // 更新密码
            String hashedPassword = SHA256Util.sha256(newPassword);  // 调用统一加密函数
            user.setPassword(hashedPassword);
            if (UserDao.updateUser(conn, user)) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\":true,\"message\":\"密码修改成功\"}");
            } else {
                sendError(response, "密码修改失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "服务器错误");
        }
    }
    
    private boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8 || password.length() > 20) {
            return false;
        }
        
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasSpecial = password.matches(".*[,.!?].*");
        
        int complexityCount = 0;
        if (hasDigit) complexityCount++;
        if (hasLower) complexityCount++;
        if (hasUpper) complexityCount++;
        if (hasSpecial) complexityCount++;
        
        return complexityCount >= 3;
    }
    
    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(String.format("{\"success\":false,\"message\":\"%s\"}", message));
    }
}