package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.DBUtil;
import bean.LostItem;
import bean.User;
import dao.LostItemDao;
import utils.TokenUtil;

@WebServlet("/api/items/myItems")
public class MyItemServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    
    private LostItemDao lostItemDao = new LostItemDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 从Token中获取用户ID
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

            Connection conn = DBUtil.getConnection();
            List<LostItem> items = getMyLostItems(conn, userId);

            sendJsonResponse(response, items);
        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "数据库错误: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器错误: " + e.getMessage());
        }
    }
    
    private List<LostItem> getMyLostItems(Connection conn, String userId) throws SQLException {
        String sql = "SELECT li.*, u.username, u.phone " +
                     "FROM lostitem li " +
                     "JOIN user u ON li.user_id = u.id " +
                     "WHERE li.user_id = ? " +
                     "ORDER BY li.created_at DESC";

        try (var pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            
            var rs = pstmt.executeQuery();
            List<LostItem> items = new ArrayList<>();
            while (rs.next()) {
                LostItem item = new LostItem();
                item.setId(rs.getString("id"));
                item.setUserId(rs.getString("user_id"));
                item.setItemName(rs.getString("item_name"));
                item.setDescription(rs.getString("description"));
                item.setCategory(rs.getString("category"));
                item.setLostLocation(rs.getString("lost_location"));
                item.setLostTime(rs.getString("lost_time"));
                item.setContactInfo(rs.getString("contact_info"));
                item.setPictureUrl(rs.getString("picture_url"));
                item.setUrgent(rs.getBoolean("is_urgent"));
                item.setFound(rs.getBoolean("is_found"));

                // 设置用户信息
                User owner = new User();
                owner.setId(rs.getString("user_id"));
                owner.setUsername(rs.getString("username"));
                owner.setPhone(rs.getString("phone"));
                item.setOwner(owner);

                items.add(item);
            }
            return items;
        }
    }
}