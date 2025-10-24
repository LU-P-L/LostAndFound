package com.example.lostandfound;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> commentList;
    private OnCommentClickListener listener;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    public void setOnCommentClickListener(OnCommentClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.bind(comment);

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCommentClick(comment);
            }
        });

        // 头像点击事件
        holder.ivAvatar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAvatarClick(comment.getAuthor());
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvUsername;
        private TextView tvTime;
        private TextView tvContent;
        private TextView tvReplyTo;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvReplyTo = itemView.findViewById(R.id.tv_reply_to);
        }

        public void bind(Comment comment) {
            // 检查评论有效性
            if (comment == null || comment.getLostItem() == null) {
                Log.e("CommentAdapter", "无效评论或未关联LostItem");
                return; // 跳过无效数据
            }

            // 绑定作者信息
            Log.e("CommentAdapter", "正在绑定评论: " + comment.getContent());
            if (comment.getAuthor() != null) {
                tvUsername.setText(comment.getAuthor().getUsername());
            } else {
                tvUsername.setText("匿名用户");
            }

            // 绑定其他数据
            tvContent.setText(comment.getContent());
            tvTime.setText(comment.getCreatedAt());

            // 处理回复逻辑
            if (comment.getReplyTo() != null && comment.getReplyTo().getAuthor() != null) {
                tvReplyTo.setText("回复 @" + comment.getReplyTo().getAuthor().getUsername());
                tvReplyTo.setVisibility(View.VISIBLE);
            } else {
                tvReplyTo.setVisibility(View.GONE);
            }
        }
    }

    public interface OnCommentClickListener {
        void onCommentClick(Comment comment);
        void onAvatarClick(BmobUser user);
    }

    public static class MyLostItemsActivity extends AppCompatActivity {
        private RecyclerView recyclerView;
        private LostItemAdapter adapter;
        private List<LostItem> lostItemList = new ArrayList<>();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_my_lost_items);

            recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new LostItemAdapter(lostItemList, new LostItemAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(LostItem item) {
                    // 点击行为
                }
            });

            recyclerView.setAdapter(adapter);

            loadMyLostItems();
        }

        private void loadMyLostItems() {
            User user = BmobUser.getCurrentUser(User.class);
            if (user == null) return;

            BmobQuery<LostItem> query = new BmobQuery<>();
            query.addWhereEqualTo("user", user);
            query.order("-createdAt");
            query.findObjects(new FindListener<LostItem>() {
                @Override
                public void done(List<LostItem> list, BmobException e) {
                    if (e == null) {
                        lostItemList.clear();
                        lostItemList.addAll(list);
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }
}