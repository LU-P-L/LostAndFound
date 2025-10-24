package com.example.lostandfound;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobPushManager;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.PushListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class ChatActivity extends AppCompatActivity {
    private ImageView ivBack;
    private TextView tvTitle;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private SwipeRefreshLayout swipeRefresh;

    private MessageAdapter messageAdapter;
    private List<Message> messageList = new ArrayList<>();

    private User currentUser;
    private User receiver;
    private String conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_chat);

        // 获取当前用户
        currentUser = BmobUser.getCurrentUser(User.class);
        if (currentUser == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 获取传递过来的用户信息
        String receiverId = getIntent().getStringExtra("receiver_id");
        markMessagesAsRead();
        String receiverName = getIntent().getStringExtra("receiver_name");

        if (receiverId == null || receiverId.isEmpty()) {
            Toast.makeText(this, "无效的接收者ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化视图
        initViews();

        // 设置标题（使用安全值）
        if (receiverName != null && !receiverName.isEmpty()) {
            tvTitle.setText(receiverName);
        } else {
            tvTitle.setText("私信");
        }

        // 加载接收者用户信息
        loadReceiverInfo(receiverId);

        // 设置刷新监听
        swipeRefresh.setOnRefreshListener(this::loadMessages);

        // 发送按钮点击事件
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
        messageAdapter = new MessageAdapter(messageList, currentUser.getObjectId());
        rvMessages.setAdapter(messageAdapter);
    }

    private void loadReceiverInfo(String receiverId) {
        BmobQuery<User> query = new BmobQuery<>();
        query.getObject(receiverId, new QueryListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if (e == null && user != null) {
                    receiver = user;

                    // 更新标题
                    runOnUiThread(() -> {
                        tvTitle.setText(user.getUsername());
                    });

                    // 生成会话ID
                    if (currentUser.getObjectId().compareTo(receiverId) < 0) {
                        conversationId = currentUser.getObjectId() + "_" + receiverId;
                    } else {
                        conversationId = receiverId + "_" + currentUser.getObjectId();
                    }

                    loadMessages();
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(ChatActivity.this,
                                "加载用户失败: " + (e != null ? e.getMessage() : "用户不存在"),
                                Toast.LENGTH_LONG).show();

                        // 不立即finish，允许用户重试
                        btnSend.setEnabled(false);
                        etMessage.setHint("无法加载聊天对象");
                    });
                }
            }
        });
    }

    private void loadMessages() {
        if (receiver == null) return;

        BmobQuery<Message> query = new BmobQuery<>();
        query.addWhereEqualTo("conversationId", conversationId);
        query.order("createdAt");
        query.include("sender,receiver");

        query.findObjects(new FindListener<Message>() {
            @Override
            public void done(List<Message> messages, BmobException e) {
                swipeRefresh.setRefreshing(false);
                if (e == null) {
                    // 添加时间分隔项
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

            // 如果是第一条消息或日期变化，添加时间分隔项
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
        return dateTime.substring(0, 10); // 提取"YYYY-MM-DD"部分
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
            return dateTime.substring(0, 10); // 失败时返回原始日期部分
        }
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "消息不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建消息对象
        Message message = new Message(currentUser, receiver, content);
        message.setConversationId(conversationId);

        // 保存到Bmob
        message.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    etMessage.setText("");
                    // 添加到列表并刷新
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);
                    rvMessages.smoothScrollToPosition(messageList.size() - 1);

                    // 发送推送通知
                    sendPushNotification();
                } else {
                    Toast.makeText(ChatActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void markMessagesAsRead() {
        for (Message message : messageList) {
            // 跳过时间分隔项
            if (message.getContent() != null && message.getContent().startsWith("TIME_SEPARATOR")) {
                continue;
            }

            if (message.getReceiver().getObjectId().equals(currentUser.getObjectId()) && !message.isRead()) {
                message.setRead(true);
                message.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e != null) {
                            Log.e("ChatActivity", "标记已读失败: " + e.getMessage());
                        }
                    }
                });
            }
        }
    }

    private void sendPushNotification() {
        try {
            // 创建推送内容
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("alert", currentUser.getUsername() + ": " + etMessage.getText().toString());
            jsonObject.put("title", "新消息");
            jsonObject.put("conversationId", conversationId);
            jsonObject.put("senderId", currentUser.getObjectId());

            // 使用Bmob推送服务发送通知
            BmobPushManager pushManager = new BmobPushManager();

            // 设置推送条件
            BmobQuery<BmobInstallation> query = BmobInstallation.getQuery();
            query.addWhereEqualTo("userId", receiver.getObjectId());

            // 执行推送
            pushManager.setQuery(query);
            pushManager.pushMessage(jsonObject, new PushListener() {
                @Override
                public void done(BmobException e) {
                    if (e != null) {
                        Log.e("BmobPush", "推送失败: " + e.getMessage());
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("PushNotification", "创建推送JSON时出错: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次进入聊天界面时加载消息
        loadMessages();
    }
}