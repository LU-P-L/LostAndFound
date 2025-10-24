package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUploadUtil {
    private static final String BASE_UPLOAD_PATH = "D:/uploads";

    public static String uploadFile(jakarta.servlet.http.Part part, String fileName, String subFolder) throws IOException {
        // 构建上传目录
        String uploadDir = BASE_UPLOAD_PATH + File.separator + subFolder;
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 构建完整路径
        String filePath = uploadDir + File.separator + fileName;
        File file = new File(filePath);

        // 写入文件
        try (InputStream input = part.getInputStream();
             FileOutputStream output = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
        }

        // 返回相对路径或完整 URL
        return "/uploads/" + subFolder + "/" + fileName;
    }
}
