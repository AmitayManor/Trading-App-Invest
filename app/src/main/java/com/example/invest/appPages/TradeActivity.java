package com.example.invest.appPages;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.invest.R;
import com.example.invest.adapters.StockAdapter;
import com.example.invest.items.StockItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;


public class TradeActivity extends AppCompatActivity {

    private RecyclerView stocksRecyclerView;
    private StockAdapter stockAdapter;
    private SearchView searchView;
    private Button buyButton;
    private Button sellButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        initViews();
        setupRecyclerView();
        setupSearchView();
        setupButtons();
        setupBottomNavigation();
    }

    private void initViews() {
        stocksRecyclerView = findViewById(R.id.stocks_recycler_view);
        searchView = findViewById(R.id.search_view);
        buyButton = findViewById(R.id.buy_button);
        sellButton = findViewById(R.id.sell_button);
    }

    private void setupRecyclerView() {
        stockAdapter = new StockAdapter(getDummyStocks(), this::showStockDetails);
        stocksRecyclerView.setAdapter(stockAdapter);
        stocksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupButtons() {
        buyButton.setOnClickListener(v -> openBuyPage());
        sellButton.setOnClickListener(v -> openSellPage());
    }

    private void openBuyPage() {
        Intent intent = new Intent(this, BuyActivity.class);
        startActivity(intent);
    }

    private void openSellPage() {
        Intent intent = new Intent(this, SellActivity.class);
        startActivity(intent);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter stocks as user types
                return false;
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_trade);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent homeIntent = new Intent(TradeActivity.this, MainActivity.class);
                    startActivity(homeIntent);
                    return true;
                } else if (itemId == R.id.nav_news) {
                    Intent newsIntent = new Intent(TradeActivity.this, NewsActivity.class);
                    startActivity(newsIntent);
                    return true;
                } else if (itemId == R.id.nav_trade) {
                    // Already on Trade Activity, refresh the page
                    refreshTradePage();
                    return true;
                } else if (itemId == R.id.nav_portfolio) {
                    Intent portfolioIntent = new Intent(TradeActivity.this, PortfolioActivity.class);
                    startActivity(portfolioIntent);
                    return true;
                }
                return false;
            }
        });
    }

    private void refreshTradePage() {
        // Refresh stock list
        updateStockList();

        // Reset search view
        resetSearchView();

        // Optional: Show a refresh animation or message
        showRefreshIndicator();
    }

    private void updateStockList() {
        // TODO: Fetch the latest stock data from your data source
        // For now, we'll just update with new dummy data
        List<StockItem> newStocks = getDummyStocks();
        stockAdapter.updateStocks(newStocks);
        stockAdapter.notifyDataSetChanged();
    }

    private void resetSearchView() {
        searchView.setQuery("", false);
        searchView.clearFocus();
    }

    private void showRefreshIndicator() {
        // Show a short toast message
        Toast.makeText(this, "Refreshing stocks...", Toast.LENGTH_SHORT).show();

        // Optional: You could also implement a pull-to-refresh functionality
        // or show a progress bar during refresh
    }

    private List<StockItem> getDummyStocks() {
        List<StockItem> stocks = new ArrayList<>();
        stocks.add(new StockItem("AAPL", "Apple Inc.", 150.25, 2.5,10));
        stocks.add(new StockItem("GOOGL", "Alphabet Inc.", 2800.75, -0.8,8));
        stocks.add(new StockItem("TSLA", "Tesla, Inc.", 900.40, 3.5,7));
        stocks.add(new StockItem("AMZN", "Amazon.com, Inc.", 3300.00, 1.2,7));
        stocks.add(new StockItem("MSFT", "Microsoft Corporation", 280.50, 0.5,7));
        return stocks;
    }

    private void showStockDetails(StockItem stock) {
        // TODO: Implement stock details pop-up
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popupView = getLayoutInflater().inflate(R.layout.popup_stock_details, null);

        TextView symbolTextView = popupView.findViewById(R.id.popup_stock_symbol);
        TextView priceTextView = popupView.findViewById(R.id.popup_stock_price);
        TextView changeTextView = popupView.findViewById(R.id.popup_stock_change);
        ImageButton watchlistButton = popupView.findViewById(R.id.popup_watchlist_button);

        symbolTextView.setText(stock.getSymbol());
        priceTextView.setText(String.format("$%.2f", stock.getPrice()));
        changeTextView.setText(String.format("%.2f%%", stock.getChange()));
        changeTextView.setTextColor(stock.getChange() >= 0 ? getColor(R.color.positive) : getColor(R.color.negative));

        watchlistButton.setOnClickListener(v -> {
            // TODO: Implement add to watchlist functionality
            Toast.makeText(this, "Added to watchlist", Toast.LENGTH_SHORT).show();

            // TODO: Implement a filled star for watchlist
            //watchlistButton.setImageResource(R.drawable.ic_star_filled);
        });

        builder.setView(popupView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
