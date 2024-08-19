package com.example.invest.appPages;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.view.MenuItem;
import androidx.annotation.NonNull;

import com.example.invest.handlers.AlphaVantageAPI;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.invest.R;
import com.example.invest.adapters.PortfolioAdapter;
import com.example.invest.models.PortfolioHolding;
import com.example.invest.handlers.FirebaseManager;
import com.example.invest.models.UserPortfolio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class PortfolioActivity extends AppCompatActivity {

    private TextView portfolioValueTextView;
    private TextView portfolioChangeTextView;
    private LineChart performanceChart;
    private RecyclerView holdingsRecyclerView;
    private Button buyButton;
    private Button sellButton;
    private PortfolioAdapter portfolioAdapter;
    private FirebaseManager firebaseManager;
    private FirebaseAuth mAuth;
    private AlphaVantageAPI api;
    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);
        mAuth = FirebaseAuth.getInstance();
        firebaseManager = FirebaseManager.getInstance();
        api = AlphaVantageAPI.getInstance();

        initViews();
        loadUserPortfolio();
        setupButtons();
        setupBottomNavigation();
    }

    private void loadUserPortfolio() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            firebaseManager.getUserPortfolio(userId)
                    .thenAccept(this::updateUI)
                    .exceptionally(error -> {
                        Toast.makeText(this, "Error loading portfolio: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return null;
                    });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
            transactToLoginActivity();
        }
    }

    private CompletableFuture<AbstractMap.SimpleEntry<List<PortfolioHolding>, double[]>> fetchUpdatedHoldings(UserPortfolio userPortfolio) {
        AtomicReference<Double> totalValue = new AtomicReference<>(userPortfolio.getCashBalance());
        AtomicReference<Double> previousTotalValue = new AtomicReference<>(userPortfolio.getCashBalance());
        List<CompletableFuture<PortfolioHolding>> futures = new ArrayList<>();

        for (Map.Entry<String, UserPortfolio.Holdings> entry : userPortfolio.getHoldings().entrySet()) {
            String symbol = entry.getKey();
            UserPortfolio.Holdings holding = entry.getValue();

            CompletableFuture<PortfolioHolding> future = api.getQuote(symbol).thenApply(quote -> {
                double currentPrice = quote.getPrice();
                double previousPrice = currentPrice / (1 + quote.getChangePercent() / 100);

                double holdingValue = currentPrice * holding.getQuantity();
                double previousHoldingValue = previousPrice * holding.getQuantity();

                totalValue.updateAndGet(v -> v + holdingValue);
                previousTotalValue.updateAndGet(v -> v + previousHoldingValue);

                return new PortfolioHolding(symbol, holding.getQuantity(), currentPrice, holdingValue, quote.getChangePercent(), 0);
            });

            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<PortfolioHolding> holdings = futures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList());

                    double portfolioValue = totalValue.get();
                    double portfolioChange = ((portfolioValue - previousTotalValue.get()) / previousTotalValue.get()) * 100;

                    for (PortfolioHolding h : holdings) {
                        h.setPortfolioPercentage((h.getTotalValue() / portfolioValue) * 100);
                    }

                    return new AbstractMap.SimpleEntry<>(holdings, new double[]{portfolioValue, portfolioChange});
                });
    }

    private void transactToLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("DefaultLocale")
    private void updateUI(UserPortfolio userPortfolio) {
        if (userPortfolio != null) {
            setupPortfolioSummary(userPortfolio);
            setupPerformanceChart(userPortfolio);
            setupRecyclerView(userPortfolio);
        } else {
            Toast.makeText(this, "Failed to load portfolio data", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        portfolioValueTextView = findViewById(R.id.portfolio_value);
        portfolioChangeTextView = findViewById(R.id.portfolio_change);
        performanceChart = findViewById(R.id.performance_chart);
        holdingsRecyclerView = findViewById(R.id.holdings_recycler_view);
        buyButton = findViewById(R.id.buy_button);
        sellButton = findViewById(R.id.sell_button);
    }

    @SuppressLint("DefaultLocale")
    private void setupPortfolioSummary(UserPortfolio userPortfolio) {
        fetchUpdatedHoldings(userPortfolio)
                .thenAccept(result -> {
                    List<PortfolioHolding> holdings = result.getKey();
                    double portfolioValue = result.getValue()[0];
                    double portfolioChange = result.getValue()[1];

                    runOnUiThread(() -> {
                        portfolioValueTextView.setText(String.format("$%.2f", portfolioValue));
                        portfolioChangeTextView.setText(String.format("%.2f%%", portfolioChange));
                        portfolioChangeTextView.setTextColor(portfolioChange >= 0 ? getColor(R.color.positive) : getColor(R.color.negative));
                        portfolioAdapter.updateHoldings(holdings);
                    });
                })
                .exceptionally(error -> {
                    runOnUiThread(() -> Toast.makeText(this, "Error updating portfolio: " + error.getMessage(), Toast.LENGTH_SHORT).show());
                    return null;
                });
    }


    private void setupPerformanceChart(UserPortfolio userPortfolio) {
        api.getWeeklyAdjusted(userPortfolio.getHoldings().keySet().iterator().next())
                .thenAccept(weeklyAdjusted -> {
                    List<Entry> entries = new ArrayList<>();
                    List<String> dates = new ArrayList<>();

                    for (int i = 0; i < Math.min(12, weeklyAdjusted.size()); i++) {
                        entries.add(new Entry(i, weeklyAdjusted.get(i).getAdjustedClose().floatValue()));
                        dates.add(weeklyAdjusted.get(i).getDate());
                    }

                    runOnUiThread(() -> {
                        LineDataSet dataSet = new LineDataSet(entries, "Portfolio Performance");
                        dataSet.setColor(getColor(R.color.chart_line));
                        dataSet.setValueTextColor(getColor(R.color.chart_values));

                        LineData lineData = new LineData(dataSet);
                        performanceChart.setData(lineData);

                        XAxis xAxis = performanceChart.getXAxis();
                        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setGranularity(1f);
                        xAxis.setLabelRotationAngle(45);

                        performanceChart.getDescription().setEnabled(false);
                        performanceChart.invalidate();
                    });
                })
                .exceptionally(error -> {
                    runOnUiThread(() -> Toast.makeText(this, "Error fetching performance data", Toast.LENGTH_SHORT).show());
                    return null;
                });
    }
//    TODO: Choose what is better
    private void setupRecyclerView(UserPortfolio userPortfolio) {
//        portfolioAdapter = new PortfolioAdapter(new ArrayList<>());
//        holdingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        holdingsRecyclerView.setAdapter(portfolioAdapter);

        loadingIndicator.setVisibility(View.VISIBLE);
        convertToPortfolioHoldings(userPortfolio)
                .thenAccept(holdings -> {
                    runOnUiThread(() -> {
                        loadingIndicator.setVisibility(View.GONE);
                        portfolioAdapter.updateHoldings(holdings);
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        loadingIndicator.setVisibility(View.GONE);
                        Toast.makeText(this, "Error loading portfolio: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                    });
                    return null;
                });
    }

    private CompletableFuture<List<PortfolioHolding>> convertToPortfolioHoldings(UserPortfolio userPortfolio) {
        List<CompletableFuture<PortfolioHolding>> futures = new ArrayList<>();
        AtomicReference<Double> totalPortfolioValue = new AtomicReference<>(userPortfolio.getCashBalance());

        for (Map.Entry<String, UserPortfolio.Holdings> entry : userPortfolio.getHoldings().entrySet()) {
            String symbol = entry.getKey();
            UserPortfolio.Holdings holding = entry.getValue();

            CompletableFuture<PortfolioHolding> future = api.getQuote(symbol)
                    .thenApply(quoteResponse -> {
                        double currentPrice = quoteResponse.getPrice();
                        int quantity = holding.getQuantity();
                        double totalValue = quantity * currentPrice;
                        totalPortfolioValue.updateAndGet(v -> v + totalValue);
                        double change = quoteResponse.getChangePercent();

                        return new PortfolioHolding(
                                symbol,
                                quantity,
                                currentPrice,
                                totalValue,
                                change,
                                0 // We'll calculate this after all API calls are complete
                        );
                    });

            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<PortfolioHolding> holdings = new ArrayList<>();
                    double finalTotalPortfolioValue = totalPortfolioValue.get();

                    for (CompletableFuture<PortfolioHolding> future : futures) {
                        try {
                            PortfolioHolding holding = future.get();
                            holding.setPortfolioPercentage((holding.getTotalValue() / finalTotalPortfolioValue) * 100);
                            holdings.add(holding);
                        } catch (InterruptedException | ExecutionException e) {
                            Log.e("PortfolioActivity", "Error getting holding: " + e.getMessage());
                        }
                    }

                    return holdings;
                });
    }

    private void setupButtons() {
        buyButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, BuyActivity.class);
            startActivity(intent);
        });

        sellButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SellActivity.class);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_portfolio);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent homeIntent = new Intent(PortfolioActivity.this, MainActivity.class);
                    startActivity(homeIntent);
                    return true;
                } else if (itemId == R.id.nav_trade) {
                    Intent tradeIntent = new Intent(PortfolioActivity.this, TradeActivity.class);
                    startActivity(tradeIntent);
                    return true;
                } else if (itemId == R.id.nav_portfolio) {
                    // Already on Portfolio Activity, refresh the page
                    refreshPortfolioPage();
                    return true;
                }
                return false;
            }
        });
    }

    private void refreshPortfolioPage() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            transactToLoginActivity();
        }

        String userId = currentUser.getUid();
        firebaseManager.getUserPortfolio(userId)
                .thenAccept(userPortfolio -> {
                    runOnUiThread(() -> {
                        setupPortfolioSummary(userPortfolio);
                        setupPerformanceChart(userPortfolio);
                        setupRecyclerView(userPortfolio);
                        Toast.makeText(this, "Portfolio refreshed", Toast.LENGTH_SHORT).show();
                    });
                })
                .exceptionally(error -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error refreshing portfolio: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    });
                    return null;
                });
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadUserPortfolio();
    }

}
