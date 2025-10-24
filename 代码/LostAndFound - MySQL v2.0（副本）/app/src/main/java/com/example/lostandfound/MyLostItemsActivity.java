package com.example.lostandfound;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostandfound.network.ApiService;
import com.example.lostandfound.network.RetrofitClient;
import com.example.lostandfound.network.UpdateStatusRequest;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyLostItemsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyLostItemAdapter adapter;
    private List<LostItem> lostItems = new ArrayList<>();
    private ImageView ivBack;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_my_lost_items);

        apiService = RetrofitClient.getClient(this).create(ApiService.class);

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
            intent.putExtra("lost_item", item.getId()); // 传递失物ID
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
        Call<List<LostItem>> call = apiService.getMyLostItems();
        call.enqueue(new Callback<List<LostItem>>() {
            @Override
            public void onResponse(Call<List<LostItem>> call, Response<List<LostItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    lostItems.clear();
                    lostItems.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MyLostItemsActivity.this, "加载失败: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<LostItem>> call, Throwable t) {
                Toast.makeText(MyLostItemsActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateItemStatus(LostItem item) {
        UpdateStatusRequest request = new UpdateStatusRequest(item.getId(), item.getFound());

        Call<JsonObject> call = apiService.updateItemStatus(request);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    adapter.notifyDataSetChanged();
                    Toast.makeText(MyLostItemsActivity.this, "状态更新成功", Toast.LENGTH_SHORT).show();
                } else {
                    // 回滚状态改变，因为更新失败
                    item.setFound(!item.getFound());
                    adapter.notifyDataSetChanged();
                    Toast.makeText(MyLostItemsActivity.this, "更新失败: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                // 回滚状态改变，因为网络错误
                item.setFound(!item.getFound());
                adapter.notifyDataSetChanged();
                Toast.makeText(MyLostItemsActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}