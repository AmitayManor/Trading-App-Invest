package com.example.invest.appPages;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
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

public class BuyActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView portfolioRecyclerView;
    private Button cancelButton;
    private StockAdapter stockAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        initViews();
        setupRecyclerView();
        setupSearchView();
        setupCancelButton();
    }

    private void initViews() {
        searchView = findViewById(R.id.search_view);
        portfolioRecyclerView = findViewById(R.id.portfolio_recycler_view);
        cancelButton = findViewById(R.id.cancel_button);
    }

    private void setupRecyclerView() {
        stockAdapter = new StockAdapter(getPortfolioStocks(), this::showBuyDialog);
        portfolioRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        portfolioRecyclerView.setAdapter(stockAdapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO: Implement stock search
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO: Implement real-time filtering
                return false;
            }
        });
    }

    private void setupCancelButton() {
        cancelButton.setOnClickListener(v -> finish());
    }

    private List<StockItem> getPortfolioStocks() {
        // TODO: Fetch actual portfolio stocks
        List<StockItem> stocks = new ArrayList<>();
        stocks.add(new StockItem("AAPL", "Apple Inc.", 150.25, 2.5, 10));
        stocks.add(new StockItem("GOOGL", "Alphabet Inc.", 2800.75, -0.8, 5));
        stocks.add(new StockItem("TSLA", "Tesla, Inc.", 900.40, 3.5, 8));
        return stocks;
    }

    private void showBuyDialog(StockItem stock) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_buy_sell, null);

        TextView symbolText = view.findViewById(R.id.stock_symbol);
        TextView priceText = view.findViewById(R.id.stock_price);
        TextView changeText = view.findViewById(R.id.stock_change);
        TextView totalText = view.findViewById(R.id.total_value);
        EditText sharesEdit = view.findViewById(R.id.shares_edit);
        Button minusButton = view.findViewById(R.id.minus_button);
        Button plusButton = view.findViewById(R.id.plus_button);
        Button buyButton = view.findViewById(R.id.action_button);
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
            shares.incrementAndGet();
            sharesEdit.setText(String.valueOf(shares.get()));
            updateTotalValue(totalText, shares.get(), stock.getPrice());
        });

        buyButton.setText("Buy");
        buyButton.setOnClickListener(v -> {
            // TODO: Implement buy confirmation
            Toast.makeText(this, "Bought " + shares.get() + " shares of " + stock.getSymbol(), Toast.LENGTH_SHORT).show();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateTotalValue(TextView totalText, int shares, double price) {
        double total = shares * price;
        totalText.setText(String.format("Total: $%.2f", total));
    }
}
