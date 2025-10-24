package com.example.lostandfound;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.lostandfound.Comment;
import com.example.lostandfound.User;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class DetailActivity extends AppCompatActivity {
    private ImageView ivBack;
    private ImageView ivItemImage;
    private TextView tvItemName;
    private TextView tvCategory;
    private TextView tvLostTime;
    private TextView tvLostLocation;
    private TextView tvDescription;
    private TextView tvOwner;
    private TextView tvContact;
    private TextView tvUrgent;
    private TextView tvPublishTime;
    private Button btnContact;
    private RecyclerView rvComments;
    private EditText etComment;
    private ImageButton btnSendComment;
    private ImageView ivMore;

    private LostItem lostItem;
    private List<Comment> commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_detail);

        initViews();
        getIntentData();
        setClickListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        ivItemImage = findViewById(R.id.iv_item_image);
        tvItemName = findViewById(R.id.tv_item_name);
        tvCategory = findViewById(R.id.tv_item_type);
        tvLostTime = findViewById(R.id.tv_lost_time);
        tvLostLocation = findViewById(R.id.tv_lost_location);
        tvDescription = findViewById(R.id.tv_description);
        tvOwner = findViewById(R.id.tv_publisher);
        tvUrgent = findViewById(R.id.tv_emergency_level);
        tvPublishTime = findViewById(R.id.tv_publish_time);
        btnContact = findViewById(R.id.btn_contact);
        rvComments = findViewById(R.id.rv_comments);
        etComment = findViewById(R.id.et_comment);
        btnSendComment = findViewById(R.id.btn_send_comment);
        ivMore = findViewById(R.id.iv_more);


        // 设置评论列表
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(commentList);
        rvComments.setAdapter(commentAdapter);

        // 获取当前用户
        BmobUser currentUser = BmobUser.getCurrentUser(BmobUser.class);
        if (currentUser != null) {
            currentUserId = currentUser.getObjectId();
        }
    }

    private void getIntentData() {
        String lostItemId = getIntent().getStringExtra("lost_item");
        if (lostItemId == null || lostItemId.isEmpty()) {
            Toast.makeText(this, "无效的失物信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        BmobQuery<LostItem> query = new BmobQuery<>();
        query.include("owner");
        query.getObject(lostItemId, new QueryListener<LostItem>() {
            @Override
            public void done(LostItem item, BmobException e) {
                if (e == null && item != null) {
                    runOnUiThread(() ->{
                        lostItem = item;
                        Log.e("DetailActivity", "获取到的LostItem ID: " + item.getObjectId());
                        updateUI();
                        loadComments(item);


                        // 检查是否是发布者
                        if (lostItem.getOwner() != null && lostItem.getOwner().getObjectId().equals(currentUserId))
                        {
                            ivMore.setVisibility(View.VISIBLE);
                        } else {
                            ivMore.setVisibility(View.GONE);
                        }
                    });
                } else {
                    Log.e("DetailActivity", "查询失败: " + e.getMessage());
                    Toast.makeText(DetailActivity.this, "获取失物信息失败", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void updateUI() {
        tvItemName.setText(lostItem.getItemName());
        tvCategory.setText(lostItem.getCategory());
        tvLostTime.setText(lostItem.getLostTime());
        tvLostLocation.setText(lostItem.getLostLocation());
        tvDescription.setText(lostItem.getDescription());
        tvPublishTime.setText(lostItem.getCreatedAt());

        if (lostItem.getOwner() != null) {
            tvOwner.setText(lostItem.getOwner().getUsername());
        } else {
            tvOwner.setText("未知发布者");
        }

        // 设置找回状态
        if (lostItem.getFound()) {
            tvUrgent.setText("已找回");
            tvUrgent.setTextColor(ContextCompat.getColor(this, R.color.green));
        } else {
            // 原有紧急程度显示逻辑
            if (lostItem.getUrgent() != null && lostItem.getUrgent()) {
                tvUrgent.setText("紧急");
                tvUrgent.setTextColor(ContextCompat.getColor(this, R.color.red));
            } else {
                tvUrgent.setText("一般");
                tvUrgent.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            }
        }

        // 图片处理：仅在存在图片时显示
        if (lostItem.getItemImage() != null && lostItem.getItemImage().getFileUrl() != null) {
            ivItemImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(lostItem.getItemImage().getFileUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.error_image)
                    .into(ivItemImage);
        } else {
            ivItemImage.setVisibility(View.GONE);
        }
    }

    private void setClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnContact.setOnClickListener(v -> {
            if (lostItem != null && lostItem.getContact() != null) {
                showContactOptions();
            }
        });

        btnSendComment.setOnClickListener(v -> {
            String commentContent = etComment.getText().toString().trim();
            if (commentContent.isEmpty()) {
                Toast.makeText(this, "评论内容不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            postComment(commentContent);
        });

        ivMore.setOnClickListener(v -> showMoreOptions());

        // 设置评论点击事件
        commentAdapter.setOnCommentClickListener(new CommentAdapter.OnCommentClickListener() {
            @Override
            public void onCommentClick(com.example.lostandfound.Comment comment) {
                // 回复评论
                showReplyDialog((Comment) comment);
            }

            @Override
            public void onAvatarClick(BmobUser user) {
                // 跳转到私信页面
                startPrivateChat((User) user);
            }
        });
    }

    private void showContactOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("联系发布者");

        // 检查联系方式类型
        String contact = lostItem.getContact();
        List<String> options = new ArrayList<>();
        List<DialogInterface.OnClickListener> actions = new ArrayList<>();

        // 添加电话选项（如果有电话）
        if (contact != null && !contact.contains("@")) {
            options.add("拨打电话: " + contact);
            actions.add((dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + contact));
                startActivity(intent);
            });
        }

        // 添加邮件选项（如果有邮箱）
        if (contact != null && contact.contains("@")) {
            options.add("发送邮件: " + contact);
            actions.add((dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + contact));
                startActivity(intent);
            });
        }

        // 添加私信选项（如果发布者不是当前用户）
        if (lostItem.getOwner() != null && !lostItem.getOwner().getObjectId().equals(currentUserId)) {
            options.add("发送私信");
            actions.add((dialog, which) -> {
                startPrivateChat(lostItem.getOwner());
            });
        }

        // 如果没有可用的联系方式
        if (options.isEmpty()) {
            builder.setMessage("该发布者未提供联系方式");
            builder.setPositiveButton("确定", null);
        } else {
            // 将选项转换为数组
            CharSequence[] items = options.toArray(new CharSequence[0]);
            builder.setItems(items, (dialog, which) -> {
                if (which >= 0 && which < actions.size()) {
                    actions.get(which).onClick(dialog, which);
                }
            });
        }

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void loadComments(LostItem lostItem) {
        if (lostItem == null) {
            Log.e("DetailActivity", "LostItem is null, cannot load comments");
            return;
        }

        BmobQuery<Comment> query = new BmobQuery<>();
        query.addWhereEqualTo("lostItem", new BmobPointer(lostItem));
        query.include("author,replyTo.author"); // 加载作者信息和回复对象的作者
        query.order("createdAt"); // 按创建时间排序（可选）
        query.findObjects(new FindListener<Comment>() {
            @Override
            public void done(List<Comment> comments, BmobException e) {
                if (e == null) {
                    Log.e("DetailActivity", "加载到评论数量: " + comments.size());
                    commentList.clear();
                    commentList.addAll(comments);
                    // 重要：检查Adapter是否初始化
                    if (commentAdapter == null) {
                        Log.e("DetailActivity", "CommentAdapter is null!");
                        commentAdapter = new CommentAdapter(commentList);
                        rvComments.setAdapter(commentAdapter);
                    } else {
                        commentAdapter.notifyDataSetChanged();
                    }

                    // 检查RecyclerView是否可见
                    if (rvComments.getVisibility() != View.VISIBLE) {
                        Log.d("DetailActivity", "RecyclerView was not visible, making it visible now");
                        rvComments.setVisibility(View.VISIBLE);
                    }

                    // 如果评论为空显示提示
                    if (comments.isEmpty()) {
                        Toast.makeText(DetailActivity.this, "暂无评论", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("DetailActivity", "加载评论失败：" + e.getMessage());
                    Toast.makeText(DetailActivity.this, "加载评论失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void postComment(String content) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(BmobUser.getCurrentUser(User.class));
        comment.setLostItem(lostItem);

        comment.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    etComment.setText("");
                    loadComments(lostItem);
                } else {
                    Toast.makeText(DetailActivity.this, "评论失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showReplyDialog(Comment comment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("回复 " + comment.getAuthor().getUsername());

        final EditText input = new EditText(this);
        input.setHint("输入回复内容");
        builder.setView(input);

        builder.setPositiveButton("发送", (dialog, which) -> {
            String replyContent = input.getText().toString().trim();
            if (!replyContent.isEmpty()) {
                postReply(comment, replyContent);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void postReply(Comment parentComment, String content) {
        if (lostItem == null || currentUserId == null) return;

        Comment reply = new Comment();
        reply.setContent(content);
        reply.setAuthor(BmobUser.getCurrentUser(User.class));
        reply.setLostItem(lostItem);
        reply.setReplyTo(parentComment);

        reply.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if (e == null) {
                    loadComments(lostItem);
                } else {
                    Toast.makeText(DetailActivity.this, "回复失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startPrivateChat(User user) {
        if (user == null || user.getObjectId() == null) {
            Toast.makeText(this, "用户信息无效", Toast.LENGTH_SHORT).show();
            return;
        }

        // 确保用户名不为空
        String username = user.getUsername() != null ? user.getUsername() : "未知用户";

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("receiver_id", user.getObjectId());
        intent.putExtra("receiver_name", username);
        startActivity(intent);
    }

    private void showMoreOptions() {
        PopupMenu popupMenu = new PopupMenu(this, ivMore);
        popupMenu.getMenuInflater().inflate(R.menu.menu_lost_detail, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_delete) {
                confirmDelete();
                return true;
            } else if (item.getItemId() == R.id.menu_mark_found) {
                markAsFound();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除这条失物信息吗？")
                .setPositiveButton("删除", (dialog, which) -> deleteLostItem())
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteLostItem() {
        lostItem.delete(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Toast.makeText(DetailActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(DetailActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void markAsFound() {
        if (lostItem == null) return;

        // 更新为已找回状态
        lostItem.setFound(true);

        // 更新到服务器
        lostItem.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(DetailActivity.this, "已标记为已找回", Toast.LENGTH_SHORT).show();
                        updateUI(); // 刷新UI显示新状态
                    });
                } else {
                    Toast.makeText(DetailActivity.this, "标记失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}