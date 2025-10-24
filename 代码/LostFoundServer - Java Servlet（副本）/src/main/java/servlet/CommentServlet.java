package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import bean.Comment;
import dao.CommentDao;
import dao.LostItemDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.DBUtil;
import utils.JsonUtil;

@WebServlet("/api/comments")
public class CommentServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private CommentDao commentDao = new CommentDao();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String lostItemId = request.getParameter("lost_item_id");
        if (lostItemId == null || lostItemId.isEmpty()) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "缺少lost_item_id参数");
            return;
        }
        
        try {
            List<Comment> comments = commentDao.getCommentsByLostItemId(lostItemId);
            sendJsonResponse(response, comments);
        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                             "数据库错误: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            // 从请求体中解析JSON数据
        	BufferedReader reader = request.getReader();
        	StringBuilder jsonBuilder = new StringBuilder();
        	String line;
        	while ((line = reader.readLine()) != null) {
        	    jsonBuilder.append(line);
        	}
        	Comment comment = JsonUtil.fromJson(jsonBuilder.toString(), Comment.class);

            if (comment == null || comment.getUserId() == null || 
                comment.getLostItemId() == null || comment.getContent() == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "参数不完整");
                return;
            }
            
            Connection conn = null;
            try {
                conn = DBUtil.getConnection();
                conn.setAutoCommit(false); // 开始事务
                
                // 检查失物是否存在
                LostItemDao lostItemDao = new LostItemDao();
                if (lostItemDao.getLostItemById(comment.getLostItemId()) == null) {
                    sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "失物信息不存在");
                    return;
                }
                
                // 插入评论
                boolean success = commentDao.insertComment(conn, comment);
                if (success) {
                    conn.commit();
                    sendJsonResponse(response, comment);
                } else {
                    conn.rollback();
                    sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                                    "评论发布失败");
                }
            } catch (SQLException e) {
                if (conn != null) {
                    conn.rollback();
                }
                throw e;
            } finally {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    DBUtil.closeConnection(conn);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                             "服务器错误: " + e.getMessage());
        }
    }
}