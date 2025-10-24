package com.example.lostandfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.lostandfound.network.ApiResponse;
import com.example.lostandfound.network.ApiService;
import com.example.lostandfound.network.RetrofitClient;
import com.example.lostandfound.utils.TokenUtils;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private ImageView ivBack;
    private TextView tvTitle;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private SwipeRefreshLayout swipeRefresh;

    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();

    private String currentUserId;
    private String receiverId;
    private String receiverName;
    private String conversationId;
    private List<Conversation> conversationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Chat", "aaaaaa ");
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            Log.d("Chat", "bbbbb ");
            getSupportActionBar().hide();
        }
        Log.d("Chat", "cccccc ");
        setContentView(R.layout.activity_chat);

        Log.d("Chat", "dddddd ");
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        currentUserId = sharedPreferences.getString("userId", null);
        Log.d("Chat", "currentUserId: "+currentUserId);

        if (currentUserId == null) {
            Log.d("Chat", "eeeee ");
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 获取传递过来的用户信息
        receiverId = getIntent().getStringExtra("receiver_id");
        Log.d("Chat", "receiverId: "+receiverId);
        receiverName = getIntent().getStringExtra("receiver_name");
        Log.d("Chat", "receiverName: "+receiverName);

        if (receiverId == null || receiverId.isEmpty()) {
            Toast.makeText(this, "无效的接收者ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 在调用跳转的地方添加以下检查
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", null);
        Log.d("Chat", "token: "+token);
        String userId = prefs.getString("userId", null);
        Log.d("Chat", "userId: "+userId);

        if (token == null || userId == null || TokenUtils.isTokenExpired(token)) {
            // 跳转到登录页面
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        // 确保receiverId和receiverName不为空
        if (receiverId == null || receiverId.isEmpty()) {
            Toast.makeText(this, "无效的接收者ID", Toast.LENGTH_SHORT).show();
            return;
        }

        initViews();

        if (receiverName != null && !receiverName.isEmpty()) {
            tvTitle.setText(receiverName);
        } else {
            tvTitle.setText("私信");
        }

        // 生成会话ID
        if (currentUserId.compareTo(receiverId) < 0) {
            conversationId = currentUserId + "_" + receiverId;
        } else {
            conversationId = receiverId + "_" + currentUserId;
        }

        loadMessages();

        swipeRefresh.setOnRefreshListener(this::loadMessages);
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        tvTitle = findViewById(R.id.tv_title);

        ivBack.setOnClickListener(v -> finish());

        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        rvMessages.setAdapter(messageAdapter);
    }

    private void loadMessages() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<ApiResponse<List<Message>>> call = apiService.getMessagesByConversation(conversationId);

        call.enqueue(new Callback<ApiResponse<List<Message>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Message>>> call, Response<ApiResponse<List<Message>>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    List<Message> messages = response.body().getData();

                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    String currentUsername = prefs.getString("username", "我");
                    String currentAvatarUrl = prefs.getString("avatarUrl", null);

                    for (Message message : messages) {
                        if (message.getSenderId() != null) {
                            if (message.getSenderId().equals(currentUserId)) {
                                // 当前用户发送的消息
                                User sender = new User(currentUserId, currentUsername);
                                sender.setAvatarUrl(currentAvatarUrl != null ? currentAvatarUrl : message.getSenderAvatar());
                                message.setSender(sender);

                                User receiver = new User(receiverId, receiverName);
                                receiver.setAvatarUrl(message.getReceiverAvatar());
                                message.setReceiver(receiver);
                            } else {
                                // 接收到的消息
                                User sender = new User(receiverId, receiverName);
                                sender.setAvatarUrl(message.getSenderAvatar());
                                message.setSender(sender);

                                User receiver = new User(currentUserId, currentUsername);
                                receiver.setAvatarUrl(currentAvatarUrl);
                                message.setReceiver(receiver);
                            }
                        }
                    }

                    List<Message> messagesWithTime = insertTimeSeparators(messages);
                    messageList.clear();
                    messageList.addAll(messagesWithTime);
                    messageAdapter.notifyDataSetChanged();

                    if (!messageList.isEmpty()) {
                        rvMessages.smoothScrollToPosition(messageList.size() - 1);
                    }

                    markMessagesAsRead();
                } else {
                    Toast.makeText(ChatActivity.this, "加载消息失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Message>>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(ChatActivity.this, "加载消息失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 插入时间分隔项
    private List<Message> insertTimeSeparators(List<Message> messages) {
        List<Message> result = new ArrayList<>();
        if (messages == null || messages.isEmpty()) {
            return result;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String lastDate = "";

        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            String messageDate = getDatePart(message.getCreatedAt());

            if (i == 0 || !messageDate.equals(lastDate)) {
                Message timeSeparator = createTimeSeparator(message.getCreatedAt());
                result.add(timeSeparator);
                lastDate = messageDate;
            }

            result.add(message);
        }

        return result;
    }


    // 提取日期部分
    private String getDatePart(String dateTime) {
        if (dateTime == null || dateTime.length() < 10) return "";
        return dateTime.substring(0, 10);
    }

    // 创建时间分隔项消息
    private Message createTimeSeparator(String dateTime) {
        Message separator = new Message();
        separator.setContent("TIME_SEPARATOR" + formatDateForDisplay(dateTime));
        return separator;
    }

    // 格式化日期显示
    private String formatDateForDisplay(String dateTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateTime);

            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateTime.substring(0, 10);
        }
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "消息不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 获取当前用户信息
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String currentUsername = prefs.getString("username", "我");
        String currentAvatarUrl = prefs.getString("avatarUrl", null);
        Log.d("Chat","currentAvatarUrl: "+currentAvatarUrl);

        // 创建消息对象并设置完整信息
        Message message = new Message(currentUserId, receiverId, content);
        message.setConversationId(conversationId);

        // 设置发送者信息（适配MessageAdapter的显示逻辑）
        User sender = new User(currentUserId, currentUsername);
        sender.setAvatarUrl(currentAvatarUrl);
        message.setSender(sender);

        // 设置接收者信息
        User receiver = new User(receiverId, receiverName);
        message.setReceiver(receiver);

        // 同时设置直接的头像URL字段
        if (currentAvatarUrl != null) {
            message.setSenderAvatar(currentAvatarUrl);
        } else {
            message.setSenderAvatar("");
        }

        // 先添加到本地列表（优化用户体验）
        message.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        messageList.add(message);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        rvMessages.smoothScrollToPosition(messageList.size() - 1);
        etMessage.setText("");

        // 发送到服务器
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<ApiResponse<Message>> call = apiService.sendMessage(message);
        call.enqueue(new Callback<ApiResponse<Message>>() {
            @Override
            public void onResponse(Call<ApiResponse<Message>> call, Response<ApiResponse<Message>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getCode() != 200) {
                    // 发送失败，移除本地添加的消息
                    messageList.remove(message);
                    messageAdapter.notifyItemRemoved(messageList.size());
                    Toast.makeText(ChatActivity.this, "发送失败: " + (response.body() != null ?
                            response.body().getMessage() : "网络错误"), Toast.LENGTH_SHORT).show();
                }
                // 发送成功不需要额外处理，因为消息已经显示
            }

            @Override
            public void onFailure(Call<ApiResponse<Message>> call, Throwable t) {
                // 发送失败，移除本地添加的消息
                messageList.remove(message);
                messageAdapter.notifyItemRemoved(messageList.size());
                Toast.makeText(ChatActivity.this, "发送失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markMessagesAsRead() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        if (userId == null || conversationId == null) {
            return;
        }

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);

        // 创建请求体
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("conversationId", conversationId);
        requestBody.addProperty("userId", userId);

        Call<ApiResponse<Void>> call = apiService.markMessagesAsRead(requestBody);

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    Log.d("ChatActivity", "标记已读成功");
                    // 更新本地未读计数
                    for (Conversation c : conversationList) {
                        if (c.getConversationId().equals(conversationId)) {
                            c.setUnreadCount(0);
                            break;
                        }
                    }
                } else {
                    Log.e("ChatActivity", "标记已读失败: " + (response.body() != null ? response.body().getMessage() : "未知错误"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Log.e("ChatActivity", "标记已读失败: " + t.getMessage());
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadMessages();
    }

}