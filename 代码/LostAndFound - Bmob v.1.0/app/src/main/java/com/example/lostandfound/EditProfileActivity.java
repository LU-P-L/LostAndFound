package com.example.lostandfound;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import java.io.File;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar, ivBack;
    private EditText etUsername, etEmail, etPhone;
    private TextInputLayout tilUsername, tilPhone;
    private Button btnSave;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_edit_profile);

        initViews();
        loadUserData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        ivAvatar = findViewById(R.id.iv_avatar);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        btnSave = findViewById(R.id.btn_save);

        // 绑定TextInputLayout
        tilUsername = findViewById(R.id.til_username);
        tilPhone = findViewById(R.id.til_phone);

        // 设置返回按钮点击事件
        ivBack.setOnClickListener(v -> finish());

        ivAvatar.setOnClickListener(v -> {
            // 打开图片选择器
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        });

        btnSave.setOnClickListener(v -> saveProfile());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).into(ivAvatar);
        }
    }

    private void loadUserData() {
        User user = BmobUser.getCurrentUser(User.class);
        if (user != null) {
            etUsername.setText(user.getUsername());
            etEmail.setText(user.getEmail());
            etPhone.setText(user.getMobilePhoneNumber());

            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                Glide.with(this).load(user.getAvatar()).into(ivAvatar);
            }
        }
    }

    private void saveProfile() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        // 验证用户名和手机号（使用与注册页面相同的规则）
        boolean isUsernameValid = validateUsername(username);
        boolean isPhoneValid = validatePhone(phone);

        if (!isUsernameValid || !isPhoneValid) {
            return; // 验证失败则终止保存
        }

        User user = BmobUser.getCurrentUser(User.class);
        if (user == null) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            return;
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setMobilePhoneNumber(phone);

        if (selectedImageUri != null) {
            uploadImage(selectedImageUri, user);
        } else {
            updateUserProfile(user);
        }
    }

    // 用户名验证
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

    // 手机号验证
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

    private void uploadImage(Uri imageUri, User user) {
        String filePath = FileUtils.getPath(this, imageUri);
        if (filePath == null) {
            Toast.makeText(this, "无法获取图片路径", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(filePath);
        BmobFile bmobFile = new BmobFile(file);
        bmobFile.uploadblock(new UploadFileListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    user.setAvatar(bmobFile.getFileUrl());
                    updateUserProfile(user);
                } else {
                    Toast.makeText(EditProfileActivity.this, "头像上传失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUserProfile(User user) {
        user.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Toast.makeText(EditProfileActivity.this, "保存成功", Toast.LENGTH_SHORT).show();

                    // 添加结果标识，通知ProfileActivity需要刷新
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("refresh", true);
                    setResult(RESULT_OK, resultIntent);

                    finish();
                } else if (e.getErrorCode() == 202) {
                    tilUsername.setError("用户名已被使用");
                } else if (e.getErrorCode() == 209) {
                    tilPhone.setError("手机号已被注册");
                } else {
                    Toast.makeText(EditProfileActivity.this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}