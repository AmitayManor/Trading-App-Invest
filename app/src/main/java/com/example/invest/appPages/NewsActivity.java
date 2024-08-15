package com.example.invest.appPages;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.invest.R;
import com.example.invest.adapters.NewsAdapter;
import com.example.invest.items.NewsItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity {

    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;
    private SearchView searchView;
    private ChipGroup categoryChips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        initViews();
        setupRecyclerView();
        setupSearchView();
        setupCategoryChips();
        setupBottomNavigation();
    }

    private void initViews() {
        newsRecyclerView = findViewById(R.id.news_recycler_view);
        searchView = findViewById(R.id.search_view);
        categoryChips = findViewById(R.id.category_chips);
    }

    private void setupRecyclerView() {
        newsAdapter = new NewsAdapter(getDummyNewsItems());
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsRecyclerView.setAdapter(newsAdapter);
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
                // Filter news items as user types
                return false;
            }
        });
    }

    private void setupCategoryChips() {
        categoryChips.setOnCheckedChangeListener((group, checkedId) -> {
            // Filter news items based on selected category
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_news);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    // Navigate to Home Activity
                    Intent homeIntent = new Intent(NewsActivity.this, MainActivity.class);
                    startActivity(homeIntent);
                    return true;
                } else if (itemId == R.id.nav_news) {
                    // Already on News Activity, refresh the page
                    refreshNewsPage();
                    return true;
                } else if (itemId == R.id.nav_trade) {
                    // Navigate to Trade Activity
                    Intent tradeIntent = new Intent(NewsActivity.this, TradeActivity.class);
                    startActivity(tradeIntent);
                    return true;
                } else if (itemId == R.id.nav_portfolio) {
                    // Navigate to Portfolio Activity
                    Intent portfolioIntent = new Intent(NewsActivity.this, PortfolioActivity.class);
                    startActivity(portfolioIntent);
                    return true;
                }
                return false;
            }
        });
    }

    private void refreshNewsPage() {
        // Refresh news articles
        updateNewsArticles();

        // Reset search view
        resetSearchView();

        // Reset category chips
        resetCategoryChips();

        // Optional: Show a refresh animation or message
        showRefreshIndicator();
    }

    private void updateNewsArticles() {
        // TODO: Fetch the latest news articles from your data source
        // For now, we'll just update with new dummy data
        List<NewsItem> newArticles = getDummyNewsItems();
        newsAdapter.updateNewsItems(newArticles);
        newsAdapter.notifyDataSetChanged();
    }

    private void resetSearchView() {
        searchView.setQuery("", false);
        searchView.clearFocus();
    }

    private void resetCategoryChips() {
        categoryChips.clearCheck();
        categoryChips.check(R.id.chip_all); // Assuming you have an "All" chip
    }

    private void showRefreshIndicator() {
        // Show a short toast message
        Toast.makeText(this, "Refreshing news...", Toast.LENGTH_SHORT).show();

        // Optional: You could also implement a pull-to-refresh functionality
        // or show a progress bar during refresh
    }

    private List<NewsItem> getDummyNewsItems() {
        List<NewsItem> newsItems = new ArrayList<>();
        newsItems.add(new NewsItem("Stock Market Soars", "The stock market reached new heights today...", "Financial Times", "2h ago"));
        newsItems.add(new NewsItem("Crypto Crash", "Bitcoin and other cryptocurrencies experienced a sharp decline...", "CryptoNews", "4h ago"));
        newsItems.add(new NewsItem("Economic Recovery", "Experts predict a strong economic recovery in the coming months...", "Economics Today", "1d ago"));
        return newsItems;
    }
}