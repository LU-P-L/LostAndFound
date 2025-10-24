package com.example.lostandfound.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

public class TokenUtils {
    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return prefs.getString("token", null);
    }

    public static boolean isTokenValid(Context context) {
        String token = getToken(context);
        if (token == null || token.isEmpty()) {
            return false;
        }
        return !isTokenExpired(token);
    }

    public static boolean isTokenExpired(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE), "UTF-8");
            JSONObject payloadJson = new JSONObject(payload);
            long exp = payloadJson.getLong("exp");
            long currentTime = System.currentTimeMillis() / 1000;
            return currentTime >= exp;
        } catch (Exception e) {
            e.printStackTrace();
            return true; // 如果解析失败，视为过期
        }
    }

    public static void clearToken(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).edit();
        editor.remove("token");
        editor.apply();
    }
}