package com.example.invest.appPages;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.invest.R;
import com.example.invest.handlers.AlphaVantageAPI;
import com.example.invest.handlers.FirebaseManager;
import com.example.invest.models.UserPortfolio;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.atomic.AtomicReference;

public class BuyActivity extends AppCompatActivity {

    private TextView stockSymbolView, stockPriceView, stockChangeView, totalValueView;
    private EditText sharesEdit;
    private Button minusButton, plusButton, approveButton, cancelButton;
    private String stockSymbol;
    private AtomicReference<Double> stockPrice = new AtomicReference<>(0.0);
    private FirebaseManager firebaseManager;
    private FirebaseAuth mAuth;
    private AlphaVantageAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        firebaseManager = FirebaseManager.getInstance();
        mAuth = FirebaseAuth.getInstance();
        api = AlphaVantageAPI.getInstance();

        initViews();
        setupQuantityControls();
        setupButtons();
        loadStockData();
    }

    private void initViews() {
        stockSymbolView = findViewById(R.id.stock_symbol);
        stockPriceView = findViewById(R.id.stock_price);
        stockChangeView = findViewById(R.id.stock_change);
        totalValueView = findViewById(R.id.total_value);
        sharesEdit = findViewById(R.id.shares_edit);
        minusButton = findViewById(R.id.minus_button);
        plusButton = findViewById(R.id.plus_button);
        approveButton = findViewById(R.id.approve_button);
        cancelButton = findViewById(R.id.cancel_button);
    }

    @SuppressLint("DefaultLocale")
    private void loadStockData(){
        stockSymbol = getIntent().getStringExtra("STOCK_SYMBOL");
        if (stockSymbol == null) {
            Toast.makeText(this, "Error: No stock symbol provided", Toast.LENGTH_LONG).show();
            transactToTradeActivity();
        }
        api.getQuote(stockSymbol).thenAccept(quoteResponse -> {
            stockPrice.set(quoteResponse.getPrice());
            runOnUiThread(() -> {
                stockSymbolView.setText(stockSymbol);
                stockPriceView.setText(String.format("$%.2f", quoteResponse.getPrice()));
                stockChangeView.setText(String.format("%.2f%%", quoteResponse.getChangePercent()));
                stockChangeView.setTextColor(quoteResponse.getChangePercent() >= 0 ? getColor(R.color.positive) : getColor(R.color.negative));
                updateTotalValue();
            });
        }).exceptionally(error -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Error fetching stock data: " + error.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            });
            return null;
        });
    }

    private void setupQuantityControls() {
        sharesEdit.setText("1");

        minusButton.setOnClickListener(v -> {
            int currentShares = Integer.parseInt(sharesEdit.getText().toString());
            if (currentShares > 1) {
                sharesEdit.setText(String.valueOf(currentShares - 1));
                updateTotalValue();
            }
        });

        plusButton.setOnClickListener(v -> {
            int currentShares = Integer.parseInt(sharesEdit.getText().toString());
            sharesEdit.setText(String.valueOf(currentShares + 1));
            updateTotalValue();
        });

        sharesEdit.setOnEditorActionListener((v, actionId, event) -> {
            updateTotalValue();
            return false;
        });
    }

    private void setupButtons() {
        approveButton.setOnClickListener(v -> showConfirmationDialog());
        cancelButton.setOnClickListener(v -> transactToTradeActivity());
    }

    @SuppressLint("DefaultLocale")
    private void updateTotalValue() {
        int shares = Integer.parseInt(sharesEdit.getText().toString());
        double total = shares * stockPrice.get();
        totalValueView.setText(String.format("Total: $%.2f", total));
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Purchase")
                .setMessage("Are you sure you want to make this purchase?")
                .setPositiveButton("Yes, I'm sure", (dialog, which) -> makePurchase())
                .setNegativeButton("No, I'm not sure", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void makePurchase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_LONG).show();
            transactToLoginActivity();
            return;
        }

        String userId = currentUser.getUid();
        int shares = Integer.parseInt(sharesEdit.getText().toString());
        double totalCost = shares * stockPrice.get();

        firebaseManager.getUserPortfolio(userId).thenAccept(userPortfolio -> {
            if (userPortfolio.getCashBalance() >= totalCost) {
                userPortfolio.setCashBalance(userPortfolio.getCashBalance() - totalCost);
                userPortfolio.updateInitialInvestment(totalCost);
                UserPortfolio.Holdings holdings = userPortfolio.getHoldings().getOrDefault(stockSymbol, new UserPortfolio.Holdings());
                int newQuantity = holdings.getQuantity() + shares;
                double newAveragePrice = (holdings.getQuantity() * holdings.getAveragePrice() + totalCost) / newQuantity;
                holdings.setQuantity(newQuantity);
                holdings.setAveragePrice(newAveragePrice);
                userPortfolio.getHoldings().put(stockSymbol, holdings);

                firebaseManager.updateUserPortfolio(userPortfolio).thenRun(() -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Purchase successful", Toast.LENGTH_LONG).show();
                        finish();
                    });
                }).exceptionally(error -> {
                    runOnUiThread(() -> Toast.makeText(this, "Error updating portfolio: " + error.getMessage(), Toast.LENGTH_LONG).show());
                    return null;
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "Insufficient funds", Toast.LENGTH_LONG).show());
            }
        }).exceptionally(error -> {
            runOnUiThread(() -> Toast.makeText(this, "Error fetching user portfolio: " + error.getMessage(), Toast.LENGTH_LONG).show());
            return null;
        });
    }


   private void transactToTradeActivity(){
       Intent intent = new Intent(getApplicationContext(), TradeActivity.class);
       startActivity(intent);
       finish();
   }

    private void transactToLoginActivity(){
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
