package com.example.invest.appPages;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.invest.R;
import com.example.invest.adapters.StockAdapter;
import com.example.invest.items.StockItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SellActivity extends AppCompatActivity {

    private RecyclerView portfolioRecyclerView;
    private Button cancelButton;
    private StockAdapter stockAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        initViews();
        setupRecyclerView();
        setupCancelButton();
    }

    private void initViews() {
        portfolioRecyclerView = findViewById(R.id.portfolio_recycler_view);
        cancelButton = findViewById(R.id.cancel_button);
    }

    private void setupRecyclerView() {
        stockAdapter = new StockAdapter(getPortfolioStocks(), this::showSellDialog);
        portfolioRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        portfolioRecyclerView.setAdapter(stockAdapter);
    }

    private void setupCancelButton() {
        cancelButton.setOnClickListener(v -> finish());
    }

    private List<StockItem> getPortfolioStocks() {
        // TODO: Fetch actual portfolio stocks
        List<StockItem> stocks = new ArrayList<>();
        return stocks;
    }

    private void showSellDialog(StockItem stock) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_buy_sell, null);

        TextView symbolText = view.findViewById(R.id.stock_symbol);
        TextView priceText = view.findViewById(R.id.stock_price);
        TextView changeText = view.findViewById(R.id.stock_change);
        TextView totalText = view.findViewById(R.id.total_value);
        EditText sharesEdit = view.findViewById(R.id.shares_edit);
        Button minusButton = view.findViewById(R.id.minus_button);
        Button plusButton = view.findViewById(R.id.plus_button);
        Button sellButton = view.findViewById(R.id.action_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);

        symbolText.setText(stock.getSymbol());
        priceText.setText(String.format("$%.2f", stock.getPrice()));
        changeText.setText(String.format("%.2f%%", stock.getChange()));
        changeText.setTextColor(stock.getChange() >= 0 ? getColor(R.color.positive) : getColor(R.color.negative));

        AtomicInteger shares = new AtomicInteger(1);
        sharesEdit.setText(String.valueOf(shares.get()));

        updateTotalValue(totalText, shares.get(), stock.getPrice());

        builder.setView(view);
        final AlertDialog dialog = builder.create();

        minusButton.setOnClickListener(v -> {
            if (shares.get() > 1) {
                shares.decrementAndGet();
                sharesEdit.setText(String.valueOf(shares.get()));
                updateTotalValue(totalText, shares.get(), stock.getPrice());
            }
        });

        plusButton.setOnClickListener(v -> {
            //TODO: implement maximum value instead of hardcoded: 5
            if (shares.get() < 5) {
                shares.incrementAndGet();
                sharesEdit.setText(String.valueOf(shares.get()));
                updateTotalValue(totalText, shares.get(), stock.getPrice());
            }
        });

        sellButton.setText("Sell");
        sellButton.setOnClickListener(v -> {
            // TODO: Implement sell confirmation
            Toast.makeText(this, "Sold " + shares.get() + " shares of " + stock.getSymbol(), Toast.LENGTH_SHORT).show();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateTotalValue(TextView totalText, int shares, double price) {
        double total = shares * price;
        totalText.setText(String.format("Total: $%.2f", total));
    }
}