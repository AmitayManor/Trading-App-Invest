package com.example.invest.handlers;

import androidx.annotation.NonNull;

import com.google.firebase.database.*;
import com.example.invest.models.UserPortfolio;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FirebaseManager {
    private static FirebaseManager instance;
    private final DatabaseReference databaseReference;

    private FirebaseManager() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public CompletableFuture<UserPortfolio> getUserPortfolio(String userId) {
        CompletableFuture<UserPortfolio> future = new CompletableFuture<>();

        databaseReference.child("users").child(userId).child("portfolio").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserPortfolio portfolio = dataSnapshot.getValue(UserPortfolio.class);
                future.complete(portfolio);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    public CompletableFuture<Void> updateUserWatchlist(String userId, List<String> watchlist) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        databaseReference.child("users").child(userId).child("watchlist").setValue(watchlist)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Void> updateUserPortfolio(UserPortfolio portfolio) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        databaseReference.child("users").child(portfolio.getUserId()).child("portfolio").setValue(portfolio)
                .addOnSuccessListener(aVoid -> future.complete(null))
                .addOnFailureListener(future::completeExceptionally);

        return future;
    }
}