package com.example.mafscan;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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
import java.util.Objects;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanMainActivity extends AppCompatActivity implements
        ScanDataAdapter.OnItemClickListener {
    private final String TAG = getClass().getSimpleName();
    private ProgressBar progressBar;
    private TextView progressText;
    private ConstraintLayout progressOverlay;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private LinkedList<ScanData> scanDataList;
    private ScanDataAdapter adapter;
    private Button validateButton;
    private FloatingActionButton clearScanButton;
    private boolean isSessionCleared = false;
    private boolean isSendingData = false;
    private TextView emptyHintMessage;
    private String fromLocation;
    private String fromLocationCode;
    private String toLocation;
    private String fromLocationId;
    private String toLocationId;
    private String toLocationCode;
    private RecyclerView recyclerView;
    private ScanRecordDao scanRecordDao;
    private ScanSessionDao scanSessionDao;
    private String sessionId;

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
        recyclerView = findViewById(R.id.recycler_view_scans);
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

        // Disable back button when data is being sent
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSendingData) {
                    Toast.makeText(ScanMainActivity.this,
                            "Please wait while data is being sent.", Toast.LENGTH_SHORT).show();
                } else {
                    setEnabled(false);
                    finish();
                }
            }
        });

        // Set the click listener on the buttons
        clearScanButton.setOnClickListener(v -> clearScanSession());
        validateButton.setOnClickListener(v -> sendScanData());

        // Attach the SwipeToDeleteCallback to the RecyclerView
        ItemTouchHelper itemTouchHelper = onItemSwapLeftRight();
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Initialize database
        AppDatabase db = AppDatabase.getDatabase(this);
        scanRecordDao = db.scanRecordDao();
        scanSessionDao = db.scanSessionDao();

        // Enable scanner trigger
        DataLogicUtils.setTriggersEnabled(true);

        //Initialize scanner
        if (DataLogicUtils.initializeScanner(this)) {
            DataLogicUtils.setScanListener((scannedData, codeType) ->
                    runOnUiThread(() -> {
                        if (scannedData != null && !scannedData.isEmpty()) {
                            handleScannedData(scannedData, codeType);
                        } else {
                            Toast.makeText(this, "Empty Scan", Toast.LENGTH_SHORT).show();
                        }
                    })
            );
        }
    }

    private CompletableFuture<String> createNewScanSession() {
        CompletableFuture<String> future = new CompletableFuture<>();
        executor.execute(() -> {
            try {
                String newSessionId = UUID.randomUUID().toString();
                ScanSession existingSession = scanSessionDao.getScanSessionById(newSessionId);

                while (existingSession != null) {
                    newSessionId = UUID.randomUUID().toString(); // Regenerate if duplicate
                    existingSession = scanSessionDao.getScanSessionById(newSessionId);
                }
                sessionId = newSessionId;

                ScanSession scanSession = new ScanSession();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS",
                        Locale.getDefault());
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                String formattedDate = dateFormat.format(new Date());

                scanSession.sessionId = sessionId;
                scanSession.sessionCreationDate = formattedDate;
                long result = scanSessionDao.insertScanSession(scanSession);

                Log.d(TAG, "Scan session created with ID: " + sessionId + " result: " + result);
                future.complete(sessionId); // Complete the future with the sessionId
            } catch (Exception e) {
                Log.e(TAG, "Error creating scan session: " + e.getMessage());
                future.completeExceptionally(e); // Complete the future exceptionally
            }
        });
        return future;
    }

    @NonNull
    private ItemTouchHelper onItemSwapLeftRight() {
        Utils.SwipeToDeleteCallback swipeToDeleteCallback = new Utils.SwipeToDeleteCallback(this,
                position -> {
                    // Swipe left to delete
                    adapter.removeItem(position);
                    updateHintMessageVisibility();
                    onSwipeDeleteEmpty(scanDataList.isEmpty());
                },
                position -> {
                    // Swipe right to update
                    ScanData scanData = scanDataList.get(position);
                    DialogUtils.showItemDialog(this, scanData, (scanData1, quantity) -> {
                        scanData1.setScanCount(quantity);
                        adapter.updateItem(position, scanData1);
                    });
                });
        return new ItemTouchHelper(swipeToDeleteCallback);
    }
    private void handleScannedData(String scannedData, String codeType) {
        Date now = new Date();
        boolean found = false;
        int position = -1;
        for (int i = 0; i < scanDataList.size(); i++) {
            ScanData existingScan = scanDataList.get(i);
            if (existingScan.getScannedData().equals(scannedData)) {
                // Update existing scan
                existingScan.setScanCount(existingScan.getScanCount() + 1.0f);
                existingScan.setScanDate(now);
                adapter.notifyItemChanged(i);
                position = i;
                found = true;
                break;
            }
        }
        if (!found) {
            // Add new scan
            ScanData newScan = new ScanData(scannedData, codeType, now);
            scanDataList.addFirst(newScan);
            adapter.notifyItemInserted(0);
            position = 0;
        }
        // Scroll to the current record
        recyclerView.scrollToPosition(position);

        // Update the hint message and button visibility
        onSwipeDeleteEmpty(scanDataList.isEmpty());
        Log.d(TAG,
                "Scanned Data: " + scannedData + "," +
                        " Code Type: " + codeType + "," +
                        " Date: " + now + "," +
                        " Device Serial Number: " + DataLogicUtils.getDeviceInfo());
    }
    private List<Map<String, Object>> retrieveAndFormatScanData() {
        List<Map<String, Object>> scanDataToSend = new ArrayList<>();
        for (ScanData scanData : scanDataList) {
            Map<String, Object> data = new HashMap<>();
            data.put("scannedData", scanData.getScannedData());
            data.put("codeType", scanData.getCodeType());
            data.put("scanCount", scanData.getScanCount());
            data.put("scanDate", scanData.getFormattedScanDate());
            data.put("deviceSerialNumber", DataLogicUtils.getDeviceInfo());
            data.put("fromLocationId", fromLocationId);
            data.put("toLocationId", toLocationId);
            data.put("sessionId", sessionId);
            scanDataToSend.add(data);
            Log.d(TAG, "Scan Data to Send: " + scanDataToSend);
        }
        return scanDataToSend;
    }
    private void sendScanData() {
        // Create a new session in the database
        createNewScanSession().thenAccept(sessionId -> {
            // Save all scan records to Room first
            saveScanDataToDatabase(scanDataList, 0); // Auto-save
            // Retrieve and format scan data to be sent to SQL server
            List<Map<String, Object>> scanDataToSend = retrieveAndFormatScanData();
            // Establish database connection and send data
            sendScanActivity(scanDataToSend);
            // Clear database (clear empty sessions)
            deleteScanSessionsWithoutRecords();

        }).exceptionally(e -> {
            Log.e(TAG, "Error creating scan session: " + e.getMessage());
            return null;
        });
    }

    private void sendScanActivity(List<Map<String, Object>> scanDataToSend) {
        if (scanDataToSend.isEmpty()) return;
        int total = scanDataToSend.size();
        progressBar.setMax(total);
        progressBar.setProgress(0);
        progressText.setText(R.string.progress_init + total);

        List<Map<String, Object>> failedRecords = new ArrayList<>();
        List<ScanRecord> recordsToDelete = new ArrayList<>();

        executor.execute(() -> {
            try (Connection connection = SqlServerConHandler.establishSqlServCon()) {
                if (connection == null) {
                    throw new SQLException("Connection to SQL Server failed");
                }

                runOnUiThread(() -> {
                    showProgressBar(true);
                    isSendingData = true;
                    disableUserInteraction();
                });

                // Start transaction for the session
                connection.setAutoCommit(false);
                // Step 1: Insert into Scan_Session
                String sessionQuery = "INSERT INTO Scan_Session (" +
                        "Session_Id, " +
                        "Loc_Id_From, " +
                        "Loc_Id_To, " +
                        "Session_CreationDate) " +
                        "VALUES (?, ?, ?, ?)";

                try (PreparedStatement sessionStatement = connection.prepareStatement(sessionQuery)) {
                    sessionStatement.setString(1, sessionId);
                    sessionStatement.setString(2, fromLocationId);
                    sessionStatement.setString(3, toLocationId);

//                    java.util.Calendar calendar = java.util.Calendar.getInstance();
//                    java.util.TimeZone utcTimeZone = java.util.TimeZone.getTimeZone("UTC");
//                    calendar.setTimeZone(utcTimeZone);
//                    Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
//                    sessionStatement.setTimestamp(4, timestamp);

                    sessionStatement.setTimestamp(4, new Timestamp(new Date().getTime()));
                    sessionStatement.executeUpdate();
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e; // Re-throw the exception to be handled later
                } finally {
                    connection.setAutoCommit(true);
                }

                // Step 2: Insert into Scan_Reading (individual transactions)
                String readingQuery = "INSERT INTO Scan_Reading (" +
                        "Scan_Value, " +
                        "Scan_Type, " +
                        "Scan_Qty, " +
                        "Scan_DateUTC, " +
                        "Scan_DeviceSerialNumber, " +
                        "Loc_Id_From, " +
                        "Loc_Id_To, " +
                        "Session_Id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                int successCount = 0;
                for (int i = 0; i < scanDataToSend.size(); i++) {
                    Map<String, Object> data = scanDataToSend.get(i);

                    // Start a new transaction for each scan
                    connection.setAutoCommit(false);
                    try (PreparedStatement readingStatement = connection.prepareStatement(readingQuery)) {
                        readingStatement.setString(1, (String) data.get("scannedData"));
                        readingStatement.setString(2, (String) data.get("codeType"));
                        Float scanCount = (Float) data.get("scanCount");
                        if (scanCount == null) {
                            scanCount = 1.0f; // Default value
                            Log.w(TAG, "scanCount is null or not a Float");
                        }
                        readingStatement.setFloat(3, scanCount);
                         // readingStatement.setFloat(3, (Float) data.get("scanCount"));
                        // Parsing date to dateTime (sql server format)
                        parseDateSqlServerFormat(data, readingStatement);
                        readingStatement.setString(5, (String) data.get("deviceSerialNumber"));
                        readingStatement.setString(6, (String) data.get("fromLocationId"));
                        readingStatement.setString(7, (String) data.get("toLocationId"));
                        readingStatement.setString(8, (String) data.get("sessionId"));
                        readingStatement.executeUpdate();
                        Log.d(TAG, "Scan Data sent to SQL Server " + data);

                        // Commit the transaction for this scan
                        connection.commit();

                        // Add the record to the list for deletion later
                        ScanRecord scanRecord = scanRecordDao.getScanRecordByScannedData(
                                (String) data.get("scannedData"), sessionId);
                        if (scanRecord != null) {
                            recordsToDelete.add(scanRecord);
                        }

                        // Update the success count
                        successCount++;

                    } catch (SQLException e) {
                        // Rollback the transaction for this scan
                        connection.rollback();
                        failedRecords.add(data);
                    } finally {
                        // Reset auto-commit for the next scan
                        connection.setAutoCommit(true);
                    }
                    // Update the progress bar
                    int progress = i + 1;
                    String progressLabel = getString(R.string.progress);
                    String formattedProgress = progressLabel + " " + progress + "/" + total;
                    handler.post(() -> {
                        progressBar.setProgress(progress);
                        progressText.setText(formattedProgress);
                    });
                    try {
                        Thread.sleep(500); // 500ms delay
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Restore interrupt status
                    }
                }
                // Step 3: Update Scan_Session
                try {
                    connection.setAutoCommit(false);
                    String updateSessionQuery = "UPDATE Scan_Session SET Session_IsActive = 1 WHERE Session_Id = ?";
                    try (PreparedStatement updateSessionStatement = connection.prepareStatement(updateSessionQuery)) {
                        updateSessionStatement.setString(1, sessionId);
                        updateSessionStatement.executeUpdate();
                        connection.commit();
                    }
                } catch (SQLException e) {
                    connection.rollback();
                } finally {
                    connection.setAutoCommit(true);
                }
                // Delete records from Room after successful SQL Server operations
                for (ScanRecord record : recordsToDelete) {
                    scanRecordDao.deleteScanRecord(record);
                }
                // On success, update the UI
                int finalSuccessCount = successCount;
                handler.post(() -> {
                    OnProcessComplete();
                    clearScanSession();
                    showSummary(finalSuccessCount, failedRecords.size(), total);
                    Toast.makeText(ScanMainActivity.this,
                            "Data sent successfully!",
                            Toast.LENGTH_SHORT).show();
                });
            } catch (SQLException e) {
                handler.post(() -> {
                    OnProcessComplete();
                    // To avoid showing both dialogs
                    if (!scanDataList.isEmpty()) {
                        showSummary(0, scanDataToSend.size(), total);
                    }
                    Toast.makeText(ScanMainActivity.this,
                            "Error sending data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // TODO: Handle what to show in case of error and actions to be taken
                    clearScanSession();
                });
                Log.e(TAG, "Error sending data: " + e.getMessage());
            }
        });
    }

    private void deleteScanSessionsWithoutRecords() {
        executor.execute(() -> {
            List<ScanSession> sessionsToDelete = scanSessionDao.getScanSessionsWithoutRecords();
            for (ScanSession session : sessionsToDelete) {
                Log.d(TAG, "Deleting session with ID: " + session.sessionId);
                scanSessionDao.deleteSessionWithScanBySessionId(session.sessionId);
            }
        });
    }

    private void OnProcessComplete() {
        showProgressBar(false);
        enableUserInteraction();
        isSendingData = false;
    }

    private void showProgressBar(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressText.setVisibility(show ? View.VISIBLE : View.GONE);
        clearScanButton.setEnabled(!show);
        validateButton.setEnabled(!show);
    }

    private void showSummary(int successCount, int failCount, int total) {
        DialogUtils.showInvalidSelectionDialog(
                this,
                "Upload Summary: " + successCount + "/" + total +" scans",
                "✅ Sent: " + successCount + "\n\n❌ Failed: " + failCount +
                        "\n\nFailed records are saved internally and can be sent again.",
                "OK",
                (dialog, which) -> dialog.dismiss(),
                null,
                null
        );
    }

    private void parseDateSqlServerFormat(Map<String, Object> data, PreparedStatement statement)
            throws SQLException {
        try {
            String datePattern = "yyyy-MM-dd HH:mm:ss:SSS";
            SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern,
                    Locale.getDefault());
            Date scanDate = dateFormat.parse((String) Objects.requireNonNull(data.get("scanDate")));
            assert scanDate != null;
            statement.setTimestamp(4, new Timestamp(scanDate.getTime()));
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + e.getMessage());
        } catch (java.text.ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveScanDataToDatabase(List<ScanData> scanDataList, int saveType) {
        executor.execute(() -> {
            List<ScanRecord> scanRecords = new ArrayList<>();
            for (ScanData scanData : scanDataList) {
                ScanRecord scanRecord = new ScanRecord();
                scanRecord.scannedData = scanData.getScannedData();
                scanRecord.codeType = scanData.getCodeType();
                scanRecord.scanCount = scanData.getScanCount();
                scanRecord.scanDate = scanData.getFormattedScanDate();
                scanRecord.deviceSerialNumber = DataLogicUtils.getDeviceInfo();
                scanRecord.fromLocationId = fromLocationId;
                scanRecord.toLocationId = toLocationId;
                scanRecord.fromLocationName = fromLocation;
                scanRecord.toLocationName = toLocation;
                scanRecord.fromLocationCode = fromLocationCode;
                scanRecord.toLocationCode = toLocationCode;
                scanRecord.saveType = saveType; // 0 for auto-save, 1 for manual save
                scanRecord.isSentToServer = 0; // Initially not sent to server
                scanRecord.sendAttemptCount = 0; // Initially no send attempts
                scanRecord.sessionId = sessionId;
                scanRecords.add(scanRecord);
            }
            try {
                List<Long> result = scanRecordDao.insertScanRecords(scanRecords);
                Log.d(TAG, "Scan records inserted with IDs: " + result);
            } catch (Exception e) {
                Log.e(TAG, "Error inserting scan records: " + e.getMessage());
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
        executor.shutdown();
        DataLogicUtils.stopScanning();
        //DataLogicUtils.releaseScanner();

    }

    @Override
    protected void onPause() {
        super.onPause();
        DataLogicUtils.setTriggersEnabled(false);
    }

    @Override
    public void onItemClick(ScanData scanData) {
        DialogUtils.showItemDialog(this, scanData, (scanData1, quantity) -> {
            int position = scanDataList.indexOf(scanData1);
            adapter.updateItem(position, scanData1);
        });
    }

    // Implement the scanning UI state
    @Override
    protected void onResume(){
        super.onResume();
        if(isSessionCleared){
            scanDataList.clear();
            validateButton.setVisibility(View.GONE);
        }
        DataLogicUtils.setTriggersEnabled(true);
    }

    private void clearScanSession() {
        scanDataList.clear();
        adapter.notifyDataSetChanged();
        validateButton.setVisibility(View.GONE);
        clearScanButton.setVisibility(View.GONE);
        emptyHintMessage.setVisibility(View.VISIBLE);
        isSessionCleared = true;
    }
    private void onSwipeDeleteEmpty(boolean isEmpty) {
     validateButton.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
     clearScanButton.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
     emptyHintMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
    private void enableUserInteraction() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        validateButton.setEnabled(true);
        clearScanButton.setEnabled(true);
    }
    private void disableUserInteraction() {
        // disable validate button
        validateButton.setEnabled(false);
        clearScanButton.setEnabled(false);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}