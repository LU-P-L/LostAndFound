package com.example.lostandfound;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {

    public static String getPath(Context context, Uri uri) {
        // 尝试通过内容提供者获取真实路径
        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        String fileName = cursor.getString(index);
                        File file = new File(context.getCacheDir(), fileName);

                        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                             FileOutputStream outputStream = new FileOutputStream(file)) {

                            byte[] buffer = new byte[4 * 1024];
                            int read;
                            while ((read = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, read);
                            }
                            outputStream.flush();
                            filePath = file.getAbsolutePath();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        return filePath;
    }
}