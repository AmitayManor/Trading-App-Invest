package com.example.invest.models;

public class PortfolioHolding {
    private String symbol;
    private int shares;
    private double price;
    private double totalValue;
    private double change;
    private double portfolioPercentage;

    public PortfolioHolding(String symbol, int shares, double price, double totalValue, double change, double portfolioPercentage) {
        this.symbol = symbol;
        this.shares = shares;
        this.price = price;
        this.totalValue = totalValue;
        this.change = change;
        this.portfolioPercentage = portfolioPercentage;
    }

    // Getters
    public String getSymbol() { return symbol; }
    public int getShares() { return shares; }
    public double getPrice() { return price; }
    public double getTotalValue() { return totalValue; }
    public double getChange() { return change; }
    public double getPortfolioPercentage() { return portfolioPercentage; }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public void setPortfolioPercentage(double portfolioPercentage) {
        this.portfolioPercentage = portfolioPercentage;
    }
}
