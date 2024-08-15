package com.example.invest.models;

public class PortfolioHolding {
    private String symbol;
    private String name;
    private int shares;
    private double price;
    private double totalValue;
    private double change;
    private double portfolioPercentage;

    public PortfolioHolding(String symbol, String name, int shares, double price, double totalValue, double change, double portfolioPercentage) {
        this.symbol = symbol;
        this.name = name;
        this.shares = shares;
        this.price = price;
        this.totalValue = totalValue;
        this.change = change;
        this.portfolioPercentage = portfolioPercentage;
    }

    // Getters
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public int getShares() { return shares; }
    public double getPrice() { return price; }
    public double getTotalValue() { return totalValue; }
    public double getChange() { return change; }
    public double getPortfolioPercentage() { return portfolioPercentage; }
}
