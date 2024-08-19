package com.example.invest.items;

import java.time.LocalDateTime;

public class StockItem {
    private String symbol;
    private double price;
    private double change;
    private double changePercent;
    private double volume;
    private LocalDateTime lastUpdated;

    public StockItem(String symbol, double price, double change, double changePercent, double volume) {
        this.symbol = symbol;
        this.price = price;
        this.change = change;
        this.changePercent = changePercent;
        this.volume = volume;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters
    public String getSymbol() { return symbol; }
    public double getPrice() { return price; }
    public double getChange() { return change; }
    public double getChangePercent() { return changePercent; }
    public double getVolume() { return volume; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }

    // Setters
    public void setPrice(double price) {
        this.price = price;
        this.lastUpdated = LocalDateTime.now();
    }
    public void setChange(double change) {
        this.change = change;
        this.lastUpdated = LocalDateTime.now();
    }
    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
        this.lastUpdated = LocalDateTime.now();
    }
    public void setVolume(double volume) {
        this.volume = volume;
        this.lastUpdated = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "StockItem{" +
                "symbol='" + symbol + '\'' +
                ", price=" + price +
                ", change=" + change +
                ", changePercent=" + changePercent +
                ", volume=" + volume +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}