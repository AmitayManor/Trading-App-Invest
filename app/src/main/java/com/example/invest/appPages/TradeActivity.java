package com.example.invest.appPages;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ProgressBar;

import com.example.invest.R;
import com.example.invest.adapters.StockAdapter;
import com.example.invest.adapters.WatchlistAdapter;
import com.example.invest.handlers.FirebaseManager;
import com.example.invest.items.StockItem;
import com.example.invest.handlers.AlphaVantageAPI;
import com.example.invest.items.WatchlistItem;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TradeActivity extends AppCompatActivity {

    private static final String STOCK_SYMBOL = "STOCK_SYMBOL";

    private WatchlistAdapter watchlistAdapter;
    private RecyclerView watchlistRecyclerView;
    private RecyclerView stocksRecyclerView;
    private StockAdapter stockAdapter;
    private SearchView searchView;
    private ProgressBar loadingIndicator;
    private AlphaVantageAPI api;
    private FirebaseManager firebaseManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);
        api = AlphaVantageAPI.getInstance();
        firebaseManager = FirebaseManager.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        initViews();
        createSearchView();
        setupRecyclerView();
        setupSearchView();
        setupBottomNavigation();
        loadWatchlistData();
        loadPopularStocks();
    }

    private void createSearchView() {
        FrameLayout searchContainer = findViewById(R.id.search_container);
        searchView = new SearchView(this);
        searchView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search stocks");
        searchContainer.addView(searchView);
    }

    private void loadWatchlistData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        firebaseManager.getUserPortfolio(userId)
                .thenAccept(userPortfolio -> {
                    List<String> watchlist = userPortfolio.getWatchlist();
                    if (watchlist == null || watchlist.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(TradeActivity.this, "Watchlist is empty", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    for (String symbol : watchlist) {
                        api.getQuote(symbol)
                                .thenAccept(quote -> {
                                    WatchlistItem item = new WatchlistItem(
                                            symbol,
                                            quote.getPrice(),
                                            quote.getChangePercent()
                                    );
                                    runOnUiThread(() -> watchlistAdapter.addItem(item));
                                })
                                .exceptionally(error -> {
                                    runOnUiThread(() -> Toast.makeText(TradeActivity.this,
                                            "Error fetching data for " + symbol, Toast.LENGTH_SHORT).show());
                                    return null;
                                });
                    }
                })
                .exceptionally(error -> {
                    runOnUiThread(() -> Toast.makeText(TradeActivity.this,
                            "Error loading watchlist: " + error.getMessage(), Toast.LENGTH_LONG).show());
                    return null;
                });
    }

    private void loadPopularStocks() {
        List<String> popularSymbols = Arrays.asList("AAPL", "GOOGL", "MSFT", "AMZN", "FB");
        loadingIndicator.setVisibility(View.VISIBLE);
        for (String symbol : popularSymbols) {
            fetchStockData(symbol);
        }
    }


    private void fetchStockData(String symbol) {
        loadingIndicator.setVisibility(View.VISIBLE);
        api.getQuote(symbol).thenAccept(quoteResponse -> {
            StockItem item = new StockItem(
                    symbol,
                    quoteResponse.getPrice(),
                    quoteResponse.getChange(),
                    quoteResponse.getChangePercent(),
                    quoteResponse.getVolume()
            );
            runOnUiThread(() -> {
                List<StockItem> currentStocks = new ArrayList<>(stockAdapter.getStocks());
                currentStocks.add(0, item);
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
        watchlistRecyclerView = findViewById(R.id.watchlist_recycler_view);
        loadingIndicator = findViewById(R.id.loading_indicator);
    }

    private void setupRecyclerView() {
        stockAdapter = new StockAdapter(new ArrayList<>(), this::showStockDetails);
        stocksRecyclerView.setAdapter(stockAdapter);
        stocksRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        watchlistAdapter = new WatchlistAdapter(new ArrayList<>());
        watchlistRecyclerView.setAdapter(watchlistAdapter);
        watchlistRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchStockData(query.trim().toUpperCase());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Implement if you want to filter as the user types
                return false;
            }
        });
    }

    private void addToWatchlist(String userId, String symbol, ImageButton button) {
        firebaseManager.getUserPortfolio(userId).thenAccept(userPortfolio -> {
            userPortfolio.addToWatchlist(symbol);
            firebaseManager.updateUserWatchlist(userId, userPortfolio.getWatchlist())
                    .thenRun(() -> runOnUiThread(() -> {
                        updateWatchlistButton(button, true);
                        Toast.makeText(TradeActivity.this, symbol + " added to watchlist", Toast.LENGTH_SHORT).show();
                    }))
                    .exceptionally(error -> {
                        runOnUiThread(() -> Toast.makeText(TradeActivity.this, "Failed to add to watchlist", Toast.LENGTH_SHORT).show());
                        return null;
                    });
        });
    }

    private void removeFromWatchlist(String userId, String symbol, ImageButton button) {
        firebaseManager.getUserPortfolio(userId).thenAccept(userPortfolio -> {
            userPortfolio.removeFromWatchlist(symbol);
            firebaseManager.updateUserWatchlist(userId, userPortfolio.getWatchlist())
                    .thenRun(() -> runOnUiThread(() -> {
                        updateWatchlistButton(button, false);
                        Toast.makeText(TradeActivity.this, symbol + " removed from watchlist", Toast.LENGTH_SHORT).show();
                    }))
                    .exceptionally(error -> {
                        runOnUiThread(() -> Toast.makeText(TradeActivity.this, "Failed to remove from watchlist", Toast.LENGTH_SHORT).show());
                        return null;
                    });
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
        refreshStockData();
        resetSearchView();
    }

    private void resetSearchView() {
        searchView.setQuery("", false);
        searchView.clearFocus();
    }

    @SuppressLint("DefaultLocale")
    private void showStockDetails(StockItem stock) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popupView = getLayoutInflater().inflate(R.layout.popup_stock_details, null);

        TextView symbolTextView = popupView.findViewById(R.id.popup_stock_symbol);
        TextView priceTextView = popupView.findViewById(R.id.popup_stock_price);
        TextView changeTextView = popupView.findViewById(R.id.popup_stock_change);
        TextView volumeTextView = popupView.findViewById(R.id.popup_stock_volume);
        ImageButton watchlistButton = popupView.findViewById(R.id.popup_watchlist_button);
        LineChart chart = popupView.findViewById(R.id.popup_stock_chart);
        ImageButton popupBackButton = popupView.findViewById(R.id.popup_back_button);
        Button popupBuyButton = popupView.findViewById(R.id.popup_buy_button);
        Button popupSellButton = popupView.findViewById(R.id.popup_sell_button);


        symbolTextView.setText(stock.getSymbol());
        priceTextView.setText(String.format("$%.2f", stock.getPrice()));
        changeTextView.setText(String.format("%.2f%% (%.2f)",stock.getChangePercent(), stock.getChange()));
        changeTextView.setTextColor(stock.getChange() >= 0 ? getColor(R.color.positive) : getColor(R.color.negative));
        volumeTextView.setText(String.format("Vol: %2f", stock.getVolume()));
        setupStockChart(chart, stock.getSymbol());

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            firebaseManager.getUserPortfolio(userId).thenAccept(userPortfolio -> {
                boolean isInWatchlist = userPortfolio.getWatchlist().contains(stock.getSymbol());
                runOnUiThread(() -> {
                    updateWatchlistButton(watchlistButton, isInWatchlist);
                    watchlistButton.setVisibility(View.VISIBLE);
                });

                watchlistButton.setOnClickListener(v -> {
                    if (isInWatchlist) {
                        removeFromWatchlist(userId, stock.getSymbol(), watchlistButton);
                    } else {
                        addToWatchlist(userId, stock.getSymbol(), watchlistButton);
                    }
                });
            }).exceptionally(error -> {
                runOnUiThread(() -> Toast.makeText(TradeActivity.this, "Error fetching watchlist", Toast.LENGTH_SHORT).show());
                return null;
            });
        } else {
            watchlistButton.setVisibility(View.GONE);
        }

        builder.setView(popupView);
        AlertDialog dialog = builder.create();

        popupBackButton.setOnClickListener(v -> dialog.dismiss());

        popupBuyButton.setOnClickListener(v -> {
            Intent intent = new Intent(TradeActivity.this, BuyActivity.class);
            intent.putExtra(STOCK_SYMBOL, stock.getSymbol());
            startActivity(intent);
            dialog.dismiss();
        });

        popupSellButton.setOnClickListener(v -> {
            Intent intent = new Intent(TradeActivity.this, SellActivity.class);
            intent.putExtra(STOCK_SYMBOL, stock.getSymbol());
            startActivity(intent);
            dialog.dismiss();
        });

        dialog.show();
    }
    private void setupStockChart(LineChart chart, String symbol) {
        chart.setNoDataText("Loading chart data...");
        chart.invalidate();

        api.getWeeklyAdjusted(symbol).thenAccept(weeklyData -> {
            List<Entry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            for (int i = 0; i < Math.min(12, weeklyData.size()); i++) {
                AlphaVantageAPI.WeeklyAdjustedData data = weeklyData.get(i);
                entries.add(new Entry(i, data.getAdjustedClose().floatValue()));
                labels.add(data.getDate());
            }

            runOnUiThread(() -> {
                LineDataSet dataSet = new LineDataSet(entries, symbol + " Weekly Performance");
                dataSet.setColor(getColor(R.color.chart_line));
                dataSet.setValueTextColor(getColor(R.color.chart_values));
                dataSet.setDrawValues(false);
                dataSet.setDrawCircles(false);

                LineData lineData = new LineData(dataSet);
                chart.setData(lineData);

                XAxis xAxis = chart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);
                xAxis.setLabelRotationAngle(45);

                chart.getDescription().setEnabled(false);
                chart.getLegend().setEnabled(false);
                chart.getAxisRight().setEnabled(false);

                chart.invalidate();
            });
        }).exceptionally(error -> {
            runOnUiThread(() -> {
                chart.setNoDataText("Error loading chart data");
                chart.invalidate();
                Toast.makeText(TradeActivity.this, "Error fetching chart data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            });
            return null;
        });
    }


    private void updateWatchlistButton(ImageButton watchlistButton, boolean isInWatchlist) {
        watchlistButton.setImageResource(isInWatchlist ? R.drawable.ic_filled_star : R.drawable.ic_empty_star);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStockData();
    }

    private void refreshStockData() {
        List<StockItem> currentStocks = stockAdapter.getStocks();
        for (StockItem stock : currentStocks) {
            fetchStockData(stock.getSymbol());
        }
    }

}
