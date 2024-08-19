package com.example.invest.handlers;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.QuoteResponse;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.crazzyghost.alphavantage.Config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AlphaVantageAPI {
    //private static final String API_KEY = "MESG2D7QDONF28QE"; Old API
    private static final String API_KEY = "8697U8QNOUEFZB2C"; // NewAPI
    private static AlphaVantageAPI instance;

    private AlphaVantageAPI() {
        Config cfg = Config.builder()
                .key(API_KEY)
                .timeOut(10)
                .build();

        AlphaVantage.api().init(cfg);
    }

    public static synchronized AlphaVantageAPI getInstance() {
        if (instance == null) {
            instance = new AlphaVantageAPI();
        }
        return instance;
    }

    public CompletableFuture<QuoteResponse> getQuote(String symbol) {
        CompletableFuture<QuoteResponse> future = new CompletableFuture<>();

        AlphaVantage.api()
                .timeSeries()
                .quote()
                .forSymbol(symbol)
                .onSuccess(response -> future.complete((QuoteResponse) response))
                .onFailure(error -> future.completeExceptionally(new Exception(error.getMessage())))
                .fetch();

        return future;
    }

    public CompletableFuture<List<WeeklyAdjustedData>> getWeeklyAdjusted(String symbol) {
        CompletableFuture<List<WeeklyAdjustedData>> future = new CompletableFuture<>();

        AlphaVantage.api()
                .timeSeries()
                .weekly()
                .forSymbol(symbol)
                .onSuccess(response -> {
                    TimeSeriesResponse timeSeriesResponse = (TimeSeriesResponse) response;
                    List<WeeklyAdjustedData> weeklyData = new ArrayList<>();
                    for (StockUnit stock : timeSeriesResponse.getStockUnits()) {
                        if(stock != null) {
                            String date = stock.getDate();
                            double adjustedClose = stock.getAdjustedClose();
                            weeklyData.add(new WeeklyAdjustedData(date, BigDecimal.valueOf(adjustedClose)));
                        }
                    }
                    future.complete(weeklyData);
                })
                .onFailure(error -> future.completeExceptionally(new Exception(error.getMessage())))
                .fetch();

        return future;
    }

    public CompletableFuture<TimeSeriesResponse> getDailyTimeSeries(String symbol) {
        CompletableFuture<TimeSeriesResponse> future = new CompletableFuture<>();

        AlphaVantage.api()
                .timeSeries()
                .daily()
                .forSymbol(symbol)
                .outputSize(OutputSize.COMPACT)
                .onSuccess(response -> future.complete((TimeSeriesResponse) response))
                .onFailure(error -> future.completeExceptionally(new Exception(error.getMessage())))
                .fetch();

        return future;
    }

    public interface ConnectionTestCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public static class WeeklyAdjustedData {
        private String date;
        private BigDecimal adjustedClose;

        public WeeklyAdjustedData(String date, BigDecimal adjustedClose) {
            this.date = date;
            this.adjustedClose = adjustedClose;
        }

        public String getDate() {
            return date;
        }

        public BigDecimal getAdjustedClose() {
            return adjustedClose;
        }
    }

}