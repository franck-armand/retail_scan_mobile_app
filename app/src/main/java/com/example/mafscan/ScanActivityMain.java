package com.example.mafscan;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;


public class ScanActivityMain extends AppCompatActivity implements
        ScanDataAdapter.OnItemClickListener {
    private final String TAG = getClass().getName();
    private LinkedList<ScanData> scanDataList;
    private ScanDataAdapter adapter;
    private boolean hasValidScan = false;
    private Button validateButton;
    private FloatingActionButton clearScanButton;
    private long lastScanTime = 0;
    private static final long DEBOUNCE_DELAY = 100;
    private boolean isSessionCleared = false;
    private TextView emptyHintMessage;
    private String fromLocation;
    private String toLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_main);

        // Get the from and to locations from the intent
        fromLocation = getIntent().getStringExtra("fromLocation");
        toLocation = getIntent().getStringExtra("toLocation");

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.scanToolbarMain);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            String title = fromLocation + " → " + toLocation;
            getSupportActionBar().setTitle(title);
        }

        // Initialize scanned data list
        scanDataList = new LinkedList<>();
        RecyclerView recyclerView = findViewById(R.id.recycler_view_scans);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScanDataAdapter(scanDataList, this);
        recyclerView.setAdapter(adapter);

        // Set empty activity hint message
        emptyHintMessage = findViewById(R.id.empty_hint_message);
        updateHintMessageVisibility();

        // Set the click listener on the adapter
        adapter.setOnItemClickListener(this);

        // Initialize the validate and clear buttons and set theirs visibilities
        validateButton = findViewById(R.id.validateScanButton);
        validateButton.setVisibility(View.GONE);
        clearScanButton = findViewById(R.id.clear_scan_floating_action_Btn);
        clearScanButton.setVisibility(View.GONE);
        clearScanButton.setOnClickListener(v -> clearScanSession());

        //Initialize scanner
//        if (KeyenceUtils.initializeScanner(this)) {
//            KeyenceUtils.setScanListener((scannedData, codeType) -> {
            if (DatalogicUtils.initializeScanner(this)) {
                DatalogicUtils.setScanListener((scannedData, codeType) -> {
                runOnUiThread(() -> {

                    if (scannedData != null && !scannedData.isEmpty()) {
//                        long currentTime = System.currentTimeMillis();
                        // Check debounce timing needed for KeyenceUtils
//                        if (currentTime - lastScanTime < DEBOUNCE_DELAY) {
//                            Log.d(TAG, "Scan ignored due to debounce: " + scannedData);
//                            return; // Ignore scans that occur too quickly or duplicated
//                        }
                        // Update the timestamp of the last scan
//                        lastScanTime = currentTime;
                        Date now = new Date();
                        ScanData newScan = new ScanData(scannedData, codeType, now);
                        scanDataList.addFirst(newScan);
                        adapter.notifyDataSetChanged();
                        Log.d(TAG,
                                "Scanned Data: " + scannedData + "," +
                                " Code Type: " + codeType + "," +
                                " Date: " + now + "," +
                                " Device Serial Number: " + DatalogicUtils.getDeviceInfo());

                        // Update the hint message
                        updateHintMessageVisibility();

                        // Update the validate and clear buttons visibility
                        if (!hasValidScan) {
                            hasValidScan = true;
                            validateButton.setVisibility(View.VISIBLE);
                            clearScanButton.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(this, "Empty Scan", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.d(TAG, "onKeyDown: KeyCode: " + keyCode +
//                ", ScanCode: " + event.getScanCode());
//        if (triggerScanCode == 0) {
//            triggerScanCode = event.getScanCode();
//        }
//        if (event.getScanCode() == triggerScanCode) {
//            isNewScanSession = true;
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        Log.d(TAG, "onKeyUp: KeyCode: " + keyCode +
//                ", ScanCode: " + event.getScanCode());
//        if (event.getScanCode() == triggerScanCode) {
//            return true;
//        }
//        return super.onKeyUp(keyCode, event);
//    }

    private void updateHintMessageVisibility() {
        if (scanDataList.isEmpty()) {
            emptyHintMessage.setVisibility(View.VISIBLE);
        } else {
            emptyHintMessage.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatalogicUtils.stopScanning();
        DatalogicUtils.releaseScanner();
//        KeyenceUtils.stopScanning();
//        KeyenceUtils.releaseScanner();

    }

    // Implement the interface method, call the dialog from the item click in the recycler view
    @Override
    public void onItemClick(ScanData scanData) {
        DialogUtils.showItemDialog(this, scanData, new DialogUtils.OnItemClickListener() {
            @Override
            public void onValidate(ScanData scanData, float quantity) {
                int position = scanDataList.indexOf(scanData);
                adapter.updateItem(position, scanData);
            }
        });
    }

    // Implement the scanning UI state
    @Override
    protected void onResume(){
        super.onResume();
        if(isSessionCleared){
            scanDataList.clear();
            adapter.notifyDataSetChanged();
            hasValidScan = false;
            validateButton.setVisibility(View.GONE);
        }
    }

    // Explicitly clear the scan session with floating action button
    private void clearScanSession() {
        scanDataList.clear();
        adapter.notifyDataSetChanged();
        validateButton.setVisibility(View.GONE);
        clearScanButton.setVisibility(View.GONE);
        emptyHintMessage.setVisibility(View.VISIBLE);
        hasValidScan = false;
        isSessionCleared = true;
    }
}