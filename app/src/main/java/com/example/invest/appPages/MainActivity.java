package com.example.invest.appPages;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.invest.R;
import com.example.invest.adapters.WatchlistAdapter;
import com.example.invest.handlers.AlphaVantageAPI;
import com.example.invest.items.WatchlistItem;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.example.invest.handlers.FirebaseManager;
import com.example.invest.models.UserPortfolio;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    private AlphaVantageAPI alphaVantageAPI;
    private FirebaseManager firebaseManager;
    private TextView portfolioValueTextView;
    private TextView portfolioChangeTextView;
    private RecyclerView watchlistRecyclerView;
    private WatchlistAdapter watchlistAdapter;
    private LineChart performanceChart;
    private FirebaseAuth mAuth;
    private ProgressBar loadingIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        alphaVantageAPI = AlphaVantageAPI.getInstance();
        firebaseManager = FirebaseManager.getInstance();

        initViews();
        setupWatchlist();
        setupBottomNavigation();
        loadUserData();
        loadWatchlistData();
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            firebaseManager.getUserPortfolio(userId)
                    .thenAccept(this::updateUI)
                    .exceptionally(error -> {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error loading user data: " + error.getMessage(), Toast.LENGTH_LONG).show());
                        return null;
                    });
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_LONG).show();
            transactToLoginActivity();
        }
    }

    private void transactToLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        portfolioValueTextView = findViewById(R.id.portfolio_value);
        portfolioChangeTextView = findViewById(R.id.portfolio_change);
        watchlistRecyclerView = findViewById(R.id.watchlist_recycler_view);
        performanceChart = findViewById(R.id.performance_chart);
    }

    private void setupWatchlist() {
        watchlistAdapter = new WatchlistAdapter(new ArrayList<>());
        watchlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        watchlistRecyclerView.setAdapter(watchlistAdapter);
    }

    private void updateUI(UserPortfolio userPortfolio) {
        if (userPortfolio != null) {
            updatePortfolioValue(userPortfolio);
            setupPerformanceChart(userPortfolio);
            updateWatchlist(userPortfolio);
        } else {
            Toast.makeText(this, "Failed to load portfolio data", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupPerformanceChart(UserPortfolio userPortfolio) {
        loadingIndicator.setVisibility(View.VISIBLE);

        // Get the symbols of all stocks in the portfolio
        List<String> symbols = new ArrayList<>(userPortfolio.getHoldings().keySet());

        if (symbols.isEmpty()) {
            runOnUiThread(() -> {
                loadingIndicator.setVisibility(View.GONE);
                performanceChart.setNoDataText("No stocks in portfolio");
                performanceChart.invalidate();
            });
            return;
        }

        List<CompletableFuture<List<AlphaVantageAPI.WeeklyAdjustedData>>> futures = new ArrayList<>();

        for (String symbol : symbols) {
            futures.add(alphaVantageAPI.getWeeklyAdjusted(symbol));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> {
                    List<Entry> entries = new ArrayList<>();
                    List<String> labels = new ArrayList<>();
                    Map<String, Float> latestValues = new HashMap<>();

                    for (int i = 0; i < futures.size(); i++) {
                        try {
                            List<AlphaVantageAPI.WeeklyAdjustedData> weeklyData = futures.get(i).get();
                            if (!weeklyData.isEmpty()) {
                                latestValues.put(symbols.get(i), weeklyData.get(0).getAdjustedClose().floatValue());
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            Log.e("MainActivity", "Error fetching data for " + symbols.get(i), e);
                        }
                    }

                    // Calculate portfolio value for each week
                    for (int week = 0; week < 12; week++) {
                        float weekValue = 0;
                        for (int i = 0; i < futures.size(); i++) {
                            try {
                                List<AlphaVantageAPI.WeeklyAdjustedData> weeklyData = futures.get(i).get();
                                if (week < weeklyData.size()) {
                                    AlphaVantageAPI.WeeklyAdjustedData data = weeklyData.get(week);
                                    float stockValue = data.getAdjustedClose().floatValue() * userPortfolio.getHoldings().get(symbols.get(i)).getQuantity();
                                    weekValue += stockValue;
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                Log.e("MainActivity", "Error calculating portfolio value", e);
                            }
                        }
                        entries.add(new Entry(week, weekValue));
                        labels.add("Week " + (week + 1));
                    }

                    runOnUiThread(() -> {
                        loadingIndicator.setVisibility(View.GONE);
                        LineDataSet dataSet = new LineDataSet(entries, "Portfolio Performance");
                        dataSet.setColor(getColor(R.color.chart_line));
                        dataSet.setValueTextColor(getColor(R.color.chart_values));
                        dataSet.setDrawValues(false);
                        dataSet.setDrawCircles(false);

                        LineData lineData = new LineData(dataSet);
                        performanceChart.setData(lineData);

                        XAxis xAxis = performanceChart.getXAxis();
                        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setGranularity(1f);
                        xAxis.setLabelRotationAngle(45);

                        performanceChart.getDescription().setEnabled(false);
                        performanceChart.getLegend().setEnabled(false);
                        performanceChart.invalidate();
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        loadingIndicator.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Error loading performance data: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    });
                    return null;
                });
    }



//    @SuppressLint("DefaultLocale")
//    private void setupPortfolioSummary(UserPortfolio userPortfolio) {
//        double totalValue = userPortfolio.getTotalPortfolioValue();
//        double cashBalance = userPortfolio.getCashBalance();
//        double investedValue = totalValue - cashBalance;
//        double change = investedValue > 0 ? ((totalValue - cashBalance) / investedValue - 1) * 100 : 0;
//
//        runOnUiThread(() -> {
//            portfolioValueTextView.setText(String.format("$%.2f", totalValue));
//            portfolioChangeTextView.setText(String.format("%.2f%%", change));
//            portfolioChangeTextView.setTextColor(change >= 0 ? getColor(R.color.positive) : getColor(R.color.negative));
//        });
//    }

@SuppressLint("DefaultLocale")
private void updatePortfolioValue(UserPortfolio userPortfolio) {
    loadingIndicator.setVisibility(View.VISIBLE);

    List<CompletableFuture<Double>> futures = new ArrayList<>();

    for (Map.Entry<String, UserPortfolio.Holdings> entry : userPortfolio.getHoldings().entrySet()) {
        String symbol = entry.getKey();
        int quantity = entry.getValue().getQuantity();

        CompletableFuture<Double> future = alphaVantageAPI.getQuote(symbol)
                .thenApply(quote -> quote.getPrice() * quantity);

        futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                double totalValue = userPortfolio.getCashBalance();
                for (CompletableFuture<Double> future : futures) {
                    try {
                        totalValue += future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        Log.e("MainActivity", "Error calculating portfolio value", e);
                    }
                }
                return totalValue;
            })
            .thenAccept(totalValue -> {
                double initialInvestment = userPortfolio.getInitialInvestment();
                double totalChange = totalValue - initialInvestment;
                double totalChangePercent = (totalChange / initialInvestment) * 100;

                runOnUiThread(() -> {
                    loadingIndicator.setVisibility(View.GONE);
                    portfolioValueTextView.setText(String.format("$%.2f", totalValue));
                    portfolioChangeTextView.setText(String.format("%.2f%% (%.2f)", totalChangePercent, totalChange));
                    portfolioChangeTextView.setTextColor(totalChange >= 0 ? getColor(R.color.positive) : getColor(R.color.negative));
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    loadingIndicator.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Error updating portfolio value: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
                return null;
            });
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
        watchlistAdapter.clearItems();
        loadUserData();
    }


    private void updateWatchlist(UserPortfolio userPortfolio) {
        loadingIndicator.setVisibility(View.VISIBLE);
        List<String> watchlist = userPortfolio.getWatchlist();

        if (watchlist == null || watchlist.isEmpty()) {
            runOnUiThread(() -> {
                loadingIndicator.setVisibility(View.GONE);
                watchlistAdapter.clearItems();
                Toast.makeText(MainActivity.this, "Watchlist is empty", Toast.LENGTH_SHORT).show();
            });
            return;
        }

        List<CompletableFuture<WatchlistItem>> futures = new ArrayList<>();

        for (String symbol : watchlist) {
            CompletableFuture<WatchlistItem> future = alphaVantageAPI.getQuote(symbol)
                    .thenApply(quote -> new WatchlistItem(
                            symbol,
                            quote.getPrice(),
                            quote.getChangePercent()
                    ));

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<WatchlistItem> items = new ArrayList<>();
                    for (CompletableFuture<WatchlistItem> future : futures) {
                        try {
                            items.add(future.get());
                        } catch (InterruptedException | ExecutionException e) {
                            Log.e("MainActivity", "Error fetching watchlist item", e);
                        }
                    }
                    return items;
                })
                .thenAccept(items -> {
                    runOnUiThread(() -> {

                        loadingIndicator.setVisibility(View.GONE);
                        watchlistAdapter.updateItems(items);
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        loadingIndicator.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Error updating watchlist: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    });
                    return null;
                });
    }

    private void loadWatchlistData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            transactToLoginActivity();
        }

        String userId = currentUser.getUid();
        firebaseManager.getUserPortfolio(userId)
                .thenAccept(userPortfolio -> {
                    List<String> watchlist = userPortfolio.getWatchlist();
                    if (watchlist == null || watchlist.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Watchlist is empty", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    for (String symbol : watchlist) {
                        alphaVantageAPI.getQuote(symbol)
                                .thenAccept(quote -> {
                                    WatchlistItem item = new WatchlistItem(
                                            symbol,
                                            quote.getPrice(),
                                            quote.getChangePercent()
                                    );
                                    runOnUiThread(() -> watchlistAdapter.addItem(item));
                                })
                                .exceptionally(error -> {
                                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                                            "Error fetching data for " + symbol, Toast.LENGTH_SHORT).show());
                                    return null;
                                });
                    }
                })
                .exceptionally(error -> {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                            "Error loading watchlist: " + error.getMessage(), Toast.LENGTH_LONG).show());
                    return null;
                });
    }
}