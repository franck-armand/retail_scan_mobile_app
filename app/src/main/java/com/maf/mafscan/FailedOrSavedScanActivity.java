package com.maf.mafscan;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class FailedOrSavedScanActivity extends AppCompatActivity
        implements ScanSessionAdapter.OnListChangedListener {

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
        scanSessionAdapter = new ScanSessionAdapter(getApplication(), this);
        scanSessionAdapter.setOnListChangedListener(this);
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

    @Override
    public void onListChanged() {
        updateEmptyState();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.inflateMenu(this, menu, R.menu.failed_saved_menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.failed_saved_action_Help) {
            Utils.showHelpDialog(getSupportFragmentManager());
            return true;
        } else if (id == R.id.failed_saved_action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.failed_saved_action_auth) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.failed_saved_action_settings) {
            Utils.showToast(this, "Settings not implemented yet!", 0);
            return true;
        } else if (id == R.id.failed_saved_new_session) {
            Intent intent = new Intent(this, ScanFromToActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.failed_saved_scan_info){
            Intent intent = new Intent(this, RetrieveScanInfoActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

