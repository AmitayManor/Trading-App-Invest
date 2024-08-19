package com.example.invest.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.invest.items.StockItem;
import com.example.invest.R;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private List<StockItem> stocks;
    private OnStockClickListener listener;

    public interface OnStockClickListener {
        void onStockClick(StockItem stock);
    }

    public StockAdapter(List<StockItem> stocks, OnStockClickListener listener) {
        this.stocks = stocks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stock, parent, false);
        return new StockViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        StockItem stock = stocks.get(position);
        holder.bind(stock, listener);
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }

//    public List<StockItem> getStocks() {
//        return new ArrayList<>(stocks);
//    }
    public List<StockItem> getStocks() {
        return stocks;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateStocks(List<StockItem> newStocks) {
        this.stocks = newStocks;
        notifyDataSetChanged();
    }

    public static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView symbolTextView;
        TextView priceTextView;
        TextView changeTextView;
        TextView volumeTextView;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            symbolTextView = itemView.findViewById(R.id.stock_symbol);
            priceTextView = itemView.findViewById(R.id.stock_price);
            changeTextView = itemView.findViewById(R.id.stock_change);
            volumeTextView = itemView.findViewById(R.id.stock_volume);
        }

        @SuppressLint("DefaultLocale")
        void bind(final StockItem stock, final OnStockClickListener listener) {
            symbolTextView.setText(stock.getSymbol());
            priceTextView.setText(String.format("$%.2f", stock.getPrice()));
            changeTextView.setText(String.format("%.2f%%", stock.getChange()));
            changeTextView.setTextColor(stock.getChange() >= 0 ? itemView.getContext().getColor(R.color.positive) : itemView.getContext().getColor(R.color.negative));
            volumeTextView.setText(String.format("Vol: %.2f", stock.getVolume()));

            itemView.setOnClickListener(v -> listener.onStockClick(stock));
        }
    }
}