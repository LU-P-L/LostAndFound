package com.example.lostandfound;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class MyLostItemAdapter extends RecyclerView.Adapter<MyLostItemAdapter.ViewHolder> {

    private List<LostItem> items;
    private boolean showStatus;
    private OnItemClickListener listener;
    private OnStatusClickListener statusListener; // 新增状态点击监听器

    public MyLostItemAdapter(List<LostItem> items, boolean showStatus) {
        this.items = items;
        this.showStatus = showStatus;
    }

    // 设置整个条目的点击监听器
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // 设置状态区域的点击监听器
    public void setOnStatusClickListener(OnStatusClickListener listener) {
        this.statusListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lost_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LostItem item = items.get(position);

        holder.tvItemName.setText(item.getItemName());
        holder.tvLocation.setText(item.getLostLocation());
        holder.tvTime.setText(item.getLostTime());
        holder.tvDescription.setText(item.getDescription());

        // 加载图片
        if (item.getImageUrl() != null && item.getImageUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.placeholder_image);
        }

        // 显示状态
        if (showStatus) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            if (item.getFound() != null && item.getFound()) {
                holder.tvStatus.setText("已找回");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_found);
            } else {
                holder.tvStatus.setText("未找回");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_not_found);
            }
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }

        // 整个条目的点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item); // 传递整个LostItem对象
            }
        });

        // 状态区域的点击事件
        holder.tvStatus.setOnClickListener(v -> {
            if (statusListener != null) {
                statusListener.onStatusClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvItemName, tvLocation, tvTime, tvDescription, tvStatus;

        ViewHolder(View view) {
            super(view);
            ivImage = view.findViewById(R.id.iv_image);
            tvItemName = view.findViewById(R.id.tv_item_name);
            tvLocation = view.findViewById(R.id.tv_location);
            tvTime = view.findViewById(R.id.tv_time);
            tvDescription = view.findViewById(R.id.tv_description);
            tvStatus = view.findViewById(R.id.tv_status);
        }
    }

    // 传递LostItem对象
    public interface OnItemClickListener {
        void onItemClick(LostItem item);
    }

    // 处理状态区域的点击
    public interface OnStatusClickListener {
        void onStatusClick(int position);
    }
}