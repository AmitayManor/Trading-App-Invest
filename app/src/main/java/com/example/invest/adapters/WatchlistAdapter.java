package com.example.invest.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.invest.R;
import com.example.invest.items.WatchlistItem;

import java.util.List;
import java.util.ArrayList;

public class WatchlistAdapter extends RecyclerView.Adapter<WatchlistAdapter.ViewHolder> {
    private List<WatchlistItem> watchlistItems;

    public WatchlistAdapter(List<WatchlistItem> watchlistItems) {
        this.watchlistItems = watchlistItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_watchlist, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WatchlistItem item = watchlistItems.get(position);
        holder.symbolTextView.setText(item.getSymbol());
        holder.priceTextView.setText(String.format("$%.2f", item.getPrice()));
        holder.changeTextView.setText(String.format("%.1f%%", item.getChange()));
        holder.changeTextView.setTextColor(item.getChange() >= 0 ? holder.itemView.getContext().getColor(R.color.positive) : holder.itemView.getContext().getColor(R.color.negative));
    }
    @SuppressLint("NotifyDataSetChanged")
    public void updateItems(List<WatchlistItem> newItems) {
        this.watchlistItems.clear();
        this.watchlistItems.addAll(newItems);
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return watchlistItems.size();
    }

    public List<WatchlistItem> getWatchlistItems() {
        return watchlistItems;
    }


    @SuppressLint("NotifyDataSetChanged")
    public void updateWatchlist(List<WatchlistItem> newItems) {
        this.watchlistItems.clear();
        this.watchlistItems.addAll(newItems);
        notifyDataSetChanged();
    }

    public void addItem(WatchlistItem item) {
        watchlistItems.add(item);
        notifyItemInserted(watchlistItems.size() - 1);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearItems() {
        watchlistItems.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView symbolTextView;
        TextView priceTextView;
        TextView changeTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            symbolTextView = itemView.findViewById(R.id.watchlist_item_symbol);
            priceTextView = itemView.findViewById(R.id.watchlist_item_price);
            changeTextView = itemView.findViewById(R.id.watchlist_item_change);
        }
    }
}