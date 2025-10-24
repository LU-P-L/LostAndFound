package com.example.lostandfound;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class MyLostItemsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyLostItemAdapter adapter;
    private List<LostItem> lostItems = new ArrayList<>();
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_my_lost_items);

        initViews();
        loadLostItems();
    }

    private void initViews() {
        // 初始化返回按钮
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish()); // 设置点击事件

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MyLostItemAdapter(lostItems, true);
        recyclerView.setAdapter(adapter);

        // 设置整个条目点击监听（跳转到详情页）
        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(MyLostItemsActivity.this, DetailActivity.class);
            intent.putExtra("lost_item", item.getObjectId()); // 传递失物ID
            startActivity(intent);
        });

        // 设置状态区域点击监听（切换找回状态）
        adapter.setOnStatusClickListener(position -> {
            LostItem item = lostItems.get(position);
            item.setFound(!item.getFound());
            updateItemStatus(item);
        });
    }

    private void loadLostItems() {
        User currentUser = BmobUser.getCurrentUser(User.class);
        if (currentUser == null) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        BmobQuery<LostItem> query = new BmobQuery<>();
        query.addWhereEqualTo("owner", currentUser);
        query.order("-createdAt");
        query.findObjects(new FindListener<LostItem>() {
            @Override
            public void done(List<LostItem> list, BmobException e) {
                if (e == null) {
                    lostItems.clear();
                    lostItems.addAll(list);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MyLostItemsActivity.this, "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateItemStatus(LostItem item) {
        item.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MyLostItemsActivity.this, "更新状态失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}