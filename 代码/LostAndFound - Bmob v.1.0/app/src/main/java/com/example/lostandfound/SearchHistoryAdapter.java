package com.example.lostandfound;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {

    private List<String> historyItems;
    private OnHistoryItemClickListener listener;

    public interface OnHistoryItemClickListener {
        void onHistoryItemClick(String keyword);
    }

    public SearchHistoryAdapter(List<String> historyItems, OnHistoryItemClickListener listener) {
        this.historyItems = historyItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String keyword = historyItems.get(position);
        holder.tvKeyword.setText(keyword);
        holder.itemView.setOnClickListener(v -> listener.onHistoryItemClick(keyword));
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvKeyword;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKeyword = itemView.findViewById(R.id.tvKeyword);
        }
    }
}