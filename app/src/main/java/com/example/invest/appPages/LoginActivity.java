package com.example.invest.appPages;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.invest.R;
import com.example.invest.models.UserPortfolio;
import com.example.invest.models.UserProfile;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();


        if(user == null)
            signIn();
        else{
            transactToMainActivity();
        }
    }

    private void transactToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }


    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );


    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                createOrUpdateUserData(user);
                transactToMainActivity();
            } else {
                Toast.makeText(this, "Error: User is null after successful sign-in", Toast.LENGTH_LONG).show();
            }
        } else {
            if (response == null) {
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
            } else if (response.getError() != null) {
                Toast.makeText(this, "Sign in error: " + response.getError().getErrorCode(), Toast.LENGTH_LONG).show();
            }
        }
    }


    private void createOrUpdateUserData(FirebaseUser user) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("displayName", user.getDisplayName());

        UserPortfolio newUserPortfolio = new UserPortfolio(user.getUid(), 10000);
        newUserPortfolio.setHoldings(new HashMap<>());
        newUserPortfolio.setWatchlist(new ArrayList<>());

        usersRef.child(user.getUid()).setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    usersRef.child(user.getUid()).child("portfolio").setValue(newUserPortfolio)
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d("LoginActivity", "User and portfolio created/updated successfully");
                                transactToMainActivity();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("LoginActivity", "Error creating/updating user portfolio", e);
                                Toast.makeText(LoginActivity.this, "Failed to set up user portfolio. Please try again.", Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("LoginActivity", "Error creating/updating user data", e);
                    Toast.makeText(LoginActivity.this, "Failed to set up user data. Please try again.", Toast.LENGTH_LONG).show();
                });
    }

    private void signIn(){
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.ic_logo)
                .build();
        signInLauncher.launch(signInIntent);

    }


}