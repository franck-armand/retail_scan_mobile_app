package com.example.mafscan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.net.ParseException;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanActivityMain extends AppCompatActivity implements
        ScanDataAdapter.OnItemClickListener {
    private final String TAG = getClass().getSimpleName();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
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
    private String fromLocationId;
    private String toLocationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_main);

        // Get the from and to locations from the intent
        Intent intent = getIntent();
        fromLocation = intent.getStringExtra("fromLocation");
        fromLocationId = intent.getStringExtra("fromLocationId");
        String fromLocationCode = intent.getStringExtra("fromLocationCode");
        toLocation = intent.getStringExtra("toLocation");
        toLocationId = intent.getStringExtra("toLocationId");
        String toLocationCode = intent.getStringExtra("toLocationCode");

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.scanToolbarMain);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            String title = fromLocationCode + " → " + toLocationCode;
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

        // Set the click listener on the buttons
        clearScanButton.setOnClickListener(v -> clearScanSession());
        validateButton.setOnClickListener(v -> sendScanData());

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

    private void sendScanData() {
        List<Map<String, Object>> scanDataToSend = new ArrayList<>();
        for (ScanData scanData : scanDataList) {
            Map<String, Object> data = new HashMap<>();
            data.put("scannedData", scanData.getScannedData());
            data.put("codeType", scanData.getCodeType());
            data.put("scanCount", scanData.getScanCount());
            data.put("scanDate", scanData.getFormattedScanDate());
            data.put("deviceSerialNumber", DatalogicUtils.getDeviceInfo());
            data.put("fromLocationId", fromLocationId);
            data.put("toLocationId", toLocationId);
            scanDataToSend.add(data);
            Log.d(TAG, "Scan Data to Send: " + scanDataToSend);
        }

        // establish database connection and send data
        executor.execute(() -> {
            try(Connection connection = SqlServerConHandler.establishSqlServCon()){
                if(connection == null){
                    throw new SQLException("Connection to SQL Server failed");
                }

                String query = "INSERT INTO Scan_Reading (" +
                        "Scan_Value," +
                        " Scan_Type, " +
                        "Scan_Qty, " +
                        "Scan_DateUTC, " +
                        " Scan_DeviceSerialNumber," +
                        " Loc_Id_From, " +
                        "Loc_Id_To ) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

                try(PreparedStatement statement = connection.prepareStatement(query)){
                    for(Map<String, Object> data : scanDataToSend){
                        statement.setString(1, (String) data.get("scannedData"));
                        statement.setString(2, (String) data.get("codeType"));
                        statement.setFloat(3, (Float) data.get("scanCount"));

                        // Parsing date to dateTime
                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                    Locale.getDefault());
                            Date scanDate = dateFormat.parse((String) data.get("scanDate"));
                            statement.setTimestamp(4, new Timestamp(scanDate.getTime()));
                        } catch (ParseException e) {
                            Log.e(TAG, "Error parsing date: " + e.getMessage());
                        } catch (java.text.ParseException e) {
                            throw new RuntimeException(e);
                        }

                        statement.setString(5, (String) data.get("deviceSerialNumber"));
                        statement.setString(6, (String) data.get("fromLocationId"));
                        statement.setString(7, (String) data.get("toLocationId"));
                        statement.executeUpdate();
                        Log.d(TAG, "Scan Data sent to SQL Server " +scanDataToSend);
                    }
                    handler.post(() -> {
                        scanDataList.clear();
                        adapter.notifyDataSetChanged();
                        hasValidScan = false;
                        validateButton.setVisibility(View.GONE);
                        clearScanButton.setVisibility(View.GONE);
                        updateHintMessageVisibility();

                        Toast.makeText(ScanActivityMain.this,
                                "Data sent successfully!",
                                Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (SQLException e) {
                handler.post(() -> {
                    Toast.makeText(ScanActivityMain.this,
                            "Error sending data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // TODO: Handle retries or temporally failure
                });
                Log.e(TAG, "Error sending data: " + e.getMessage());
            }
        });
    }

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