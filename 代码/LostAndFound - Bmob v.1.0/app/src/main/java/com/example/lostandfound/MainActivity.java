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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

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
            // 获取筛选条件
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
            refreshItems();
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
                intent.putExtra("lost_item", item.getObjectId());
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
        BmobQuery<LostItem> query = new BmobQuery<>();
        query.include("owner");
        query.order("-createdAt"); // 按创建时间降序排列
        query.setLimit(10); // 每次加载10条数据

        // 添加筛选条件
        applyFilters(query);

        query.findObjects(new FindListener<LostItem>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void done(List<LostItem> list, BmobException e) {
                if (e == null) {
                    lostItemList.clear();
                    lostItemList.addAll(list);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void refreshItems() {
        isLoading = true;
        BmobQuery<LostItem> query = new BmobQuery<>();
        query.include("owner");
        query.order("-createdAt");
        query.setLimit(10);

        // 添加筛选条件
        applyFilters(query);

        query.findObjects(new FindListener<LostItem>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void done(List<LostItem> list, BmobException e) {
                swipeRefreshLayout.setRefreshing(false);
                isLoading = false;
                if (e == null) {
                    lostItemList.clear();
                    lostItemList.addAll(list);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "刷新失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadMoreItems() {
        if (isLoading) return;
        isLoading = true;

        BmobQuery<LostItem> query = new BmobQuery<>();
        query.include("owner");
        query.order("-createdAt");
        query.setSkip(lostItemList.size()); // 跳过已加载的数据
        query.setLimit(10);

        // 添加筛选条件
        applyFilters(query);

        query.findObjects(new FindListener<LostItem>() {
            @Override
            public void done(List<LostItem> list, BmobException e) {
                isLoading = false;
                if (e == null && list != null && !list.isEmpty()) {
                    int startPosition = lostItemList.size();
                    // 去重判断：只添加未存在的
                    for (LostItem item : list) {
                        boolean exists = false;
                        for (LostItem existing : lostItemList) {
                            if (existing.getObjectId().equals(item.getObjectId())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            lostItemList.add(item);
                        }
                    }
                    adapter.notifyItemRangeInserted(startPosition, lostItemList.size() - startPosition);
                }
            }
        });
    }

    // 应用筛选条件到查询
    private void applyFilters(BmobQuery<LostItem> query) {
        // 类别筛选
        if (!selectedCategory.isEmpty()) {
            query.addWhereEqualTo("category", selectedCategory);
        }

        // 紧急程度筛选
        if (urgentOnly) {
            query.addWhereEqualTo("isUrgent", true);
        }

        // 时间范围筛选
        if (!timeRange.equals("all")) {
            Calendar calendar = Calendar.getInstance();
            Date endDate = new Date();

            switch (timeRange) {
                case "today":
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    break;
                case "week":
                    calendar.add(Calendar.WEEK_OF_YEAR, -1);
                    break;
                case "month":
                    calendar.add(Calendar.MONTH, -1);
                    break;
            }

            Date startDate = calendar.getTime();
            query.addWhereGreaterThanOrEqualTo("createdAt", new BmobDate(startDate));
            query.addWhereLessThanOrEqualTo("createdAt", new BmobDate(endDate));
        }
    }
}