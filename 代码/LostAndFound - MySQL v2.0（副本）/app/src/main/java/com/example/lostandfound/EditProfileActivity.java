package com.example.lostandfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.lostandfound.network.ApiResponse;
import com.example.lostandfound.network.ApiService;
import com.example.lostandfound.network.RetrofitClient;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        if (userId == null) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<ApiResponse<JsonObject>> call = apiService.getUserProfile(userId);

        call.enqueue(new Callback<ApiResponse<JsonObject>>() {
            @Override
            public void onResponse(Call<ApiResponse<JsonObject>> call, Response<ApiResponse<JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    JsonObject userData = response.body().getData();
                    etUsername.setText(userData.has("username") ? userData.get("username").getAsString() : "");
                    etEmail.setText(userData.has("email") ? userData.get("email").getAsString() : "");
                    etPhone.setText(userData.has("phone") ? userData.get("phone").getAsString() : "");

                    // 显示用户头像
                    if (userData.has("avatarUrl") && !userData.get("avatarUrl").isJsonNull()) {
                        String avatarUrl = userData.get("avatarUrl").getAsString();
                        if (!avatarUrl.startsWith("http")) {
                            // 拼接完整URL
                            avatarUrl = "http://172.20.10.6:8080" + avatarUrl;
                        }

                        Glide.with(EditProfileActivity.this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.ic_default_avatar)  // 默认占位图
                                .error(R.drawable.ic_default_avatar)       // 加载失败时
                                .into(ivAvatar);
                    } else {
                        // 没有头像字段或为空时，设置默认头像
                        ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<JsonObject>> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "加载用户信息失败", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void saveProfile() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        boolean isUsernameValid = validateUsername(username);
        boolean isPhoneValid = validatePhone(phone);

        if (!isUsernameValid || !isPhoneValid) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        if (userId == null) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);

        // 创建多部分请求
        MultipartBody.Part avatarPart = null;
        if (selectedImageUri != null) {
            File file = new File(FileUtils.getPath(this, selectedImageUri));
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            avatarPart = MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);
        }

        RequestBody userIdBody = RequestBody.create(MediaType.parse("text/plain"), userId);
        RequestBody usernameBody = RequestBody.create(MediaType.parse("text/plain"), username);
        RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), email);
        RequestBody phoneBody = RequestBody.create(MediaType.parse("text/plain"), phone);

        Call<ApiResponse<JsonObject>> call = apiService.updateProfile(
                userIdBody, usernameBody, emailBody, phoneBody, avatarPart);

        call.enqueue(new Callback<ApiResponse<JsonObject>>() {
            @Override
            public void onResponse(Call<ApiResponse<JsonObject>> call, Response<ApiResponse<JsonObject>> response) {
                Log.d("EditProfile","response: "+response);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getCode() == 200) {
                        Toast.makeText(EditProfileActivity.this, "保存成功", Toast.LENGTH_SHORT).show();

                        JsonObject updatedUserData = response.body().getData();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("updatedUserData", updatedUserData.toString());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        String errorMsg = response.body().getMessage();
                        if (errorMsg.contains("用户名已被使用")) {
                            tilUsername.setError("用户名已被使用");
                        } else if (errorMsg.contains("手机号已被注册")) {
                            tilPhone.setError("手机号已被注册");
                        } else {
                            Toast.makeText(EditProfileActivity.this,
                                    "保存失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<JsonObject>> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
}