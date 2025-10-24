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

@WebServlet("/api/items/listWithFilters")
public class MainItemServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    
    private LostItemDao lostItemDao = new LostItemDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String keyword = request.getParameter("keyword");
            String category = request.getParameter("category");
            String timeRange = request.getParameter("timeRange");
            String urgentOnlyStr = request.getParameter("urgentOnly");
            boolean urgentOnly = "true".equalsIgnoreCase(urgentOnlyStr);

            Connection conn = DBUtil.getConnection();
            List<LostItem> items = new ArrayList<>();

            if ((keyword == null || keyword.isEmpty()) && (category == null || category.isEmpty())
                    && (timeRange == null || timeRange.isEmpty()) && !urgentOnly) {
                // 无任何筛选，查询所有
                items = getAllLostItems(conn);
            } else {
                // 带筛选条件查询
                items = searchLostItemsWithFilters(conn, keyword, category, timeRange, urgentOnly);
            }

            sendJsonResponse(response, items);
        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "数据库错误: " + e.getMessage());
        }
    }
    
    private List<LostItem> getAllLostItems(Connection conn) throws SQLException {
        String sql = "SELECT li.*, u.username, u.phone " +
                     "FROM lostitem li " +
                     "JOIN user u ON li.user_id = u.id " +
                     "WHERE li.is_found = false " +
                     "ORDER BY li.created_at DESC";

        try (var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {

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
    
    private List<LostItem> searchLostItems(Connection conn, String keyword) throws SQLException {
        // 实现根据关键词搜索失物的逻辑
        String sql = "SELECT li.*, u.username, u.phone " +
                "FROM lostitem li " +
                "JOIN user u ON li.user_id = u.id " +
                "WHERE li.is_found = false AND " +
                "(li.item_name LIKE ? OR li.description LIKE ? OR li.lost_location LIKE ?) " +
                "ORDER BY li.created_at DESC";
        
        try (var pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
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
    
    private List<LostItem> searchLostItemsWithFilters(Connection conn, String keyword, String category, String timeRange, boolean urgentOnly) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT li.*, u.username, u.phone " +
                "FROM lostitem li JOIN user u ON li.user_id = u.id WHERE li.is_found = false");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (li.item_name LIKE ? OR li.description LIKE ? OR li.lost_location LIKE ?)");
            String likeKeyword = "%" + keyword + "%";
            params.add(likeKeyword);
            params.add(likeKeyword);
            params.add(likeKeyword);
        }

        if (category != null && !category.isEmpty()) {
            sql.append(" AND li.category = ?");
            params.add(category);
        }

        if (urgentOnly) {
            sql.append(" AND li.is_urgent = true");
        }

        if (timeRange != null && !timeRange.isEmpty() && !"all".equalsIgnoreCase(timeRange)) {
            // 计算时间起点
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            java.util.Date endDate = new java.util.Date();
            switch (timeRange) {
                case "today":
                    calendar.add(java.util.Calendar.DAY_OF_MONTH, -1);
                    break;
                case "week":
                    calendar.add(java.util.Calendar.WEEK_OF_YEAR, -1);
                    break;
                case "month":
                    calendar.add(java.util.Calendar.MONTH, -1);
                    break;
            }
            java.util.Date startDate = calendar.getTime();
            sql.append(" AND li.created_at BETWEEN ? AND ?");
            params.add(new java.sql.Timestamp(startDate.getTime()));
            params.add(new java.sql.Timestamp(endDate.getTime()));
        }

        sql.append(" ORDER BY li.created_at DESC");

        try (var pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String) {
                    pstmt.setString(i + 1, (String) p);
                } else if (p instanceof java.sql.Timestamp) {
                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) p);
                }
            }

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