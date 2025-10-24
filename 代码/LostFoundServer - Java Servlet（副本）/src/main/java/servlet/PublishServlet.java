package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import bean.LostItem;
import dao.LostItemDao;
import dao.UserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import utils.DBUtil;
import utils.FileUploadUtil;
import utils.JsonUtil;
import utils.TokenUtil;

@WebServlet("/api/publish")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
    maxFileSize = 1024 * 1024 * 10,      // 10 MB
    maxRequestSize = 1024 * 1024 * 100   // 100 MB
)
public class PublishServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private LostItemDao lostItemDao;
    private UserDao userDao;
    
    @Override
    public void init() throws ServletException {
        super.init();
        lostItemDao = new LostItemDao();
        userDao = new UserDao();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 验证Token获取用户ID
    	String authHeader = request.getHeader("Authorization");
    	if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    	    JsonUtil.sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未授权或Token格式错误");
    	    return;
    	}

    	// 提取真正的Token（去掉"Bearer "前缀）
    	String token = authHeader.substring(7);
    	if (!TokenUtil.verifyToken(token)) {
    	    JsonUtil.sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未授权或Token已过期");
    	    return;
    	}

    	String userId = TokenUtil.getUserIdFromToken(token);
        
        // 验证表单数据
        String itemName = request.getParameter("itemName");
        String category = request.getParameter("category");
        String lostTime = request.getParameter("lostTime");
        String lostLocation = request.getParameter("lostLocation");
        String description = request.getParameter("description");
        String contactInfo = request.getParameter("contactInfo");
        String isUrgentStr = request.getParameter("isUrgent");
        
        if (itemName == null || itemName.trim().isEmpty() ||
            lostTime == null || lostTime.trim().isEmpty() ||
            lostLocation == null || lostLocation.trim().isEmpty() ||
            contactInfo == null || contactInfo.trim().isEmpty()) {
            
            JsonUtil.sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, "必填字段不能为空");
            return;
        }
        
        boolean isUrgent = "true".equalsIgnoreCase(isUrgentStr);
        
        // 处理上传的图片
        List<String> imageUrls = new ArrayList<>();
        try {
            for (Part part : request.getParts()) {
                if (part.getName().equals("images") && part.getSize() > 0) {
                    String fileName = UUID.randomUUID().toString() + 
                                     getFileExtension(part.getSubmittedFileName());
                    String fileUrl = FileUploadUtil.uploadFile(part, fileName, "lost_items");
                    if (fileUrl != null) {
                        imageUrls.add(fileUrl);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                                    "图片上传失败: " + e.getMessage());
            return;
        }
        
        // 创建LostItem对象
        LostItem lostItem = new LostItem();
        lostItem.setId(UUID.randomUUID().toString().replace("-", ""));
        lostItem.setUserId(userId);
        lostItem.setItemName(itemName);
        lostItem.setDescription(description);
        lostItem.setCategory(category);
        lostItem.setLostLocation(lostLocation);
        lostItem.setLostTime(lostTime);
        lostItem.setContactInfo(contactInfo);
        lostItem.setPictureUrl(imageUrls.isEmpty() ? null : imageUrls.get(0));
        lostItem.setUrgent(isUrgent);
        lostItem.setFound(false);
        
        // 保存到数据库
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            
            // 检查用户是否存在
            if (!userDao.checkUserExists(conn, userId)) {
                JsonUtil.sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, "用户不存在");
                return;
            }
            
            // 插入失物信息
            boolean success = lostItemDao.insertLostItem(conn, lostItem);
            if (!success) {
                conn.rollback();
                JsonUtil.sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                                        "发布失物信息失败");
                return;
            }
            
            conn.commit();
            JsonUtil.sendJsonResponse(response, HttpServletResponse.SC_OK, "发布成功", lostItem);
            
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            JsonUtil.sendJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                                    "数据库错误: " + e.getMessage());
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }
}