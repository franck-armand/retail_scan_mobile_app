package com.example.mafscan;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class FailedOrSavedScanActivity extends AppCompatActivity {

    private RecyclerView failedScansRecyclerView;
    private TextView emptyFailedScansTextView;
    private ScanSessionAdapter scanSessionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_failed_or_saved_scans);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.failedScansToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Get references to views
        failedScansRecyclerView = findViewById(R.id.failedScansRecyclerView);
        emptyFailedScansTextView = findViewById(R.id.emptyFailedScansTextView);

        // Set up the RecyclerView
        failedScansRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scanSessionAdapter = new ScanSessionAdapter(getApplication());
        failedScansRecyclerView.setAdapter(scanSessionAdapter);

        // Set up the ViewModel
        FailedOrSavedScanViewModel viewModel = new
                ViewModelProvider(this).get(FailedOrSavedScanViewModel.class);

        // Observe the LiveData for ScanSessions
        viewModel.getAllScanSessions().observe(this, scanSessions -> {
            scanSessionAdapter.submitList(scanSessions);
            updateEmptyState();
        });
    }

    public void updateEmptyState() {
        if (scanSessionAdapter.getCurrentList().isEmpty()) {
            emptyFailedScansTextView.setVisibility(View.VISIBLE);
            failedScansRecyclerView.setVisibility(View.GONE);
        } else {
            emptyFailedScansTextView.setVisibility(View.GONE);
            failedScansRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}