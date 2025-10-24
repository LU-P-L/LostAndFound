package com.example.lostandfound;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lostandfound.network.ApiService;
import com.example.lostandfound.network.RetrofitClient;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText etPhone, etCaptcha, etNewPwd, etConfirmPwd;
    private TextInputLayout tilNewPwd, tilConfirmPwd;
    private Button btnGetCaptcha, btnSubmit;
    private ImageView ivBack, ivCaptcha;
    private String currentSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_change_password);

        initViews();
        prefillPhoneNumber();
        loadCaptchaImage();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        etPhone = findViewById(R.id.et_phone);
        etCaptcha = findViewById(R.id.et_captcha);
        etNewPwd = findViewById(R.id.et_new_pwd);
        etConfirmPwd = findViewById(R.id.et_confirm_pwd);
        btnGetCaptcha = findViewById(R.id.btn_get_captcha);
        btnSubmit = findViewById(R.id.btn_submit);
        ivCaptcha = findViewById(R.id.iv_captcha);

        tilNewPwd = findViewById(R.id.til_new_pwd);
        tilConfirmPwd = findViewById(R.id.til_confirm_pwd);

        ivBack.setOnClickListener(v -> finish());
        btnGetCaptcha.setOnClickListener(v -> loadCaptchaImage());
        btnSubmit.setOnClickListener(v -> changePassword());
    }

    private void prefillPhoneNumber() {
        // 从 SharedPreferences 获取已登录用户的手机号
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String phone = prefs.getString("phone", ""); // 默认值为空字符串

        if (!phone.isEmpty()) {
            etPhone.setText(phone);
        } else {
            Toast.makeText(this, "未找到已登录用户的手机号", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadCaptchaImage() {
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);
        Call<JsonObject> call = apiService.getCaptcha();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String imageBase64 = response.body().get("image").getAsString();
                        if (imageBase64.contains(",")) {
                            imageBase64 = imageBase64.split(",")[1];
                        }
                        currentSessionId = response.body().get("sessionId").getAsString();

                        // 显示 Base64 验证码图片
                        byte[] decodedBytes = android.util.Base64.decode(imageBase64, android.util.Base64.DEFAULT);
                        Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        ivCaptcha.setImageBitmap(bitmap);

                        Toast.makeText(ChangePasswordActivity.this,
                                "验证码已刷新", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ChangePasswordActivity.this,
                                "验证码加载失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChangePasswordActivity.this,
                            "验证码获取失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this,
                        "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void changePassword() {
        String phone = etPhone.getText().toString().trim();
        String captcha = etCaptcha.getText().toString().trim();
        String newPwd = etNewPwd.getText().toString().trim();
        String confirmPwd = etConfirmPwd.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(captcha)) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证新密码
        boolean isNewPwdValid = validatePassword(newPwd);
        boolean isConfirmPwdValid = validateConfirmPassword(newPwd, confirmPwd);

        if (!isNewPwdValid || !isConfirmPwdValid) {
            return;
        }

        // 调用API重置密码
        ApiService apiService = RetrofitClient.getClient(this).create(ApiService.class);

        Map<String, String> params = new HashMap<>();
        params.put("phone", phone);
        params.put("captcha", captcha);
        params.put("newPassword", newPwd);

        Call<JsonObject> call = apiService.resetPassword(params);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        boolean success = response.body().get("success").getAsBoolean();
                        String message = response.body().get("message").getAsString();

                        if (success) {
                            Toast.makeText(ChangePasswordActivity.this,
                                    "密码修改成功", Toast.LENGTH_SHORT).show();
                            // 返回登录页面
                            Intent intent = new Intent(ChangePasswordActivity.this,
                                    LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(ChangePasswordActivity.this,
                                    "密码修改失败: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ChangePasswordActivity.this,
                                "密码修改失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        String errorMsg = response.errorBody().string();
                        JSONObject errorObj = new JSONObject(errorMsg);
                        Toast.makeText(ChangePasswordActivity.this,
                                "密码修改失败: " + errorObj.getString("message"),
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(ChangePasswordActivity.this,
                                "密码修改失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this,
                        "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 密码验证
    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            tilNewPwd.setError("密码不能为空");
            return false;
        }

        if (password.length() < 8 || password.length() > 20) {
            tilNewPwd.setError("密码长度应在8-20位之间");
            return false;
        }

        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasSpecial = password.matches(".*[,.!?].*");

        int complexityCount = 0;
        if (hasDigit) complexityCount++;
        if (hasLower) complexityCount++;
        if (hasUpper) complexityCount++;
        if (hasSpecial) complexityCount++;

        if (complexityCount < 3) {
            tilNewPwd.setError("需包含数字、小写字母、大写字母、特殊字符(，。？！)中的至少三种");
            return false;
        }

        tilNewPwd.setError(null);
        return true;
    }

    // 确认密码验证
    private boolean validateConfirmPassword(String password, String confirmPassword) {
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPwd.setError("请确认密码");
            return false;
        }

        if (!confirmPassword.equals(password)) {
            tilConfirmPwd.setError("两次输入的密码不一致");
            return false;
        }

        tilConfirmPwd.setError(null);
        return true;
    }
}