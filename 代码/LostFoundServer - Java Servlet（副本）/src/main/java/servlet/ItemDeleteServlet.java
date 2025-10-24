package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import utils.DBUtil;

@WebServlet("/api/items/delete")
public class ItemDeleteServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String id = request.getParameter("id");
            if (id == null || id.isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "缺少物品ID参数");
                return;
            }

            Connection conn = DBUtil.getConnection();
            String sql = "DELETE FROM lostitem WHERE id = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, id);
                int rows = pstmt.executeUpdate();
                
                JsonObject result = new JsonObject();
                if (rows > 0) {
                    result.addProperty("success", true);
                    sendJsonResponse(response, result);
                } else {
                    result.addProperty("success", false);
                    result.addProperty("message", "未找到指定的失物信息");
                    sendJsonResponse(response, result);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "数据库错误: " + e.getMessage());
        }
    }
}