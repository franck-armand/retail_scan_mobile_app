package com.example.mafscan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class ScanActivityFromTo extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_0);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_scan0);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Scan Activity: From/To");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Handle button click
        findViewById(R.id.validateButton).setOnClickListener(v -> {
            Toast.makeText(this,"validation button clicked", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Handle back navigation
        return true;
    }
}

