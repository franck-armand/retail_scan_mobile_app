package com.example.mafscan;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;

public class ScanActivityMain extends AppCompatActivity implements
        ScanDataAdapter.OnItemClickListener {
    private static final String TAG = "ScanActivityMain";
    private LinkedList<ScanData> scanDataList;
    private ScanDataAdapter adapter;
    private boolean hasValidScan = false;
    private Button validateButton;
    private String lastScannedData = "";
    private boolean isNewScanSession = true;
    private int triggerScanCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_main);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.scanToolbarMain);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Scanning");

        // Initialize scanned data list
        scanDataList = new LinkedList<>();
        RecyclerView recyclerView = findViewById(R.id.recycler_view_scans);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScanDataAdapter(scanDataList, this);
        recyclerView.setAdapter(adapter);

        // Set the click listener on the adapter
        adapter.setOnItemClickListener(this);

        // Initialize the validate button and set its visibility
        validateButton = findViewById(R.id.validateScanButton);
        validateButton.setVisibility(View.GONE);

        //Initialize scanner
        if (KeyenceUtils.initializeScanner(this)) {
            KeyenceUtils.setScanListener((scannedData, codeType) -> {
                runOnUiThread(() -> {
                    Log.d(TAG, "Scanned data: " + scannedData);
                    if (scannedData != null && !scannedData.isEmpty()) {
                        if (isNewScanSession) {
                            lastScannedData = "";
                            isNewScanSession = false;
                        }
                        if (!scannedData.equals(lastScannedData)) {
                            Date now = new Date();
                            ScanData newScan = new ScanData(scannedData, codeType, now);
                            scanDataList.addFirst(newScan);
                            adapter.notifyDataSetChanged();
                        }
                        lastScannedData = scannedData;
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: KeyCode: " + keyCode +
                ", ScanCode: " + event.getScanCode());
        if (triggerScanCode == 0) {
            triggerScanCode = event.getScanCode();
        }
        if (event.getScanCode() == triggerScanCode) {
            isNewScanSession = true;
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp: KeyCode: " + keyCode + ", ScanCode: " + event.getScanCode());
        if (event.getScanCode() == triggerScanCode) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KeyenceUtils.stopScanning();
        KeyenceUtils.releaseScanner();

    }

    // Implement the interface method
    @Override
    public void onItemClick(ScanData scanData) {
        DialogUtils.showItemDialog(this, scanData, new DialogUtils.OnItemClickListener() {
            @Override
            public void onValidate(ScanData scanData, int quantity) {
                int position = scanDataList.indexOf(scanData);
                adapter.updateItem(position, scanData);
            }

        });
    }
}