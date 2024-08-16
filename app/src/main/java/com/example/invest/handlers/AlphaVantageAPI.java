package com.example.invest.handlers;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.timeseries.response.QuoteResponse;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.crazzyghost.alphavantage.Config;

import java.util.concurrent.CompletableFuture;

public class AlphaVantageAPI {
    private static final String API_KEY = "MESG2D7QDONF28QE";
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

    public void testConnection(ConnectionTestCallback callback) {
        getQuote("AAPL")
                .thenAccept(response -> {
                    if (response != null && response.getSymbol() != null) {
                        callback.onSuccess("API Connection Successful. AAPL price: $" + response.getPrice());
                    } else {
                        callback.onFailure("API connected but received invalid data");
                    }
                })
                .exceptionally(error -> {
                    callback.onFailure("API Connection Failed: " + error.getMessage());
                    return null;
                });
    }

    public interface ConnectionTestCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

}