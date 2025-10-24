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

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
    private List<Conversation> conversationList;
    private OnItemClickListener listener;
    public static final int MENU_DELETE = 0;
    public static final int MENU_MARK_UNREAD = 1;
    private int swipePosition = -1;

    public ConversationAdapter(List<Conversation> conversationList) {
        this.conversationList = conversationList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);
        holder.tvUsername.setText(conversation.getUsername());
        holder.tvLastMessage.setText(conversation.getLastMessage());

        if (conversation.getUnreadCount() > 0) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(conversation.getUnreadCount()));
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }

        if (conversation.getAvatarUrl() != null && !conversation.getAvatarUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(conversation.getAvatarUrl())
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(conversation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Conversation conversation);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername, tvLastMessage, tvUnreadCount;

        ViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvUnreadCount = itemView.findViewById(R.id.tv_unread_count);
        }
    }
}
