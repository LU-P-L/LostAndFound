package com.example.lostandfound;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etPhone, etCode, etNewPwd, etConfirmPwd;
    private TextInputLayout tilNewPwd, tilConfirmPwd;
    private Button btnSendCode, btnSubmit;
    private CountDownTimer countDownTimer;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_change_password);

        initViews();
        prefillPhoneNumber();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        etPhone = findViewById(R.id.et_phone);
        etCode = findViewById(R.id.et_code);
        etNewPwd = findViewById(R.id.et_new_pwd);
        etConfirmPwd = findViewById(R.id.et_confirm_pwd);
        btnSendCode = findViewById(R.id.btn_send_code);
        btnSubmit = findViewById(R.id.btn_submit);

        // 绑定TextInputLayout
        tilNewPwd = findViewById(R.id.til_new_pwd);
        tilConfirmPwd = findViewById(R.id.til_confirm_pwd);

        // 设置返回按钮点击事件
        ivBack.setOnClickListener(v -> finish());
        btnSendCode.setOnClickListener(v -> sendVerificationCode());
        btnSubmit.setOnClickListener(v -> changePassword());
    }

    private void prefillPhoneNumber() {
        User user = BmobUser.getCurrentUser(User.class);
        if (user != null && user.getMobilePhoneNumber() != null) {
            etPhone.setText(user.getMobilePhoneNumber());
        }
    }

    private void sendVerificationCode() {
        String phone = etPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        // 发送验证码
        BmobSMS.requestSMSCode(phone, "ChangePassword", new QueryListener<Integer>() {
            @Override
            public void done(Integer smsId, BmobException e) {
                if (e == null) {
                    Toast.makeText(ChangePasswordActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                    startCountDown();
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "验证码发送失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startCountDown() {
        btnSendCode.setEnabled(false);
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnSendCode.setText("重新发送(" + millisUntilFinished / 1000 + "s)");
            }

            @Override
            public void onFinish() {
                btnSendCode.setEnabled(true);
                btnSendCode.setText("发送验证码");
            }
        }.start();
    }

    private void changePassword() {
        String phone = etPhone.getText().toString().trim();
        String code = etCode.getText().toString().trim();
        String newPwd = etNewPwd.getText().toString().trim();
        String confirmPwd = etConfirmPwd.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证新密码（使用与注册页面相同的规则）
        boolean isNewPwdValid = validatePassword(newPwd);
        boolean isConfirmPwdValid = validateConfirmPassword(newPwd, confirmPwd);

        if (!isNewPwdValid || !isConfirmPwdValid) {
            return; // 验证失败则终止操作
        }

        // 使用验证码重置密码
        BmobUser.resetPasswordBySMSCode(code, newPwd, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Toast.makeText(ChangePasswordActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();

                    // 直接返回登录页面
                    Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 清除中间所有Activity
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "密码修改失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 密码验证
    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            tilNewPwd.setError("密码不能为空");
            return false;
        }

        // 密码长度检查
        if (password.length() < 8 || password.length() > 20) {
            tilNewPwd.setError("密码长度应在8-20位之间");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}