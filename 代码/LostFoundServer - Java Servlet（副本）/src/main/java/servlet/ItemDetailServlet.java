package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import bean.LostItem;
import bean.User;
import dao.LostItemDao;
import utils.DBUtil;

@WebServlet("/api/items/detail")
public class ItemDetailServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private LostItemDao lostItemDao = new LostItemDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String id = request.getParameter("id");
            if (id == null || id.isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "缺少物品ID参数");
                return;
            }

            Connection conn = DBUtil.getConnection();
            LostItem item = lostItemDao.getLostItemById(id);
            
            if (item != null) {
                // 获取用户信息
                String userSql = "SELECT id, username, phone FROM user WHERE id = ?";
                try (var pstmt = conn.prepareStatement(userSql)) {
                    pstmt.setString(1, item.getUserId());
                    var rs = pstmt.executeQuery();
                    if (rs.next()) {
                        User owner = new User();
                        owner.setId(rs.getString("id"));
                        owner.setUsername(rs.getString("username"));
                        owner.setPhone(rs.getString("phone"));
                        item.setOwner(owner);
                    }
                }
                
                sendJsonResponse(response, item);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "未找到指定的失物信息");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "数据库错误: " + e.getMessage());
        }
    }
}