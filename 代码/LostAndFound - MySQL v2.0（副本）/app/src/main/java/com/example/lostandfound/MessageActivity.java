package com.example.lostandfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostandfound.network.ApiResponse;
import com.example.lostandfound.network.ApiService;
import com.example.lostandfound.network.RetrofitClient;
import com.example.lostandfound.utils.TokenUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://172.20.10.6:8080";
    private RecyclerView rvConversations;
    private ConversationAdapter adapter;
    private List<Conversation> conversationList = new ArrayList<>();
    private BottomNavigationView bottomNavigationView;
    private LinearLayout tvEmpty;
    TextView btnMarkRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_message);

        // 检查Token
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", null);
        if (token == null || TokenUtils.isTokenExpired(token)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        rvConversations = findViewById(R.id.rv_conversations);
        tvEmpty = findViewById(R.id.tv_empty);
        rvConversations.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConversationAdapter(conversationList);
        rvConversations.setAdapter(adapter);
        btnMarkRead = findViewById(R.id.btn_mark_read);
        btnMarkRead.setOnClickListener(v -> markAllMessagesAsRead());

        adapter.setOnItemClickListener(conversation -> {
            conversation.setUnreadCount(0);
            adapter.notifyDataSetChanged();

            Intent intent = new Intent(MessageActivity.this, ChatActivity.class);
            intent.putExtra("receiver_id", conversation.getUserId());
            intent.putExtra("receiver_name", conversation.getUsername());
            startActivity(intent);

        });

        loadConversations();
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // 设置选中项颜色
        bottomNavigationView.setItemIconTintList(null); // 禁用默认着色
        bottomNavigationView.setItemTextColor(null); // 禁用默认文字着色

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_message) {
                // 已经在首页
                return true;
            } else if (id == R.id.nav_publish) {
                startActivity(new Intent(MessageActivity.this, PublishActivity.class));
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(MessageActivity.this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MessageActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadConversations() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        String username = prefs.getString("username", "未知用户"); // 获取当前用户名

        if (userId == null) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<ApiResponse<List<Map<String, Object>>>> call = apiService.getConversations(userId);

        call.enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Map<String, Object>>>> call,
                                   Response<ApiResponse<List<Map<String, Object>>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    conversationList.clear();

                    for (Map<String, Object> serverConv : response.body().getData()) {
                        Conversation clientConv = new Conversation();

                        String otherUserId = (String) serverConv.get("otherUserId");
                        String otherUsername = (String) serverConv.get("otherUsername");

                        clientConv.setUserId(otherUserId);
                        clientConv.setUsername(otherUsername != null ? otherUsername : otherUserId);

                        String avatarUrl = (String) serverConv.get("avatarUrl");
                        String fullAvatarUrl = BASE_URL + avatarUrl;
                        if (avatarUrl != null) {
                            clientConv.setAvatarUrl(fullAvatarUrl);
                        }

                        String lastMessage = (String) serverConv.get("lastMessage");
                        String senderId = (String) serverConv.get("senderId");

                        if (userId.equals(senderId)) {
                            lastMessage = "我: " + lastMessage;
                        }
                        clientConv.setLastMessage(lastMessage);

                        Object countObj = serverConv.get("unreadCount");
                        if (countObj instanceof Number) {
                            clientConv.setUnreadCount(((Number) countObj).intValue());
                        }

                        clientConv.setUpdatedAt((String) serverConv.get("updatedAt"));

                        conversationList.add(clientConv);
                    }

                    // 按更新时间排序
                    Collections.sort(conversationList, (c1, c2) ->
                            c2.getUpdatedAt().compareTo(c1.getUpdatedAt()));

                    adapter.notifyDataSetChanged();

                    if (conversationList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(MessageActivity.this, "获取会话失败: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Map<String, Object>>>> call, Throwable t) {
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(MessageActivity.this, "加载会话失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("MessageActivity", "加载会话失败", t);
            }
        });
    }

    private void markAllMessagesAsRead() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        if (userId == null) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);

        // 创建请求体
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("userId", userId);

        Call<ApiResponse<Void>> call = apiService.markAllMessagesAsRead(requestBody);

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    // 本地更新所有未读计数
                    for (Conversation c : conversationList) {
                        c.setUnreadCount(0);
                    }
                    adapter.notifyDataSetChanged();
                    Toast.makeText(MessageActivity.this, "所有消息已标记为已读", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MessageActivity.this, "标记所有消息为已读失败: " +
                                    (response.body() != null ? response.body().getMessage() : "服务器错误"),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(MessageActivity.this, "标记所有消息为已读失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_message);
            loadConversations();
        }
    }
}