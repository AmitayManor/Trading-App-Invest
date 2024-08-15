package com.example.invest.items;

public class NewsItem {
    private String title;
    private String summary;
    private String source;
    private String timeAgo;

    public NewsItem(String title, String summary, String source, String timeAgo) {
        this.title = title;
        this.summary = summary;
        this.source = source;
        this.timeAgo = timeAgo;
    }

    // Getters
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getSource() { return source; }
    public String getTimeAgo() { return timeAgo; }
}