package com.example.mafscan;


import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import android.os.Vibrator;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // SplashScreen
        SplashScreen.installSplashScreen(this);

        // initialize Vibrator
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        final VibrationEffect clickVibration = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home3);

        // Set up the custom Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up Scan button navigation
        LinearLayout scanButton = findViewById(R.id.btn_scan);
        scanButton.setOnClickListener(v -> {
            vibrator.vibrate(clickVibration);
            Intent intent = new Intent(MainActivity.this, ScanActivityFromTo.class);
            startActivity(intent);
        });

        // set up Retrieve QR code info button navigation
        LinearLayout retrieveButton = findViewById(R.id.retrieve_scan_info);
        retrieveButton.setOnClickListener(v -> {
            vibrator.vibrate(clickVibration);
            Intent intent = new Intent(MainActivity.this, RetrieveScanInfo.class);
            startActivity(intent);
        });
    }

}
