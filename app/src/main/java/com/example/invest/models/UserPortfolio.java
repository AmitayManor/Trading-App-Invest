package com.example.invest.models;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserPortfolio {
    private String userId;
    private Map<String, Holdings> holdings;
    private double cashBalance;
    private List<String> watchlist;
    private double initialInvestment;


    public UserPortfolio() {
    }

    public UserPortfolio(String userId, double initialBalance) {
        this.userId = userId;
        this.holdings = new HashMap<>();
        this.cashBalance = initialBalance;
        this.watchlist = new ArrayList<>();
        this.initialInvestment = initialBalance;
    }

    public double getTotalPortfolioValue() {
        double total = cashBalance;
        for (Holdings holding : holdings.values()) {
            total += holding.getQuantity() * holding.getAveragePrice();
        }
        return total;
    }

    public void addToWatchlist(String symbol) {
        if (watchlist == null) {
            watchlist = new ArrayList<>();
        }
        if (!watchlist.contains(symbol)) {
            watchlist.add(symbol);
        }
    }

    public void removeFromWatchlist(String symbol) {
        if (watchlist != null) {
            watchlist.remove(symbol);
        }
    }
    public double getInitialInvestment() {
        return initialInvestment;
    }

    public void setInitialInvestment(double initialInvestment) {
        this.initialInvestment = initialInvestment;
    }

    public void updateInitialInvestment(double amount) {
        this.initialInvestment += amount;
    }

    public List<String> getWatchlist() {
        return watchlist;
    }

    public void setWatchlist(List<String> watchlist) {
        this.watchlist = watchlist;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, Holdings> getHoldings() {
        return holdings;
    }

    public void setHoldings(Map<String, Holdings> holdings) {
        this.holdings = holdings;
    }

    public double getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(double cashBalance) {
        this.cashBalance = cashBalance;
    }


    public static class Holdings {
        private int quantity;
        private double averagePrice;

        public Holdings() {
        }

        public Holdings(int quantity, double averagePrice) {
            this.quantity = quantity;
            this.averagePrice = averagePrice;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getAveragePrice() {
            return averagePrice;
        }

        public void setAveragePrice(double averagePrice) {
            this.averagePrice = averagePrice;
        }
    }
}