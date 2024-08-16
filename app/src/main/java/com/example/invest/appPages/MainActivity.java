package com.example.invest.appPages;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.invest.R;
import com.example.invest.adapters.WatchlistAdapter;
import com.example.invest.handlers.AlphaVantageAPI;
import com.example.invest.items.WatchlistItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AlphaVantageAPI api;

    private TextView portfolioValueTextView;
    private TextView portfolioChangeTextView;
    private RecyclerView watchlistRecyclerView;
    private WatchlistAdapter watchlistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        api = AlphaVantageAPI.getInstance();

        initViews();
        setupWatchlist();
        setupBottomNavigation();
        // Test Connection
        Button testButton = findViewById(R.id.test_api_button);
        testButton.setOnClickListener(v -> testApiConnection());
    }

    // Testing Connection
    private void testApiConnection() {
        api.testConnection(new AlphaVantageAPI.ConnectionTestCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show());
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void initViews() {
        portfolioValueTextView = findViewById(R.id.portfolio_value);
        portfolioChangeTextView = findViewById(R.id.portfolio_change);
        watchlistRecyclerView = findViewById(R.id.watchlist_recycler_view);
    }

    private void setupWatchlist() {
        watchlistAdapter = new WatchlistAdapter(getWatchlistData());
        watchlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        watchlistRecyclerView.setAdapter(watchlistAdapter);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {

                    refreshHomePage();
                    return true;
                } else if (itemId == R.id.nav_news) {
                    // Navigate to News Activity
                    Intent newsIntent = new Intent(MainActivity.this, NewsActivity.class);
                    startActivity(newsIntent);
                    return true;
                } else if (itemId == R.id.nav_trade) {
                    // Navigate to Trade Activity
                    Intent tradeIntent = new Intent(MainActivity.this, TradeActivity.class);
                    startActivity(tradeIntent);
                    return true;
                } else if (itemId == R.id.nav_portfolio) {
                    // Navigate to Portfolio Activity
                    Intent portfolioIntent = new Intent(MainActivity.this, PortfolioActivity.class);
                    startActivity(portfolioIntent);
                    return true;
                }
                return false;
            }
        });
    }

    private void refreshHomePage() {
        // Refresh portfolio value
        updatePortfolioValue();

        // Refresh performance chart
        updatePerformanceChart();

        // Refresh watchlist
        updateWatchlist();

        // Optional: Show a refresh animation or message
        showRefreshIndicator();
    }

    private void updatePortfolioValue() {
        // TODO: Fetch the latest portfolio value from your data source
        // For now, we'll just update with a random value
        double newValue = 10000 + Math.random() * 1000;
        TextView portfolioValueTextView = findViewById(R.id.portfolio_value);
        portfolioValueTextView.setText(String.format("$%.2f", newValue));

        // Update change percentage
        double changePercentage = (Math.random() * 10) - 5; // Random value between -5% and 5%
        TextView portfolioChangeTextView = findViewById(R.id.portfolio_change);
        portfolioChangeTextView.setText(String.format("%.2f%%", changePercentage));
        portfolioChangeTextView.setTextColor(changePercentage >= 0 ? getColor(R.color.positive) : getColor(R.color.negative));
    }

    private void updatePerformanceChart() {
        // TODO: Update the performance chart with the latest data
        // This will depend on how you've implemented your chart
        // For now, we'll just log a message
        Log.d("MainActivity", "Updating performance chart");
    }

    private void updateWatchlist() {
        // TODO: Fetch the latest watchlist data from your data source
        // For now, we'll just update the existing items with random values
        WatchlistAdapter adapter = (WatchlistAdapter) watchlistRecyclerView.getAdapter();
        if (adapter != null) {
            List<WatchlistItem> items = adapter.getWatchlistItems();
            for (WatchlistItem item : items) {
                double newPrice = item.getPrice() * (1 + (Math.random() * 0.1 - 0.05)); // ±5% change
                double newChange = (Math.random() * 10) - 5; // Random value between -5% and 5%
                item.setPrice(newPrice);
                item.setChange(newChange);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void showRefreshIndicator() {
        // Show a short toast message
        Toast.makeText(this, "Refreshing data...", Toast.LENGTH_SHORT).show();

        // Optional: You could also implement a pull-to-refresh functionality
        // or show a progress bar during refresh
    }

    private List<WatchlistItem> getWatchlistData() {
        // TODO: Replace with actual data from your data source
        List<WatchlistItem> watchlistItems = new ArrayList<>();
        watchlistItems.add(new WatchlistItem("AAPL", 150.25, 1.2));
        watchlistItems.add(new WatchlistItem("GOOGL", 2800.75, -0.8));
        watchlistItems.add(new WatchlistItem("TSLA", 900.40, 3.5));
        return watchlistItems;
    }
}