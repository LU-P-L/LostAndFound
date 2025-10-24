package com.example.lostandfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        EditText etUsername = findViewById(R.id.et_username);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            String phone = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(LoginActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                return;
            }

            String loginUrl = "http://172.20.10.6:8080/LostFoundServer/login";

            new Thread(() -> {
                try {
                    // 构建参数
                    String params = "phone=" + phone + "&password=" + password;
                    Log.d("Login",params);

                    // 建立连接
                    URL url = new URL(loginUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    Log.d("Login", "aaaaaa");
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(100000);
                    conn.setReadTimeout(100000);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    Log.d("Login", "bbbbbb");

                    // 发送参数
                    OutputStream os = conn.getOutputStream();
                    Log.d("Login", "ccccc");
                    os.write(params.getBytes());
                    os.flush();
                    os.close();
                    Log.d("Login", "cccccc");

                    // 读取返回数据
                    InputStream is = conn.getInputStream();
                    Log.d("Login", "dddddd");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                        Log.d("Login", "eeeeee");
                    }
                    Log.d("Login", "ffffff");

                    reader.close();
                    is.close();
                    conn.disconnect();

                    try {
                        // 解析 JSON
                        Log.d("Login", "gggggg");
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        Log.d("Login", "hhhhhh");
                        String status = jsonResponse.getString("status");
                        Log.d("Login", "iiiiii");

                        runOnUiThread(() -> {
                            try {
                                if (status.equals("success")) {
                                    // 登录成功，存储登录状态
                                    SharedPreferences.Editor editor = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit();
                                    editor.putBoolean(KEY_IS_LOGGED_IN, true);
                                    editor.putString("userId", jsonResponse.getJSONObject("user").optString("id"));
                                    editor.putString("username", jsonResponse.getJSONObject("user").optString("username"));
                                    editor.putString("phone", jsonResponse.getJSONObject("user").optString("phone"));
                                    String relativeUrl = jsonResponse.getJSONObject("user").optString("avatar_url");
                                    String baseUrl = "http://172.20.10.6:8080";
                                    String fullAvatarUrl = baseUrl + relativeUrl;
                                    editor.putString("avatarUrl", fullAvatarUrl);

                                    String token = jsonResponse.optString("token");
                                    Log.d("Login", "从服务器获取的Token: " + token);
                                    editor.putString("token", token);
                                    boolean success = editor.commit();
                                    Log.d("Login", "存储Token结果: " + success + ", 已存储Token到SharedPreferences");

                                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();

                                    // 跳转主页面
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception uiEx) {
                                uiEx.printStackTrace();
                                Toast.makeText(LoginActivity.this, "解析响应失败", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (Exception jsonEx) {
                        jsonEx.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "服务器返回格式错误", Toast.LENGTH_SHORT).show());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        // 添加注册跳转
        TextView tvRegister = findViewById(R.id.tv_register);
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
}