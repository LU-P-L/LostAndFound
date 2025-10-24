package com.example.lostandfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.lostandfound.network.ApiResponse;
import com.example.lostandfound.network.ApiService;
import com.example.lostandfound.network.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private TextView tvUsername, tvPhone, tvEmail;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_my);

        initViews();
        loadUserData();
        setupBottomNavigation();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.iv_avatar);
        tvUsername = findViewById(R.id.tv_username);
        tvPhone = findViewById(R.id.tv_phone);
        tvEmail = findViewById(R.id.tv_email);

        // 设置点击事件
        findViewById(R.id.card_lost_info).setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, MyLostItemsActivity.class)));

        findViewById(R.id.card_personal_info).setOnClickListener(v ->
                startActivityForResult(new Intent(ProfileActivity.this, EditProfileActivity.class), 100)
        );
        findViewById(R.id.card_change_pwd).setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class)));

        findViewById(R.id.card_about_us).setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, AboutUsActivity.class)));

        findViewById(R.id.card_help).setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, HelpActivity.class)));

        findViewById(R.id.card_logout).setOnClickListener(v -> logout());
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        // 禁用默认着色
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setItemTextColor(null);

        // 设置当前选中项
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                navigateToMainActivity();
                return true;
            } else if (id == R.id.nav_publish) {
                startActivity(new Intent(this, PublishActivity.class));
                return true;
            } else if (id == R.id.nav_message) {
                startActivity(new Intent(this, MessageActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                // 已经在个人页面，无需操作
                return true;
            }
            return false;
        });
    }

    // 特殊处理返回首页的逻辑
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        if (userId == null) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<ApiResponse<JsonObject>> call = apiService.getUserProfile(userId);

        call.enqueue(new Callback<ApiResponse<JsonObject>>() {
            @Override
            public void onResponse(Call<ApiResponse<JsonObject>> call, Response<ApiResponse<JsonObject>> response) {
                Log.d("Profile","response: "+response);
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    JsonObject userData = response.body().getData();
                    updateUIWithUserData(userData);
                } else {
                    Toast.makeText(ProfileActivity.this, "获取用户信息失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<JsonObject>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithUserData(JsonObject userData) {
        tvUsername.setText(userData.has("username") ? userData.get("username").getAsString() : "未设置");
        tvPhone.setText(userData.has("phone") ? userData.get("phone").getAsString() : "未设置");
        tvEmail.setText(userData.has("email") ? userData.get("email").getAsString() : "未设置");

        if (userData.has("avatarUrl") && !userData.get("avatarUrl").isJsonNull()) {
            String avatarUrl = userData.get("avatarUrl").getAsString();

            if (!avatarUrl.startsWith("http")) {
                avatarUrl = "http://172.20.10.6:8080" + avatarUrl;
            }

            Log.d("ProfileActivity", "最终加载的头像URL: " + avatarUrl);

            Glide.with(ProfileActivity.this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }
    }

    private void logout() {
        // 调用服务器登出API
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("action", "logout");

        Call<ApiResponse<Void>> call = apiService.logout(requestBody);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                performLocalLogout();
                // 确保跳转到WelcomeActivity并清除返回栈
                Intent intent = new Intent(ProfileActivity.this, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                performLocalLogout();
                // 即使网络失败也跳转到WelcomeActivity
                Intent intent = new Intent(ProfileActivity.this, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void performLocalLogout() {
        // 清除所有登录状态和Token
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.remove("auth_token"); // 清除Token
        editor.remove("user_id"); // 清除其他用户信息（如果有）
        editor.apply();

        // 可选：清除Cookie或WebView缓存（如果有）
        CookieManager.getInstance().removeAllCookies(null);
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        // 确保导航栏选中状态正确
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        }
    }

    // 添加结果处理回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            try {
                String updatedUserDataStr = data.getStringExtra("updatedUserData");
                if (updatedUserDataStr != null) {
                    JsonObject updatedUserData = new JsonParser().parse(updatedUserDataStr).getAsJsonObject();
                    updateUIWithUserData(updatedUserData);

                    // 更新头像，带时间戳强制刷新一次
                    if (updatedUserData.has("avatarUrl")) {
                        String newAvatarUrl = updatedUserData.get("avatarUrl").getAsString();
                        if (!newAvatarUrl.startsWith("http")) {
                            newAvatarUrl = "http://172.20.10.6:8080" + newAvatarUrl;
                        }
                        newAvatarUrl += "?t=" + System.currentTimeMillis();

                        Glide.with(this)
                                .load(newAvatarUrl)
                                .placeholder(R.drawable.ic_default_avatar)
                                .error(R.drawable.ic_default_avatar)
                                .into(ivAvatar);
                    }

                    // 更新本地存储的用户信息
                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    if (updatedUserData.has("username")) {
                        editor.putString("username", updatedUserData.get("username").getAsString());
                    }
                    editor.apply();
                }
            } catch (Exception e) {
                Log.e("ProfileActivity", "解析更新数据失败", e);
                // 如果解析失败，仍然重新加载数据
                loadUserData();
            }
        }
    }
}