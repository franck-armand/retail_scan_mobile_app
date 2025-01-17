package com.example.mafscan;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Objects;

public class ScanActivityMain extends AppCompatActivity {
    private static final String TAG = "ScanActivityMain";
    private ArrayList<String> scannedDataList;
    private ArrayAdapter<String> adapter;
    private boolean hasValidScan = false;
    private Button validateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_main);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.scanToolbarMain);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Scanning");

        // Initialize scanned data list
        scannedDataList = new ArrayList<>();
        ListView listView = findViewById(R.id.list_scanned_data);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scannedDataList);
        listView.setAdapter(adapter);

        // Initialize the validate button and set its visibility
        validateButton = findViewById(R.id.validateScanButton);
        validateButton.setVisibility(View.GONE);

        //Initialize scanner
        if (KeyenceUtils.initializeScanner(this)) {
            KeyenceUtils.setScanListener(scannedData -> {
                runOnUiThread(() -> {
                    Log.d(TAG, "Scanned data: " + scannedData);
                    if (scannedData != null && !scannedData.isEmpty()) {
                        scannedDataList.add(scannedData);
                        adapter.notifyDataSetChanged();
                        if (!hasValidScan) {
                            hasValidScan = true;
                            validateButton.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(this, "Empty Scan",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KeyenceUtils.stopScanning();
        KeyenceUtils.releaseScanner();

    }
}