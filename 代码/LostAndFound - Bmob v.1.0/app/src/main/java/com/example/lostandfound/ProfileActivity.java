package com.example.lostandfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import cn.bmob.v3.BmobUser;

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
        User user = BmobUser.getCurrentUser(User.class);
        if (user != null) {
            tvUsername.setText(user.getUsername() != null ? user.getUsername() : "未设置");
            tvPhone.setText(user.getMobilePhoneNumber() != null ? user.getMobilePhoneNumber() : "未设置");
            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "未设置");

            // 加载头像
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                Glide.with(this).load(user.getAvatar()).into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }
        }
    }

    private void logout() {
        // 当用户点击退出登录时
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply(); // 或者使用 commit() 确保立即生效

        // 然后跳转到欢迎页面或登录页面
        Intent intent = new Intent(this, WelcomeActivity.class);
        startActivity(intent);
        finish();
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

        if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("refresh", false)) {
                // 当接收到刷新标志时重新加载数据
                loadUserData();
            }
        }
    }
}