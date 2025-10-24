package utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import jakarta.servlet.http.Part;

public class FileUtil {
    // 修改为相对路径，由Servlet根据请求动态生成完整URL
	private static final String UPLOAD_DIR = "D:/uploads/";
    
    public static String saveUploadedFile(Part filePart, String subDir) throws IOException {
        String fileName = UUID.randomUUID().toString() + 
                getFileExtension(filePart.getSubmittedFileName());
        
        File uploadDir = new File(UPLOAD_DIR + subDir);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        String filePath = UPLOAD_DIR + subDir + "/" + fileName;
        try (InputStream fileContent = filePart.getInputStream()) {
            Files.copy(fileContent, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        }
        
        // 只返回相对路径，完整URL由Servlet统一生成
        return "/uploads/" + subDir + "/" + fileName;
    }
    
    private static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}