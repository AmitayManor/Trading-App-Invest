package com.example.invest.items;

public class StockItem {
    private String symbol;
    private String name;
    private double price;
    private double change;
    private int shares;


    public StockItem(String symbol, String name, double price, double change, int shares) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.change = change;
        this.shares = shares;
    }

    // Getters
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public double getChange() { return change; }
    public int getShares() { return shares; }
}