package com.example.lostandfound;

import android.icu.text.SimpleDateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.type.Date;

import java.util.List;
import java.util.Locale;

public class LostItemAdapter extends RecyclerView.Adapter<LostItemAdapter.ViewHolder> {

    private List<LostItem> lostItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LostItem item);
    }

    public LostItemAdapter(List<LostItem> lostItems, OnItemClickListener listener) {
        this.lostItems = lostItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lost, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LostItem item = lostItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return lostItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemImage;
        TextView tvItemName;
        TextView tvCategory;
        TextView tvLostTime;
        TextView tvLostLocation;
        TextView tvOwner;
        ImageView ivUrgency;
        TextView tvContact;
        TextView tvPublishTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemImage = itemView.findViewById(R.id.ivItemImage);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvLostTime = itemView.findViewById(R.id.tvLostTime);
            tvLostLocation = itemView.findViewById(R.id.tvLostLocation);
            tvOwner = itemView.findViewById(R.id.tvOwner);
            ivUrgency = itemView.findViewById(R.id.ivUrgency);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvPublishTime = itemView.findViewById(R.id.tv_publish_time);
        }

        public void bind(final LostItem item, final OnItemClickListener listener) {
            if (item.getItemImage() != null) {
                Glide.with(itemView.getContext())
                        .load(item.getItemImage().getFileUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(ivItemImage);
            } else {
                ivItemImage.setImageResource(R.drawable.placeholder_image);
            }

            tvItemName.setText(item.getItemName());
            tvCategory.setText(item.getCategory());
            tvLostTime.setText("时间: " + item.getLostTime());
            tvLostLocation.setText("地点: " + item.getLostLocation());
            tvContact.setText("联系方式: " + item.getContact());
            if (item.getOwner() != null) {
                tvOwner.setText(item.getOwner().getUsername());
            } else {
                tvOwner.setText("未知发布者");
            }

            // 设置紧急程度
            setUrgencyDisplay(item.getUrgent());

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        private void setUrgencyDisplay(boolean isUrgent) {
            if (isUrgent) {
                ivUrgency.setVisibility(View.VISIBLE);
                ivUrgency.setImageResource(R.drawable.ic_urgent_star); // 紧急时显示的星星图标
                ivUrgency.setContentDescription("紧急");
            } else {
                ivUrgency.setVisibility(View.GONE); // 不紧急时隐藏图标
            }
        }
    }
}