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

import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.invest.R;
import com.example.invest.adapters.StockAdapter;
import com.example.invest.items.StockItem;
import com.example.invest.handlers.AlphaVantageAPI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;


public class TradeActivity extends AppCompatActivity {

    private RecyclerView stocksRecyclerView;
    private StockAdapter stockAdapter;
    private SearchView searchView;
    private ProgressBar loadingIndicator;
    private EditText symbolEditText;
    private Button searchButton;
    private AlphaVantageAPI api;
    private Button buyButton;
    private Button sellButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        initViews();
        setupRecyclerView();
        api = AlphaVantageAPI.getInstance();
        setupSearchView();
        setupSearchQueries();
        setupButtons();
        setupBottomNavigation();
    }

    private void setupSearchQueries() {
        searchButton.setOnClickListener(v -> {
            String symbol = symbolEditText.getText().toString().trim().toUpperCase();
            if (!symbol.isEmpty()) {
                fetchStockData(symbol);
            } else {
                Toast.makeText(this, "Please enter a stock symbol", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchStockData(String symbol) {
        loadingIndicator.setVisibility(View.VISIBLE);
        api.getQuote(symbol).thenAccept(quoteResponse -> {
            StockItem item = new StockItem(
                    symbol,
                    //quoteResponse.getName(),
                    quoteResponse.getPrice(),
                    quoteResponse.getChange(),
                    quoteResponse.getChangePercent(),
                    quoteResponse.getVolume()
            );
            runOnUiThread(() -> {
                List<StockItem> currentStocks = new ArrayList<>(stockAdapter.getStocks());
                currentStocks.add(0, item);  // Add new item at the top of the list
                stockAdapter.updateStocks(currentStocks);
                loadingIndicator.setVisibility(View.GONE);
                Toast.makeText(TradeActivity.this, "Stock data fetched successfully", Toast.LENGTH_SHORT).show();
            });
        }).exceptionally(error -> {
            runOnUiThread(() -> {
                loadingIndicator.setVisibility(View.GONE);
                Toast.makeText(TradeActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_LONG).show();
            });
            return null;
        });
    }

    private void initViews() {
        stocksRecyclerView = findViewById(R.id.stocks_recycler_view);
        searchView = findViewById(R.id.search_view);
        loadingIndicator = findViewById(R.id.loading_indicator);
        symbolEditText = findViewById(R.id.symbol_edit_text);
        searchButton = findViewById(R.id.search_button);
        buyButton = findViewById(R.id.buy_button);
        sellButton = findViewById(R.id.sell_button);
    }

    private void setupRecyclerView() {
        stockAdapter = new StockAdapter(new ArrayList<>(), new StockAdapter.OnStockClickListener(){

            @Override
            public void onStockClick(StockItem stock) {
                showStockDetails(stock);
            }
        });
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

    //TODO: delete this function - for tests only
    private List<StockItem> getDummyStocks() {
        List<StockItem> stocks = new ArrayList<>();
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
