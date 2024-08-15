package com.example.invest.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.invest.R;
import com.example.invest.models.PortfolioHolding;

import java.util.List;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ViewHolder> {
    private List<PortfolioHolding> holdings;

    public PortfolioAdapter(List<PortfolioHolding> holdings) {
        this.holdings = holdings;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_portfolio_holding, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PortfolioHolding holding = holdings.get(position);
        holder.bind(holding);
    }

    @Override
    public int getItemCount() {
        return holdings.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView symbolTextView;
        TextView nameTextView;
        TextView sharesTextView;
        TextView valueTextView;
        TextView changeTextView;
        TextView percentageTextView;

        ViewHolder(View itemView) {
            super(itemView);
            symbolTextView = itemView.findViewById(R.id.holding_symbol);
            nameTextView = itemView.findViewById(R.id.holding_name);
            sharesTextView = itemView.findViewById(R.id.holding_shares);
            valueTextView = itemView.findViewById(R.id.holding_value);
            changeTextView = itemView.findViewById(R.id.holding_change);
            percentageTextView = itemView.findViewById(R.id.holding_percentage);
        }

        void bind(PortfolioHolding holding) {
            symbolTextView.setText(holding.getSymbol());
            nameTextView.setText(holding.getName());
            sharesTextView.setText(String.format("%d shares", holding.getShares()));
            valueTextView.setText(String.format("$%.2f", holding.getTotalValue()));
            changeTextView.setText(String.format("%.2f%%", holding.getChange()));
            changeTextView.setTextColor(holding.getChange() >= 0 ? itemView.getContext().getColor(R.color.positive) : itemView.getContext().getColor(R.color.negative));
            percentageTextView.setText(String.format("%.1f%% of portfolio", holding.getPortfolioPercentage()));
        }
    }
}
