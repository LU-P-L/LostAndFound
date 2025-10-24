package com.example.lostandfound;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.lostandfound.network.ApiService;
import com.example.lostandfound.network.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.example.lostandfound.utils.TokenUtils;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublishActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGES = 1001;
    private static final int REQUEST_CODE_PERMISSION = 1002;

    private LinearLayout imageContainer;
    private List<Uri> selectedImages = new ArrayList<>();
    private List<ImageView> imageViews = new ArrayList<>();

    private EditText etItemName, etCategory, etLostTime, etLostLocation, etDescription, etContact;
    private TextInputLayout tilItemName, tilCategory, tilLostTime, tilLostLocation, tilDescription, tilContact;
    private CheckBox cbIsUrgent;

    private boolean isDraft = false;
    private String draftId = null;

    private static final String PREFS_NAME = "LostItemDraft";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_CONTACT = "contact";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_LOST_TIME = "lostTime";
    private static final String KEY_LOST_LOCATION = "lostLocation";

    private ApiService apiService;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_publish);

        // 检查用户是否登录
        authToken = TokenUtils.getToken(this);
        Log.d("Publish", "获取的Token: " + authToken);

        if (!TokenUtils.isTokenValid(this)) {
            Toast.makeText(this, "请先登录或会话已过期", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 初始化Retrofit服务
        apiService = RetrofitClient.getClient(this).create(ApiService.class);

        initViews();
        setupListeners();
        setupBottomNavigation();
        // 检查是否有草稿数据
        checkDraft();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_publish);
    }

    private void initViews() {
        imageContainer = findViewById(R.id.imageContainer);

        etItemName = findViewById(R.id.etItemName);
        etCategory = findViewById(R.id.etCategory);
        etLostTime = findViewById(R.id.etLostTime);
        etLostLocation = findViewById(R.id.etLostLocation);
        etDescription = findViewById(R.id.etDescription);

        tilItemName = findViewById(R.id.tilItemName);
        tilCategory = findViewById(R.id.tilCategory);
        tilLostTime = findViewById(R.id.tilLostTime);
        tilLostLocation = findViewById(R.id.tilLostLocation);
        tilDescription = findViewById(R.id.tilDescription);

        cbIsUrgent = findViewById(R.id.cbIsUrgent);

        etContact = findViewById(R.id.etContact);
        tilContact = findViewById(R.id.tilContact);
    }

    private void setupListeners() {
        // 添加图片按钮点击事件
        findViewById(R.id.ivAddImage).setOnClickListener(v -> {
            if (selectedImages.size() >= 3) {
                Toast.makeText(this, "最多只能上传3张图片", Toast.LENGTH_SHORT).show();
                return;
            }
            checkPermissionAndPickImages();
        });

        // 发布按钮点击事件
        findViewById(R.id.btnPublish).setOnClickListener(v -> publishLostItem());

        // 存草稿按钮点击事件
        findViewById(R.id.btnSaveDraft).setOnClickListener(v -> saveAsDraft());

        // 退出按钮点击事件
        findViewById(R.id.btnExit).setOnClickListener(v -> showExitConfirmationDialog());
    }

    private void checkPermissionAndPickImages() {
        // 检查是否执行到这里
        Log.d("Permission", "checkPermissionAndPickImages called");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("Permission", "Permission not granted, requesting...");

            // 先检查是否应该显示解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // 显示解释对话框
                new AlertDialog.Builder(this)
                        .setTitle("需要存储权限")
                        .setMessage("请允许访问相册以选择图片")
                        .setPositiveButton("确定", (dialog, which) -> {
                            // 用户理解后再次请求
                            ActivityCompat.requestPermissions(
                                    PublishActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_CODE_PERMISSION
                            );
                        })
                        .setNegativeButton("取消", null)
                        .show();
            } else {
                // 直接请求权限
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_PERMISSION
                );
            }
        } else {
            Log.d("Permission", "Permission already granted");
            pickImages();
        }
    }

    private void pickImages() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGES && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    // 多选图片
                    int count = Math.min(data.getClipData().getItemCount(), 3 - selectedImages.size());
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        selectedImages.add(imageUri);
                        addImageToContainer(imageUri);
                    }
                } else if (data.getData() != null) {
                    // 单选图片
                    if (selectedImages.size() < 3) {
                        Uri imageUri = data.getData();
                        selectedImages.add(imageUri);
                        addImageToContainer(imageUri);
                    }
                }
            }
        }
    }

    private void addImageToContainer(Uri imageUri) {
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(80), dpToPx(80));
        params.setMargins(0, 0, dpToPx(8), 0);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setBackground(ContextCompat.getDrawable(this, R.drawable.image_upload_bg));

        Glide.with(this)
                .load(imageUri)
                .into(imageView);

        // 添加删除按钮功能
        imageView.setOnClickListener(v -> {
            int position = imageViews.indexOf(imageView);
            if (position != -1) {
                selectedImages.remove(position);
                imageContainer.removeView(imageView);
                imageViews.remove(imageView);
            }
        });

        imageContainer.addView(imageView, imageContainer.getChildCount() - 1);
        imageViews.add(imageView);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (TextUtils.isEmpty(etItemName.getText())) {
            tilItemName.setError("物品名称不能为空");
            isValid = false;
        } else {
            tilItemName.setError(null);
        }

        if (TextUtils.isEmpty(etCategory.getText())) {
            tilCategory.setError("物品类别不能为空");
            isValid = false;
        } else {
            tilCategory.setError(null);
        }

        if (TextUtils.isEmpty(etLostTime.getText())) {
            tilLostTime.setError("丢失时间不能为空");
            isValid = false;
        } else {
            tilLostTime.setError(null);
        }

        if (TextUtils.isEmpty(etLostLocation.getText())) {
            tilLostLocation.setError("丢失地点不能为空");
            isValid = false;
        } else {
            tilLostLocation.setError(null);
        }

        if (TextUtils.isEmpty(etContact.getText())) {
            tilContact.setError("联系方式不能为空");
            isValid = false;
        } else {
            tilContact.setError(null);
        }

        if (TextUtils.isEmpty(etDescription.getText())) {
            tilDescription.setError("物品描述不能为空");
            isValid = false;
        } else {
            tilDescription.setError(null);
        }

        return isValid;
    }

    private void publishLostItem() {
        if (!validateForm()) {
            return;
        }

        // 准备表单数据
        RequestBody itemName = RequestBody.create(
                MediaType.parse("text/plain"), etItemName.getText().toString());
        RequestBody category = RequestBody.create(
                MediaType.parse("text/plain"), etCategory.getText().toString());
        RequestBody lostTime = RequestBody.create(
                MediaType.parse("text/plain"), etLostTime.getText().toString());
        RequestBody lostLocation = RequestBody.create(
                MediaType.parse("text/plain"), etLostLocation.getText().toString());
        RequestBody description = RequestBody.create(
                MediaType.parse("text/plain"), etDescription.getText().toString());
        RequestBody contactInfo = RequestBody.create(
                MediaType.parse("text/plain"), etContact.getText().toString());
        RequestBody isUrgent = RequestBody.create(
                MediaType.parse("text/plain"), String.valueOf(cbIsUrgent.isChecked()));

        // 准备图片文件
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri uri : selectedImages) {
            try {
                File file = new File(FileUtils.getPath(this, uri));
                RequestBody requestFile = RequestBody.create(
                        MediaType.parse("image/*"), file);
                MultipartBody.Part part = MultipartBody.Part.createFormData(
                        "images", file.getName(), requestFile);
                imageParts.add(part);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "图片处理失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        // 发送请求
        Call<JsonObject> call = apiService.publishLostItem(
                itemName,
                category,
                lostTime,
                lostLocation,
                description,
                contactInfo,
                isUrgent,
                imageParts
        );

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PublishActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                    if (isDraft) {
                        deleteDraft();
                    }
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "未知错误";
                        Toast.makeText(PublishActivity.this,
                                "发布失败: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(PublishActivity.this,
                        "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAsDraft() {
        if (!validateForm()) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TITLE, etItemName.getText().toString().trim());
        editor.putString(KEY_DESCRIPTION, etDescription.getText().toString().trim());
        editor.putString(KEY_CONTACT, etContact.getText().toString().trim());
        editor.putString(KEY_CATEGORY, etCategory.getText().toString().trim());
        editor.putString(KEY_LOST_TIME, etLostTime.getText().toString().trim());
        editor.putString(KEY_LOST_LOCATION, etLostLocation.getText().toString().trim());

        editor.apply();

        Toast.makeText(this, "已保存到草稿箱", Toast.LENGTH_SHORT).show();
        isDraft = true;
    }

    private void deleteDraft() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        Toast.makeText(this, "草稿已删除", Toast.LENGTH_SHORT).show();
        isDraft = false;
    }

    private void checkDraft() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String title = prefs.getString(KEY_TITLE, "");
        String description = prefs.getString(KEY_DESCRIPTION, "");
        String contact = prefs.getString(KEY_CONTACT, "");

        if (!title.isEmpty() || !description.isEmpty() || !contact.isEmpty()) {
            etItemName.setText(title);
            etDescription.setText(description);
            etContact.setText(contact);

            Toast.makeText(this, "已加载草稿", Toast.LENGTH_SHORT).show();
            isDraft = true;
        }
    }

    private void showExitConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("是否放弃此次编辑？")
                .setPositiveButton("继续退出", (dialog, which) -> finish())
                .setNegativeButton("返回", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showExitConfirmationDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION) {
            // 检查是否为空（某些设备可能返回空数组）
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "Permission granted");
                pickImages();
            } else {
                Log.d("Permission", "Permission denied");

                // 检查用户是否勾选了"不再询问"
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // 引导用户去设置页手动开启
                    new AlertDialog.Builder(this)
                            .setTitle("权限被永久拒绝")
                            .setMessage("请在设置中手动开启存储权限")
                            .setPositiveButton("去设置", (dialog, which) -> {
                                openAppSettings();
                            })
                            .setNegativeButton("取消", null)
                            .show();
                } else {
                    Toast.makeText(this, "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // 打开应用设置页
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_publish) {
                // 已经在发布页面
                return true;
            }

            // 检查是否有未保存的更改
            if (hasUnsavedChanges()) {
                showNavigationConfirmationDialog(item.getItemId());
                return false; // 暂时阻止导航
            } else {
                navigateTo(item.getItemId());
                return true;
            }
        });
    }

    private boolean hasUnsavedChanges() {
        // 检查表单是否有输入内容
        return !TextUtils.isEmpty(etItemName.getText().toString()) ||
                !TextUtils.isEmpty(etCategory.getText().toString()) ||
                !TextUtils.isEmpty(etLostTime.getText().toString()) ||
                !TextUtils.isEmpty(etLostLocation.getText().toString()) ||
                !TextUtils.isEmpty(etDescription.getText().toString()) ||
                !selectedImages.isEmpty();
    }

    private void showNavigationConfirmationDialog(int targetItemId) {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("是否放弃此次编辑？")
                .setPositiveButton("继续退出", (dialog, which) -> {
                    performNavigation(targetItemId);
                })
                .setNegativeButton("返回", (dialog, which) -> {
                    // 用户选择返回，不做任何操作
                })
                .setCancelable(false)
                .show();
    }

    private void performNavigation(int itemId) {
        Intent intent = null;

        if (itemId == R.id.nav_home) {
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        } else if (itemId == R.id.nav_message) {
            intent = new Intent(this, MessageActivity.class);
        } else if (itemId == R.id.nav_profile) {
            intent = new Intent(this, ProfileActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
            finish();
        }
    }

    private void navigateTo(int itemId) {
        if (itemId == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (itemId == R.id.nav_message) {
            startActivity(new Intent(this, MessageActivity.class));
        } else if (itemId == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        }
        finish();
    }
}