package utils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletResponse;

public class JsonUtil {
    private static final Gson gson = new Gson();
    
    public static void sendJsonResponse(HttpServletResponse response, int statusCode, String message) 
            throws IOException {
        sendJsonResponse(response, statusCode, message, null);
    }
    
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
    
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }
    
    public static <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }
    
    public static void sendJsonResponse(HttpServletResponse response, int statusCode, 
                                       String message, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("code", statusCode);
        responseMap.put("message", message);
        if (data != null) {
            responseMap.put("data", data);
        }
        
        response.getWriter().write(gson.toJson(responseMap));
    }
}