package com.example.invest;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;

public class MainActivity extends AppCompatActivity {

    Config cfg;
    String apiKey = "MESG2D7QDONF28QE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set views

        //set db

        //set alpha vantage api
        setUpAlphaVantage();






    }

    private void setUpAlphaVantage() {

        cfg = Config.builder()
                .key(apiKey)
                .timeOut(10)
                .build();

        AlphaVantage.api().init(cfg);





    }


}