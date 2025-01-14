package com.example.mafscan;


import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home3);

        // Set up the custom Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up Scan button navigation
        LinearLayout scanButton = findViewById(R.id.btn_scan);
        scanButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScanActivityFromTo.class);
            startActivity(intent);
        });
    }

}
