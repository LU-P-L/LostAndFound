package servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dao.MessageDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/messages/markRead")
public class MarkReadServlet extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String body = req.getReader().lines().collect(Collectors.joining());
        JsonObject json = new Gson().fromJson(body, JsonObject.class);
        
        String convId = json.has("conversationId") ? json.get("conversationId").getAsString() : null;
        String userId = json.has("userId") ? json.get("userId").getAsString() : null;

        if (convId == null || userId == null) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"missing conversationId or userId\"}");
            return;
        }

        boolean ok = new MessageDao().markConversationRead(convId, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", ok ? 200 : 500);
        response.put("message", ok ? "ok" : "failed");
        
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(new Gson().toJson(response));
    }
}