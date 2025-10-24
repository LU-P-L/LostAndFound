package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import bean.Message;
import dao.MessageDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.JsonUtil;

@WebServlet("/api/messages")
public class InsertMessageServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String convId = req.getParameter("conversationId");
        if (convId == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"missing conversationId\"}");
            return;
        }

        List<Message> messages = new MessageDao().getMessagesByConversation(convId);
        JsonUtil.sendJsonResponse(resp, 200, "ok", messages);
    }
	
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);

        try {
            Message message = JsonUtil.fromJson(sb.toString(), Message.class);

            // 校验字段
            if (message.getSenderId() == null || message.getReceiverId() == null || message.getContent() == null) {
                JsonUtil.sendJsonResponse(response, 400, "缺少参数");
                return;
            }

            // 构造会话ID
            String a = message.getSenderId();
            String b = message.getReceiverId();
            message.setConversationId(a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a);

            // 插入数据库
            MessageDao dao = new MessageDao();
            boolean success = dao.insertMessage(message);
            if (success) {
                JsonUtil.sendJsonResponse(response, 200, "发送成功", message);
            } else {
                JsonUtil.sendJsonResponse(response, 500, "发送失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(response, 500, "服务器错误: " + e.getMessage());
        }
    }
}
