package com.example.lostandfound;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilPhone, tilPassword, tilConfirmPassword;
    private String username, phone, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_register);

        // 初始化Bmob
        Bmob.initialize(this, "1eb189c2c0fd83bd44c9992769cdb1b2");

        // 初始化视图
        initViews();

        // 设置文本变化监听
        setupTextWatchers();
    }

    private void initViews() {
        tilUsername = findViewById(R.id.til_username);
        tilPhone = findViewById(R.id.til_phone);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);

        Button btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(v -> attemptRegister());

        TextView tvLogin = findViewById(R.id.tv_login);
        tvLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void setupTextWatchers() {
        // 用户名实时验证
        tilUsername.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUsername(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 手机号实时验证
        tilPhone.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePhone(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 密码实时验证
        tilPassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 确认密码实时验证
        tilConfirmPassword.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void attemptRegister() {
        // 获取输入值
        username = tilUsername.getEditText().getText().toString().trim();
        phone = tilPhone.getEditText().getText().toString().trim();
        password = tilPassword.getEditText().getText().toString().trim();
        confirmPassword = tilConfirmPassword.getEditText().getText().toString().trim();

        // 验证所有输入
        boolean isUsernameValid = validateUsername(username);
        boolean isPhoneValid = validatePhone(phone);
        boolean isPasswordValid = validatePassword(password);
        boolean isConfirmPasswordValid = validateConfirmPassword(confirmPassword);

        if (isUsernameValid && isPhoneValid && isPasswordValid && isConfirmPasswordValid) {
            registerUser();
        }
    }

    private boolean validateUsername(String username) {
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("用户名不能为空");
            return false;
        }

        // 用户名仅限中英文字符与数字
        if (!username.matches("^[a-zA-Z0-9\\u4e00-\\u9fa5]+$")) {
            tilUsername.setError("只能包含中英文字符和数字");
            return false;
        }

        // 用户名长度检查
        if (username.length() < 4 || username.length() > 16) {
            tilUsername.setError("用户名长度应在4-16位之间");
            return false;
        }

        tilUsername.setError(null);
        return true;
    }

    private boolean validatePhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("手机号不能为空");
            return false;
        }

        // 内地11位手机号验证
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            tilPhone.setError("请输入有效的11位手机号");
            return false;
        }

        tilPhone.setError(null);
        return true;
    }

    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("密码不能为空");
            return false;
        }

        // 密码长度检查
        if (password.length() < 8 || password.length() > 20) {
            tilPassword.setError("密码长度应在8-20位之间");
            return false;
        }

        // 密码复杂度检查
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasSpecial = password.matches(".*[,.?!].*");

        int complexityCount = 0;
        if (hasDigit) complexityCount++;
        if (hasLower) complexityCount++;
        if (hasUpper) complexityCount++;
        if (hasSpecial) complexityCount++;

        if (complexityCount < 3) {
            tilPassword.setError("需包含数字、大小写字母、特殊字符(，。？！)中的至少三种");
            return false;
        }

        tilPassword.setError(null);
        return true;
    }

    private boolean validateConfirmPassword(String confirmPassword) {
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("请确认密码");
            return false;
        }

        if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError("两次输入的密码不一致");
            return false;
        }

        tilConfirmPassword.setError(null);
        return true;
    }

    private void registerUser() {
        BmobUser user = new BmobUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setMobilePhoneNumber(phone);

        user.signUp(new SaveListener<BmobUser>() {
            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                runOnUiThread(() -> {
                    if (e == null) {
                        Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    } else {
                        Log.e("BmobError", "错误码: " + e.getErrorCode() + ", 错误信息: " + e.getMessage());
                        if (e.getErrorCode() == 202) {
                            tilUsername.setError("用户名已被注册");
                        } else if (e.getErrorCode() == 209) {
                            tilPhone.setError("手机号已被注册");
                        } else {
                            Toast.makeText(RegisterActivity.this, "注册失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    // XML中onClick方法
    public void onLoginTextClicked(View view) {
        navigateToLogin();
    }
}