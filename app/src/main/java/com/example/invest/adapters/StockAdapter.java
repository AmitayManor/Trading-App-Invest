package com.example.invest.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        StockItem stock = stocks.get(position);
        holder.symbolTextView.setText(stock.getSymbol());
        holder.nameTextView.setText(stock.getName());
        holder.priceTextView.setText(String.format("$%.2f", stock.getPrice()));
        holder.changeTextView.setText(String.format("%.2f%%", stock.getChange()));
        holder.changeTextView.setTextColor(stock.getChange() >= 0 ?
                holder.itemView.getContext().getColor(R.color.positive) :
                holder.itemView.getContext().getColor(R.color.negative));

        holder.itemView.setOnClickListener(v -> listener.onStockClick(stock));
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }

    public void updateStocks(List<StockItem> newStocks) {
        this.stocks = newStocks;
    }

    public static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView symbolTextView;
        TextView nameTextView;
        TextView priceTextView;
        TextView changeTextView;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            symbolTextView = itemView.findViewById(R.id.stock_symbol);
            nameTextView = itemView.findViewById(R.id.stock_name);
            priceTextView = itemView.findViewById(R.id.stock_price);
            changeTextView = itemView.findViewById(R.id.stock_change);
        }

        void bind(final StockItem stock, final OnStockClickListener listener) {
            symbolTextView.setText(stock.getSymbol());
            nameTextView.setText(stock.getName());
            priceTextView.setText(String.format("$%.2f", stock.getPrice()));
            changeTextView.setText(String.format("%.2f%%", stock.getChange()));
            changeTextView.setTextColor(stock.getChange() >= 0 ? itemView.getContext().getColor(R.color.positive) : itemView.getContext().getColor(R.color.negative));

            itemView.setOnClickListener(v -> listener.onStockClick(stock));
        }
    }
}