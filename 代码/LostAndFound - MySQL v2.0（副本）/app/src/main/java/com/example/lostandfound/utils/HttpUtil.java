package com.example.lostandfound.utils;

import java.io.IOException;
import okhttp3.*;

public class HttpUtil {
    private static final OkHttpClient client = new OkHttpClient();

    public static void post(String url, RequestBody body, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }
}
