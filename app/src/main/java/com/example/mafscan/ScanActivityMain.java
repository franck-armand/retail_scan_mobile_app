package com.example.mafscan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
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
    private ProgressBar progressBar;
    private TextView progressText;
    private ConstraintLayout progressOverlay;
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
    private String fromLocationCode;
    private String toLocation;
    private String fromLocationId;
    private String toLocationId;
    private String toLocationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_main);

        // Get the from and to locations from the intent
        Intent intent = getIntent();
        fromLocation = intent.getStringExtra("fromLocation");
        fromLocationId = intent.getStringExtra("fromLocationId");
        fromLocationCode = intent.getStringExtra("fromLocationCode");
        toLocation = intent.getStringExtra("toLocation");
        toLocationId = intent.getStringExtra("toLocationId");
        toLocationCode = intent.getStringExtra("toLocationCode");

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.scanToolbarMain);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            String title = fromLocationCode + " → " + toLocationCode;
            getSupportActionBar().setTitle(title);
        }

        // Initialize progress bar
        progressBar = findViewById(R.id.progress_bar);
        progressText = findViewById(R.id.progress_text);
        progressOverlay = findViewById(R.id.progress_overlay);

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
                        // TODO: Warning, Compiler suggesting to find a specific notification message
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

    private List<Map<String, Object>> retrieveAndFormatScanData()
    {
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
        return scanDataToSend;
    }
    private void sendScanData() {
        // DONE: Checking if this approach of saving to Room then delete on sent to server is robust and well written
        // Save all scan records to Room first
        saveScanDataToDatabase(scanDataList, 0); // Auto-save

        // Retrieve and format scan data to be sent to Room and SQL server
        List<Map<String, Object>> scanDataToSend = retrieveAndFormatScanData();

        // establish database connection and send data
        sendScanActivity(scanDataToSend);
    }

    private void sendScanActivity(List<Map<String, Object>> scanDataToSend) {
        if (scanDataToSend.isEmpty()) return;

        runOnUiThread(() -> {progressOverlay.setVisibility(View.VISIBLE);
                            clearScanButton.setEnabled(false);
                            validateButton.setEnabled(false);
        });

        int total = scanDataToSend.size();
        progressBar.setMax(total);
        progressBar.setProgress(0);
        progressText.setText("Progress: 0/" + total);

        List<Map<String, Object>> failedRecords = new ArrayList<>();

        executor.execute(() -> {
            try(Connection connection = SqlServerConHandler.establishSqlServCon()){
                if(connection == null){
                    throw new SQLException("Connection to SQL Server failed");
                }

                String query = "INSERT INTO Scan_Reading (" +
                        "Scan_Value, " +
                        "Scan_Type, " +
                        "Scan_Qty, " +
                        "Scan_DateUTC, " +
                        "Scan_DeviceSerialNumber, " +
                        "Loc_Id_From, " +
                        "Loc_Id_To ) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

                try(PreparedStatement statement = connection.prepareStatement(query)){
                    int successCount = 0;
                    for (int i = 0; i < scanDataToSend.size(); i++){
                        Map<String, Object> data = scanDataToSend.get(i);

                        try {
                            statement.setString(1, (String) data.get("scannedData"));
                            statement.setString(2, (String) data.get("codeType"));
                            statement.setFloat(3, (Float) data.get("scanCount"));

                            // Parsing date to dateTime (sql server format)
                            parseDateSqlServerFormat(data, statement);

                            statement.setString(5, (String) data.get("deviceSerialNumber"));
                            statement.setString(6, (String) data.get("fromLocationId"));
                            statement.setString(7, (String) data.get("toLocationId"));
                            statement.executeUpdate();
                            Log.d(TAG, "Scan Data sent to SQL Server " + scanDataToSend);

                            // Delete record from room is sent to server
                            DeleteScanRecordFromRoom((String) data.get("scannedData"),
                                                    (String) data.get("scanDate"));

                            // Update the success count
                            successCount++;

                        } catch (SQLException e) {
                            failedRecords.add(data);
                        }
                        // Update the progress bar
                        int progress = i + 1;
                        handler.post(() -> {
                            progressBar.setProgress(progress);
                            progressText.setText("Progress: " + progress + "/" + total);
                        });
                        try {
                            Thread.sleep(500); // 500ms delay
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // Restore interrupt status
                        }
                    }
                    int finalSuccessCount = successCount;
                    handler.post(() -> {
                        progressOverlay.setVisibility(View.GONE);
                        validateButton.setEnabled(true);
                        clearScanButton.setEnabled(true);
                        clearRecyclerViewUpdateBtnVisibility();
                        // TODO: Replace Toast with a dialog later
                        Toast.makeText(ScanActivityMain.this,
                                "Data sent successfully!",
                                Toast.LENGTH_SHORT).show();
                        showSummary(finalSuccessCount, failedRecords.size(), total);
                    });
                }
            } catch (SQLException e) {
                handler.post(() -> {
                    progressOverlay.setVisibility(View.GONE);
                    showSummary(0, scanDataToSend.size(), total);
                    // TODO: Replace Toast with a dialog later
                    Toast.makeText(ScanActivityMain.this,
                            "Error sending data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // TODO: Handle what to show in case of error and actions to be taken
                    // Clear the session display a Dialog to inform the user of what has been sent
                    // like a recap, and what has not been sent and therefore saved and can be sent again
                });
                Log.e(TAG, "Error sending data: " + e.getMessage());
            }
        });
    }

    private void showSummary(int successCount, int failCount, int total) {
        new AlertDialog.Builder(this)
                .setTitle("Upload Summary: " + total + " scans")
                .setMessage("✅ Sent: " + successCount + "\n\n❌ Failed: " + failCount +
                        "\n\nFailed records are saved internally and can be sent again")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
    private void parseDateSqlServerFormat(Map<String, Object> data, PreparedStatement statement)
            throws SQLException {
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
    }

    private void clearRecyclerViewUpdateBtnVisibility() {
        scanDataList.clear();
        // TODO: Warning, Compiler suggesting to find a specific notification message
        adapter.notifyDataSetChanged();
        hasValidScan = false;
        validateButton.setVisibility(View.GONE);
        clearScanButton.setVisibility(View.GONE);
        updateHintMessageVisibility();
    }

    private void DeleteScanRecordFromRoom(String scannedData, String scanDate){
        AppDatabase db = AppDatabase.getDatabase(this);
        ScanRecordDao scanRecordDao = db.scanRecordDao();

        // Find the corresponding ScanRecord in the Room database
        new Thread(() -> {
            scanRecordDao.deleteByScannedDataAndDate(scannedData, scanDate);
            Log.d(TAG, "Scan record deleted from Room: " + scannedData);
        }).start();
    }

    private void saveScanDataToDatabase(List<ScanData> scanDataList, int saveType) {
        AppDatabase db = AppDatabase.getDatabase(this);
        ScanRecordDao scanRecordDao = db.scanRecordDao();

        List<ScanRecord> scanRecords = new ArrayList<>();
        for (ScanData scanData : scanDataList) {
            ScanRecord scanRecord = new ScanRecord();
            scanRecord.scannedData = scanData.getScannedData();
            scanRecord.codeType = scanData.getCodeType();
            scanRecord.scanCount = scanData.getScanCount();
            scanRecord.scanDate = scanData.getFormattedScanDate();
            scanRecord.deviceSerialNumber = DatalogicUtils.getDeviceInfo();
            scanRecord.fromLocationId = fromLocationId;
            scanRecord.toLocationId = toLocationId;
            scanRecord.fromLocationName = fromLocation;
            scanRecord.toLocationName = toLocation;
            scanRecord.fromLocationCode = fromLocationCode;
            scanRecord.toLocationCode = toLocationCode;
            scanRecord.saveType = saveType; // 0 for auto-save, 1 for manual save
            scanRecord.isSentToServer = 0; // Initially not sent to server
            scanRecord.sendAttemptCount = 0; // Initially no send attempts

            scanRecords.add(scanRecord);
        }
        new Thread(() -> {
            List<Long> result = scanRecordDao.insertScanRecords(scanRecords);
            Log.d(TAG, "Scan records inserted with IDs: " + result);
        }).start();
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