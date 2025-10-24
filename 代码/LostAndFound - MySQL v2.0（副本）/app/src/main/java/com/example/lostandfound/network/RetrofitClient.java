package com.example.lostandfound.network;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.lostandfound.LoginActivity;
import com.example.lostandfound.utils.TokenUtils;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static final String BASE_URL = "http://172.20.10.6:8080/LostFoundServer/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            // 日志拦截器
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Cookie 管理器：保持 JSESSIONID
            CookieJar cookieJar = new CookieJar() {
                private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    cookieStore.put(url.host(), cookies);
                    for (Cookie cookie : cookies) {
                        Log.d("COOKIE_SAVE", cookie.toString());
                    }
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    List<Cookie> cookies = cookieStore.get(url.host());
                    if (cookies != null) {
                        for (Cookie cookie : cookies) {
                            Log.d("COOKIE_LOAD", cookie.toString());
                        }
                    }
                    return cookies != null ? cookies : new ArrayList<>();
                }
            };

            // 添加 Token 的拦截器
            Interceptor tokenInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Log.d("Retrofit", "Request URL: " + original.url());

                    // 跳过无需 Token 的请求（如登录/注册）
                    if (original.url().toString().contains("/login") ||
                            original.url().toString().contains("/register")){
                        return chain.proceed(original);
                    }

                    SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                    String token = prefs.getString("token", null);
                    Log.d("Retrofit", "从SharedPreferences获取的Token: " + token);

                    if (token == null || token.isEmpty() || TokenUtils.isTokenExpired(token)) {
                        // 跳转到登录页面
                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        throw new IOException("未授权：Token 无效或已过期");
                    }

                    // 确保Token有Bearer前缀
                    if (!token.startsWith("Bearer ")) {
                        token = "Bearer " + token;
                    }

                    Log.d("Retrofit", "添加前缀后的Token: " + token);
                    // 添加 Token 到请求头
                    Request newRequest = original.newBuilder()
                            .header("Authorization", token)
                            .header("Content-Type", "application/json")
                            .build();

                    // 打印所有请求头
                    Log.d("REQUEST_HEADERS", newRequest.headers().toString());
                    // 打印完整URL
                    Log.d("REQUEST_URL", newRequest.url().toString());

                    return chain.proceed(newRequest);
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(tokenInterceptor)
                    .cookieJar(cookieJar)
                    .connectTimeout(10, TimeUnit.SECONDS)   // 设置连接超时
                    .readTimeout(30, TimeUnit.SECONDS)      // 设置读取超时
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
