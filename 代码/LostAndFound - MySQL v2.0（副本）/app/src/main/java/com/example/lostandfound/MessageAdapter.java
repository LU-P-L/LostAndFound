package com.example.lostandfound;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SENT = 0;
    private static final int TYPE_RECEIVED = 1;
    private static final int TYPE_TIME = 2;

    private List<Message> messages;
    private String currentUserId;

    public MessageAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);

        // 如果是时间分隔项
        if (message.getContent().startsWith("TIME_SEPARATOR")) {
            return TYPE_TIME;
        }

        return message.getSender().getId().equals(currentUserId) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_SENT) {
            View view = inflater.inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else if (viewType == TYPE_RECEIVED) {
            View view = inflater.inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_time_separator, parent, false);
            return new TimeSeparatorViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        } else if (holder instanceof TimeSeparatorViewHolder) {
            ((TimeSeparatorViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // 发送的消息ViewHolder
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {

        private static final String BASE_URL = "http://172.20.10.6:8080";

        TextView tvContent, tvTime;
        ImageView ivAvatar;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }

        void bind(Message message) {
            tvContent.setText(message.getContent());
            tvTime.setText(formatTime(message.getCreatedAt()));

            // 优先使用User对象中的头像，如果没有则使用Message中的头像URL
            String avatarUrlRaw = message.getSender() != null && message.getSender().getAvatarUrl() != null
                    ? message.getSender().getAvatarUrl()
                    : message.getSenderAvatar();
            String avatarUrl = null;
            if (avatarUrlRaw != null && !avatarUrlRaw.isEmpty()) {
                if (avatarUrlRaw.startsWith("http")) {
                    avatarUrl = avatarUrlRaw;
                } else {
                    avatarUrl = BASE_URL + avatarUrlRaw;
                }
            }

            Log.d("MessageAdapter", "Sender AvatarUrl: " + avatarUrl);

            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(avatarUrl)
                        .circleCrop()
                        .placeholder(R.drawable.default_image)
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private static final String BASE_URL = "http://172.20.10.6:8080";

        TextView tvContent, tvTime, tvName;
        ImageView ivAvatar;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvName = itemView.findViewById(R.id.tv_name);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
        }

        void bind(Message message) {
            tvContent.setText(message.getContent());
            tvTime.setText(formatTime(message.getCreatedAt()));
            tvName.setText(message.getSender().getUsername());

            // 优先使用User对象中的头像，如果没有则使用Message中的头像URL
            String avatarUrlRaw = message.getSender() != null && message.getSender().getAvatarUrl() != null
                    ? message.getSender().getAvatarUrl()
                    : message.getSenderAvatar();
            String avatarUrl = null;
            if (avatarUrlRaw != null && !avatarUrlRaw.isEmpty()) {
                if (avatarUrlRaw.startsWith("http")) {
                    avatarUrl = avatarUrlRaw;
                } else {
                    avatarUrl = BASE_URL + avatarUrlRaw;
                }
            }

            Log.d("MessageAdapter", "Receiver AvatarUrl: " + avatarUrl);

            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(avatarUrl)
                        .circleCrop()
                        .placeholder(R.drawable.default_image)
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }
        }
    }

    // 时间分隔ViewHolder
    static class TimeSeparatorViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;

        TimeSeparatorViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        void bind(Message message) {
            // 提取时间字符串
            String timeStr = message.getContent().substring(14);
            tvTime.setText(timeStr);
        }
    }

    // 格式化时间
    private static String formatTime(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(dateStr);

            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            // 尝试简化处理
            if (dateStr != null && dateStr.length() > 11) {
                return dateStr.substring(11, 16); // 提取"HH:mm"部分
            }
            return dateStr;
        }
    }
}