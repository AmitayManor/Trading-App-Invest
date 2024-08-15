package com.example.invest.appPages;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.invest.R;
import com.example.invest.adapters.PortfolioAdapter;
import com.example.invest.models.PortfolioHolding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class PortfolioActivity extends AppCompatActivity {

    private TextView portfolioValueTextView;
    private TextView portfolioChangeTextView;
    private LineChart performanceChart;
    private RecyclerView holdingsRecyclerView;
    private Button buyButton;
    private Button sellButton;
    private PortfolioAdapter portfolioAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        initViews();
        setupPortfolioSummary();
        setupPerformanceChart();
        setupHoldingsRecyclerView();
        setupButtons();
        setupBottomNavigation();
    }

    private void initViews() {
        portfolioValueTextView = findViewById(R.id.portfolio_value);
        portfolioChangeTextView = findViewById(R.id.portfolio_change);
        performanceChart = findViewById(R.id.performance_chart);
        holdingsRecyclerView = findViewById(R.id.holdings_recycler_view);
        buyButton = findViewById(R.id.buy_button);
        sellButton = findViewById(R.id.sell_button);
    }

    private void setupPortfolioSummary() {
        // TODO: Fetch actual portfolio data
        double portfolioValue = 10234.56;
        double portfolioChange = 5.2;

        portfolioValueTextView.setText(String.format("$%.2f", portfolioValue));
        portfolioChangeTextView.setText(String.format("%.2f%%", portfolioChange));
        portfolioChangeTextView.setTextColor(portfolioChange >= 0 ? getColor(R.color.positive) : getColor(R.color.negative));
    }

    private void setupPerformanceChart() {
        // TODO: Implement chart with actual data
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0f, 100000f));
        entries.add(new Entry(1f, 102000f));
        entries.add(new Entry(2f, 103000f));
        entries.add(new Entry(3f, 101000f));
        entries.add(new Entry(4f, 104000f));
        entries.add(new Entry(5f, 102345f));

        LineDataSet dataSet = new LineDataSet(entries, "Portfolio Performance");
        dataSet.setColor(getColor(R.color.chart_line));
        dataSet.setValueTextColor(getColor(R.color.chart_values));

        LineData lineData = new LineData(dataSet);
        performanceChart.setData(lineData);
        performanceChart.invalidate(); // refresh
    }

    private void setupHoldingsRecyclerView() {
        portfolioAdapter = new PortfolioAdapter(getPortfolioHoldings());
        holdingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        holdingsRecyclerView.setAdapter(portfolioAdapter);
    }

    private List<PortfolioHolding> getPortfolioHoldings() {
        // TODO: Fetch actual portfolio holdings
        List<PortfolioHolding> holdings = new ArrayList<>();
        holdings.add(new PortfolioHolding("AAPL", "Apple Inc.", 10, 150.25, 1502.50, 2.5, 14.7));
        holdings.add(new PortfolioHolding("GOOGL", "Alphabet Inc.", 5, 2800.75, 14003.75, -0.8, 36.8));
        holdings.add(new PortfolioHolding("TSLA", "Tesla, Inc.", 8, 900.40, 7203.20, 3.5, 48.5));
        return holdings;
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
                } else if (itemId == R.id.nav_news) {
                    Intent newsIntent = new Intent(PortfolioActivity.this, NewsActivity.class);
                    startActivity(newsIntent);
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
        // Refresh portfolio value
        setupPortfolioSummary();

        // Refresh performance chart
        setupPerformanceChart();

        // Refresh holdings list
        setupHoldingsRecyclerView();

        // Optional: Show a refresh animation or message
        showRefreshIndicator();
    }

    private void showRefreshIndicator() {
        // Show a short toast message
        Toast.makeText(this, "Refreshing portfolio...", Toast.LENGTH_SHORT).show();

        // Optional: You could also implement a pull-to-refresh functionality
        // or show a progress bar during refresh
    }
}
