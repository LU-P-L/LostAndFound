package servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.google.gson.JsonObject;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.DBUtil;
import utils.TokenUtil;

@WebServlet("/api/items/updateStatus")
public class UpdateStatusServlet extends BaseServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, java.io.IOException {
        try {
            JsonObject jsonObject = parseJsonRequest(request);
            String itemId = jsonObject.get("itemId").getAsString();
            boolean isFound = jsonObject.get("isFound").getAsBoolean();

            // 验证Token
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未授权访问");
                return;
            }

            String userId = TokenUtil.getUserIdFromToken(token.substring(7));
            if (userId == null) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "无效的Token");
                return;
            }

            // 更新数据库 - 放宽条件，只检查物品ID
            Connection conn = DBUtil.getConnection();
            String sql = "UPDATE lostitem SET is_found = ? WHERE id = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setBoolean(1, isFound);
                pstmt.setString(2, itemId);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "未找到物品");
                    return;
                }
            }

            // 返回成功响应
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "状态更新成功");
            sendJsonResponse(response, responseJson);
        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "数据库错误: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "请求参数错误: " + e.getMessage());
        }
    }
}