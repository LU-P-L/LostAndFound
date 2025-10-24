package com.example.lostandfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;

public class LoginActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        EditText etUsername = findViewById(R.id.et_username);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                Toast.makeText(LoginActivity.this, "请输入手机号", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                return;
            }

            // 使用Bmob进行登录验证
            BmobUser.loginByAccount(username, password, new LogInListener<BmobUser>() {
                @Override
                public void done(BmobUser user, BmobException e) {
                    if (e == null) {
                        // 登录成功
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(KEY_IS_LOGGED_IN, true);
                        editor.apply();

                        // 跳转主页面
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    } else {
                        // 登录失败
                        Toast.makeText(LoginActivity.this, "输入账号或密码错误，请重新输入", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        // 添加注册跳转
        TextView tvRegister = findViewById(R.id.tv_register);
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // 忘记密码跳转
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ChangePasswordActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
}