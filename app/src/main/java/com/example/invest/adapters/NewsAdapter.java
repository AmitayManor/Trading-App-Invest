package com.example.invest.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.invest.R;
import com.example.invest.items.NewsItem;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<NewsItem> newsItems;

    public NewsAdapter(List<NewsItem> newsItems) {
        this.newsItems = newsItems;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem item = newsItems.get(position);
        holder.titleTextView.setText(item.getTitle());
        holder.summaryTextView.setText(item.getSummary());
        holder.sourceTextView.setText(item.getSource());
        holder.timeAgoTextView.setText(item.getTimeAgo());
    }

    @Override
    public int getItemCount() {
        return newsItems.size();
    }

    public void updateNewsItems(List<NewsItem> newItems) {
        this.newsItems = newItems;
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView summaryTextView;
        TextView sourceTextView;
        TextView timeAgoTextView;

        NewsViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.news_title);
            summaryTextView = itemView.findViewById(R.id.news_summary);
            sourceTextView = itemView.findViewById(R.id.news_source);
            timeAgoTextView = itemView.findViewById(R.id.news_time_ago);
        }
    }
}
