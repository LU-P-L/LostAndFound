package servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import dao.MessageDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/messages/conversations")
public class ConversationsServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userId = req.getParameter("userId");
        if (userId == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"missing userId\"}");
            return;
        }

        List<bean.Conversation> convs = new MessageDao().getConversationsForUser(userId);
        List<Map<String, Object>> responseData = new ArrayList<>();
        
        for (bean.Conversation conv : convs) {
            Map<String, Object> item = new HashMap<>();
            item.put("otherUserId", conv.getOtherUserId());
            item.put("otherUsername", conv.getOtherUsername());
            item.put("lastMessage", conv.getLastMessage());
            item.put("updatedAt", conv.getUpdatedAt());
            item.put("senderId", conv.getSenderId());
            item.put("unreadCount", conv.getUnreadCount());
            item.put("avatarUrl", conv.getAvatarUrl()); // 添加头像URL
            
            responseData.add(item);
        }

        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("code", 200);
        apiResponse.put("message", "ok");
        apiResponse.put("data", responseData);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(new Gson().toJson(apiResponse));
    }
}