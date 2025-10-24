package com.example.lostandfound;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.lostandfound.network.ApiService;
import com.example.lostandfound.network.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LostItemAdapter adapter;
    private List<LostItem> lostItemList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView bottomNavigationView;
    private Parcelable listState;
    private boolean isLoading = false;

    // 筛选条件变量
    private String selectedCategory = "";
    private String timeRange = "all";
    private boolean urgentOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        // 初始化视图
        initViews();

        // 设置下拉刷新
        setupSwipeRefresh();

        // 设置底部导航栏
        setupBottomNavigation();

        // 加载失物信息
        loadLostItems();

        // 设置搜索框点击事件
        setupSearchView();

        // 设置筛选按钮点击事件
        setupFilterButton();
    }

    private void setupFilterButton() {
        Button btnFilter = findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);

        // 初始化对话框中的视图
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        RadioGroup radioGroupTime = dialogView.findViewById(R.id.radioGroupTime);
        CheckBox cbUrgentOnly = dialogView.findViewById(R.id.cbUrgentOnly);
        Button btnReset = dialogView.findViewById(R.id.btnReset);
        Button btnApply = dialogView.findViewById(R.id.btnApply);

        // 设置当前筛选条件
        if (!selectedCategory.isEmpty()) {
            String[] categories = getResources().getStringArray(R.array.item_categories);
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(selectedCategory)) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }

        switch (timeRange) {
            case "today":
                radioGroupTime.check(R.id.radioToday);
                break;
            case "week":
                radioGroupTime.check(R.id.radioWeek);
                break;
            case "month":
                radioGroupTime.check(R.id.radioMonth);
                break;
            default:
                radioGroupTime.check(R.id.radioAllTime);
        }

        cbUrgentOnly.setChecked(urgentOnly);

        AlertDialog dialog = builder.create();

        btnReset.setOnClickListener(v -> {
            // 重置筛选条件
            spinnerCategory.setSelection(0);
            radioGroupTime.check(R.id.radioAllTime);
            cbUrgentOnly.setChecked(false);
        });

        btnApply.setOnClickListener(v -> {
            selectedCategory = spinnerCategory.getSelectedItem().toString();
            if (selectedCategory.equals("全部类别")) {
                selectedCategory = "";
            }

            int selectedTimeId = radioGroupTime.getCheckedRadioButtonId();
            if (selectedTimeId == R.id.radioToday) {
                timeRange = "today";
            } else if (selectedTimeId == R.id.radioWeek) {
                timeRange = "week";
            } else if (selectedTimeId == R.id.radioMonth) {
                timeRange = "month";
            } else {
                timeRange = "all";
            }

            urgentOnly = cbUrgentOnly.isChecked();

            // 应用筛选条件并刷新数据
            loadLostItems();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupSearchView() {
        LinearLayout searchLayout = findViewById(R.id.searchLayout);
        searchLayout.setOnClickListener(v -> {
            // 跳转到搜索页面
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LostItemAdapter(lostItemList, new LostItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(LostItem item) {
                // 保存 RecyclerView 状态
                listState = recyclerView.getLayoutManager().onSaveInstanceState();

                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("lost_item", item.getId());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        // 设置滚动加载更多
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();

                if (lastVisibleItem >= totalItemCount - 3) {
                    // 加载更多数据
                    loadMoreItems();
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // 刷新数据
            refreshItems();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // 设置选中项颜色
        bottomNavigationView.setItemIconTintList(null); // 禁用默认着色
        bottomNavigationView.setItemTextColor(null); // 禁用默认文字着色

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // 已经在首页
                return true;
            } else if (id == R.id.nav_publish) {
                startActivity(new Intent(MainActivity.this, PublishActivity.class));
                return true;
            } else if (id == R.id.nav_message) {
                startActivity(new Intent(MainActivity.this, MessageActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // 刷新数据前保存当前状态（避免状态还原错位）
        final Parcelable tempState = listState;

        refreshItems();

        recyclerView.postDelayed(() -> {
            if (tempState != null) {
                recyclerView.getLayoutManager().onRestoreInstanceState(tempState);
            }
        }, 300); // 延迟恢复，确保数据加载完成后执行
    }

    private void loadLostItems() {
        if (isLoading) return;
        isLoading = true;

        // 使用Retrofit代替直接OkHttp调用
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<List<LostItem>> call = apiService.getLostItemsWithFilters(
                selectedCategory.isEmpty() ? null : selectedCategory,
                timeRange.equals("all") ? null : timeRange,
                urgentOnly ? "true" : null
        );

        call.enqueue(new retrofit2.Callback<List<LostItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<LostItem>> call, @NonNull retrofit2.Response<List<LostItem>> response) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    lostItemList.clear();
                    lostItemList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "加载失败，服务器错误", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<LostItem>> call, @NonNull Throwable t) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "加载失败：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshItems() {
        if (isLoading) return;
        isLoading = true;
        swipeRefreshLayout.setRefreshing(true);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<List<LostItem>> call = apiService.getLostItemsWithFilters(
                selectedCategory.isEmpty() ? null : selectedCategory,
                timeRange.equals("all") ? null : timeRange,
                urgentOnly ? "true" : null
        );

        call.enqueue(new retrofit2.Callback<List<LostItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<LostItem>> call, @NonNull retrofit2.Response<List<LostItem>> response) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    lostItemList.clear();
                    lostItemList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "刷新失败，服务器错误", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<LostItem>> call, @NonNull Throwable t) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "刷新失败：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMoreItems() {
        refreshItems();
    }
}