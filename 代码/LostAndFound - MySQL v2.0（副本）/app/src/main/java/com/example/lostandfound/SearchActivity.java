package com.example.lostandfound;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostandfound.databinding.ActivitySearchBinding;
import com.example.lostandfound.network.ApiService;
import com.example.lostandfound.network.RetrofitClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private ImageView ivBack;
    private TextView tvSearch;
    private RecyclerView recyclerView;
    private LostItemAdapter adapter;
    private List<LostItem> searchResults = new ArrayList<>();
    private List<String> searchHistory = new ArrayList<>();
    private TextView tvClearHistory;
    private static final String SEARCH_HISTORY_KEY = "search_history";
    private static final int MAX_HISTORY_ITEMS = 10;
    private RecyclerView rvHistory;
    private SearchHistoryAdapter historyAdapter;
    private boolean isShowingResults = false;
    private ActivitySearchBinding binding;
    public boolean hasHistory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("SearchActivity", "onCreate 被调用");
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.setActivity(this);
        // 初始化视图状态
        binding.progressBar.setVisibility(View.GONE);
        binding.tvNoResults.setVisibility(View.GONE);
        binding.resultsContainer.setVisibility(View.GONE);

        // 初始化searchHistory
        searchHistory = new ArrayList<>();

        initViews();
        setupHistoryRecyclerView();
        loadSearchHistory();
        setupRecyclerView();
    }

    private void initViews() {
        Log.d("SearchActivity", "initViews 被调用");
        etSearch = binding.etSearch; // 通过binding获取视图
        ivBack = binding.ivBack;
        tvSearch = binding.tvSearch;
        recyclerView = binding.recyclerView;
        tvClearHistory = binding.tvClearHistory;


        tvClearHistory.setOnClickListener(v -> clearSearchHistory());

        // 返回按钮点击事件
        ivBack.setOnClickListener(v -> finish());

        // 搜索按钮点击事件
        tvSearch.setOnClickListener(v -> performSearch());

        // 搜索框文本变化监听
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // 可以在这里实现实时搜索或搜索建议
            }
        });
    }

    private void updateHistoryVisibility() {
        Log.d("SearchActivity", "updateHistoryVisibility 被调用");
        hasHistory = !searchHistory.isEmpty();
        binding.invalidateAll(); // 刷新绑定数据
    }

    private void clearSearchHistory() {
        Log.d("SearchActivity", "cleanSearchHistory 被调用");
        SharedPreferences prefs = getSharedPreferences("SearchPrefs", MODE_PRIVATE);
        prefs.edit().remove(SEARCH_HISTORY_KEY).apply();

        // 清空列表数据源
        searchHistory.clear();

        // 通知适配器数据已变化
        historyAdapter.notifyDataSetChanged();

        // 更新绑定数据，控制 UI 显示
        updateHistoryVisibility();
    }

    private void setupRecyclerView() {
        Log.d("SearchActivity", "setRecyclerView 被调用");
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LostItemAdapter(searchResults, item -> {
            // 点击跳转到详情页
            Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
            intent.putExtra("lost_item", item.getId());
            Log.d("DetailLaunch", "LostItemId=" + item.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // 添加分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void loadSearchHistory() {
        Log.d("SearchActivity", "loadSearchHistory 被调用");
        SharedPreferences prefs = getSharedPreferences("SearchPrefs", MODE_PRIVATE);
        String history = prefs.getString(SEARCH_HISTORY_KEY, "");

        if (!TextUtils.isEmpty(history)) {
            String[] historyArray = history.split(",");
            searchHistory.clear();
            searchHistory.addAll(Arrays.asList(historyArray));
            Log.d("SearchActivity", "加载历史记录: " + searchHistory.size() + "条");
        } else {
            searchHistory = new ArrayList<>();
            Log.d("SearchActivity", "无历史记录");
        }

        if (historyAdapter != null) {
            historyAdapter.notifyDataSetChanged(); // 通知适配器更新
        }

        // 更新UI状态
        updateHistoryVisibility();
        showSearchHistory();
    }

    private void performSearch() {
        Log.d("SearchActivity", "performSearch 被调用");
        String keyword = etSearch.getText().toString().trim();
        Log.d("SearchActivity", "keyword="+keyword);
        if (keyword.isEmpty()) {
            Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
            return;
        }

        saveSearchHistory(keyword);
        searchItems(keyword);
        showSearchResults();
    }

    private void saveSearchHistory(String keyword) {
        Log.d("SearchActivity", "saveSearchHistory 被调用 keyword=" + keyword);

        // 确保searchHistory已初始化
        if (searchHistory == null) {
            searchHistory = new ArrayList<>();
        }

        // 移除重复项
        if (searchHistory.contains(keyword)) {
            searchHistory.remove(keyword);
        }

        // 添加到开头
        searchHistory.add(0, keyword);

        // 限制历史记录数量
        if (searchHistory.size() > MAX_HISTORY_ITEMS) {
            searchHistory = new ArrayList<>(searchHistory.subList(0, MAX_HISTORY_ITEMS));
        }

        // 保存到SharedPreferences
        SharedPreferences prefs = getSharedPreferences("SearchPrefs", MODE_PRIVATE);
        String history = TextUtils.join(",", searchHistory);
        prefs.edit().putString(SEARCH_HISTORY_KEY, history).apply();

        Log.d("SearchActivity", "搜索历史已保存: " + history);

        // 更新历史记录显示
        if (historyAdapter != null) {
            historyAdapter.notifyDataSetChanged();
        }
    }

    private void setupHistoryRecyclerView() {
        Log.d("SearchActivity", "setupHistoryRecyclerView 被调用");
        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new SearchHistoryAdapter(searchHistory, keyword -> {
            etSearch.setText(keyword);
            performSearch();
        });
        rvHistory.setAdapter(historyAdapter);
        historyAdapter.notifyDataSetChanged();
    }

    private void showSearchResults() {
        Log.d("SearchActivity", "showSearchResults 被调用");
        isShowingResults = true;

        // 隐藏历史记录区域
        binding.historyHeader.setVisibility(View.GONE);
        binding.rvHistory.setVisibility(View.GONE);

        // 显示结果区域
        binding.resultsContainer.setVisibility(View.VISIBLE);

        // 确保适配器更新
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void showSearchHistory() {
        Log.d("SearchActivity", "showSearchHistory 被调用");
        isShowingResults = false;

        // 显示历史记录区域
        if (hasHistory) {
            binding.historyHeader.setVisibility(View.VISIBLE);
            binding.rvHistory.setVisibility(View.VISIBLE);
        }

        // 隐藏结果区域
        binding.resultsContainer.setVisibility(View.GONE);

        // 确保适配器更新
        if (historyAdapter != null) {
            historyAdapter.notifyDataSetChanged();
        }
    }

    private void searchItems(String keyword) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvNoResults.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<List<LostItem>> call = apiService.getLostItems(keyword);

        call.enqueue(new Callback<List<LostItem>>() {
            @Override
            public void onResponse(Call<List<LostItem>> call, Response<List<LostItem>> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    searchResults.clear();
                    searchResults.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (searchResults.isEmpty()) {
                        binding.tvNoResults.setVisibility(View.VISIBLE);
                        binding.tvNoResults.setText("未找到相关结果");
                    }
                } else {
                    binding.tvNoResults.setVisibility(View.VISIBLE);
                    binding.tvNoResults.setText("搜索失败: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<LostItem>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvNoResults.setVisibility(View.VISIBLE);
                binding.tvNoResults.setText("网络错误: " + t.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isShowingResults) {
            showSearchHistory();
        } else {
            super.onBackPressed();
        }
    }
}