package com.example.lostandfound;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {
    private RecyclerView rvConversations;
    private ConversationAdapter adapter;
    private List<Conversation> conversationList = new ArrayList<>();
    private BottomNavigationView bottomNavigationView;
    private LinearLayout tvEmpty;
    TextView btnMarkRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_message);

        // 初始化视图
        rvConversations = findViewById(R.id.rv_conversations);
        tvEmpty = findViewById(R.id.tv_empty); // 初始化空列表提示
        rvConversations.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConversationAdapter(conversationList);
        rvConversations.setAdapter(adapter);
        btnMarkRead = findViewById(R.id.btn_mark_read);
        btnMarkRead.setOnClickListener(v -> markAllMessagesAsRead());

        adapter.setOnItemClickListener(new ConversationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Conversation conversation) {
                Intent intent = new Intent(MessageActivity.this, ChatActivity.class);
                intent.putExtra("receiver_id", conversation.getUserId());
                intent.putExtra("receiver_name", conversation.getUsername());
                startActivity(intent);
            }
        });

        // 加载会话列表
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
        User currentUser = BmobUser.getCurrentUser(User.class);
        if (currentUser == null) return;

        // 查询当前用户发送或接收的所有消息
        BmobQuery<Message> sentQuery = new BmobQuery<>();
        sentQuery.addWhereEqualTo("sender", currentUser);

        BmobQuery<Message> receivedQuery = new BmobQuery<>();
        receivedQuery.addWhereEqualTo("receiver", currentUser);

        // 合并两个查询
        List<BmobQuery<Message>> queries = new ArrayList<>();
        queries.add(sentQuery);
        queries.add(receivedQuery);

        BmobQuery<Message> mainQuery = new BmobQuery<>();
        mainQuery.or(queries);
        mainQuery.include("sender,receiver");
        mainQuery.order("-createdAt"); // 按时间降序排列

        mainQuery.findObjects(new FindListener<Message>() {
            @Override
            public void done(List<Message> messages, BmobException e) {
                if (e == null) {
                    Map<String, Conversation> conversationMap = new HashMap<>();
                    String currentUserId = currentUser.getObjectId(); // 获取当前用户ID

                    for (Message message : messages) {
                        User sender = message.getSender();
                        User receiver = message.getReceiver();

                        String otherUserId = sender.getObjectId().equals(currentUserId)
                                ? receiver.getObjectId()
                                : sender.getObjectId();

                        String otherUsername = sender.getObjectId().equals(currentUserId)
                                ? receiver.getUsername()
                                : sender.getUsername();

                        if (!conversationMap.containsKey(otherUserId)) {
                            Conversation conversation = new Conversation();
                            conversation.setUserId(otherUserId);
                            conversation.setUsername(otherUsername);
                            conversation.setLastMessage(message.getContent());
                            conversation.setUnreadCount(0);
                            conversationMap.put(otherUserId, conversation);
                        }

                        // 更新未读计数
                        if (!message.isRead() && receiver.getObjectId().equals(currentUserId)) {
                            Conversation conv = conversationMap.get(otherUserId);
                            conv.setUnreadCount(conv.getUnreadCount() + 1);
                        }
                    }

                    conversationList.clear();
                    conversationList.addAll(conversationMap.values());
                    adapter.notifyDataSetChanged();

                    // 检查是否有会话
                    if (conversationList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void markAllMessagesAsRead() {
        User currentUser = BmobUser.getCurrentUser(User.class);
        if (currentUser == null) return;

        BmobQuery<Message> query = new BmobQuery<>();
        query.addWhereEqualTo("receiver", currentUser);
        query.addWhereEqualTo("isRead", false);
        query.findObjects(new FindListener<Message>() {
            @Override
            public void done(List<Message> unreadMessages, BmobException e) {
                if (e == null && unreadMessages != null && !unreadMessages.isEmpty()) {
                    for (Message message : unreadMessages) {
                        message.setRead(true);
                    }

                    // 转换 List<Message> 为 List<BmobObject>
                    List<BmobObject> objects = new ArrayList<>();
                    objects.addAll(unreadMessages);

                    new BmobBatch().updateBatch(objects).doBatch(new QueryListListener<BatchResult>() {
                        @Override
                        public void done(List<BatchResult> list, BmobException e) {
                            if (e == null) {
                                loadConversations(); // 刷新列表
                            }
                        }
                    });
                }
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        // 设置当前选中的导航项为消息
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_message);
            // 刷新未读消息计数
            loadConversations();
        }
    }
}