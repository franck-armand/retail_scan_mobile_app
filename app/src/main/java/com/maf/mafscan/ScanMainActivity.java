package com.maf.mafscan;

import static com.maf.mafscan.Utils.showToast;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
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
    private LinearLayout sendSessionButton, saveSessionButton, clearSessionButton;
    private boolean isSessionCleared = false;
    private boolean isSendingData = false;
    private TextView emptyHintMessage;
    private String fromLocation;
    private String fromLocationCode;
    private String toLocation;
    private String fromLocationId;
    private String toLocationId;
    private String toLocationCode;
    private String sessionType;
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
        Log.d(TAG, "fromLocation: " + fromLocation);
        fromLocationId = intent.getStringExtra("fromLocationId");
        Log.d(TAG, "fromLocationId: " + fromLocationId);
        fromLocationCode = intent.getStringExtra("fromLocationCode");
        Log.d(TAG, "fromLocationCode: " + fromLocationCode);
        toLocation = intent.getStringExtra("toLocation");
        Log.d(TAG, "toLocation: " + toLocation);
        toLocationId = intent.getStringExtra("toLocationId");
        Log.d(TAG, "toLocationId: " + toLocationId);
        toLocationCode = intent.getStringExtra("toLocationCode");
        Log.d(TAG, "toLocationCode: " + toLocationCode);
        sessionType = intent.getStringExtra("sessionType");
        Log.d(TAG, "sessionType: " + sessionType);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.scanToolbarMain);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            String title = setAppBarTitle();
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        sendSessionButton = findViewById(R.id.sendSessionButton);
        saveSessionButton = findViewById(R.id.saveSessionButton);
        clearSessionButton = findViewById(R.id.clearSessionButton);

        sendSessionButton.setVisibility(View.GONE);
        clearSessionButton.setVisibility(View.GONE);
        saveSessionButton.setVisibility(View.GONE);

        // Set the click listener on the buttons
        clearSessionButton.setOnClickListener(v -> clearScanSession());
        sendSessionButton.setOnClickListener(v -> sendScanData());
        saveSessionButton.setOnClickListener(v -> saveScanSession());

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
                            Utils.showToast(this, "Empty Scan", 0);
                        }
                    })
            );
        }
        // Disable back button when data is being sent
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (isSendingData) {
                            Utils.showToast(ScanMainActivity.this,
                                    "Please wait while data is being sent.", 0);
                        } else {
                            setEnabled(false);
                            finish();
                        }
                    }
                });
    }

    @NonNull
    private String setAppBarTitle() {
        String title = "";
        String arrow = "";
        if (sessionType != null) {
            switch (sessionType) {
                case Constants.SCAN_SESSION_RECEPTION:
//                    arrow = " ↓ "; // Down arrow for Reception
                    arrow = getString(R.string.reception_icon);
                    break;
                case Constants.SCAN_SESSION_EXPEDITION:
//                    arrow = " ↑ "; // Up arrow for Expedition
                    arrow = getString(R.string.expedition_icon);
                    break;
                case Constants.SCAN_SESSION_TRANSFER:
                    arrow = " → "; // Right arrow for Transfer
                    break;
                case Constants.SCAN_SESSION_INVENTORY:
                    arrow = getString(R.string.inventory_icon);
                    break;
            }
        }
        if (fromLocation == null && toLocation != null) {
            title = arrow + toLocationCode;
        } else if (toLocation == null && fromLocation != null) {
            title = arrow + fromLocationCode;
        } else if (fromLocationCode != null && toLocationCode != null) {
            title = fromLocationCode + arrow + toLocationCode;
        }
        return title;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.inflateMenu(this, menu, R.menu.scan_session_menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.session_action_Help) {
            Utils.showHelpDialog(getSupportFragmentManager());
            return true;
        }else if (id == R.id.session_action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
//        }else if (id == R.id.session_action_auth) {
//            Intent intent = new Intent(this, LoginActivity.class);
//            startActivity(intent);
//            return true;
        }else if (id == R.id.session_action_settings) {
            showToast(this, "Settings not implemented yet!", 0);
            return true;
//        }else if (id == R.id.session_failed_saved) {
//            Intent intent = new Intent(this, FailedOrSavedScanActivity.class);
//            startActivity(intent);
//            return true;
//        } else if (id == R.id.session_scan_info) {
//            Intent intent = new Intent(this, RetrieveScanInfoActivity.class);
//            startActivity(intent);
//            return true;
        }

        return super.onOptionsItemSelected(item);
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
                SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_LONG,
                        Locale.getDefault());
                dateFormat.setTimeZone(TimeZone.getTimeZone(Constants.TIME_ZONE));
                String formattedDate = dateFormat.format(new Date());

                scanSession.sessionId = sessionId;
                scanSession.sessionType = sessionType;
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
                    DialogUtils.showItemDialog(this, scanData, position,
                            (scanData1, quantity, position1) ->
                                    adapter.updateItem(position1, scanData1));
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
        return Utils.formatScanData(scanDataList, fromLocationId, toLocationId, sessionId);
    }
    private void sendScanData() {
        createNewScanSession().thenCompose(sessionId ->
            saveScanDataToDatabase(scanDataList, 0, sessionId)).thenAccept(
            isSaveSuccessful -> {
                // Retrieve and format scan data to be sent to SQL server
                List<Map<String, Object>> scanDataToSend = retrieveAndFormatScanData();
                // Establish database connection and send data
                sendScanActivity(scanDataToSend);
                // Clear database (clear empty sessions)
                deleteScanSessionsWithoutRecords();
            }).exceptionally(e -> {
                Log.e(TAG, "sendScanData, error creating scan session: " + e.getMessage());
                runOnUiThread(() -> showSavedSessionSummary(scanDataList.size(),
                        "Send Session Failed",
                        "❌ Failed to send scans session, Contact Administrator"));
                return null;
            });
    }
    private void saveScanSession() {
        createNewScanSession().thenCompose(sessionId ->
                saveScanDataToDatabase(scanDataList, 1, sessionId)).thenAccept(
                        isSaveSuccessful -> {
            if (isSaveSuccessful){
                runOnUiThread(() -> showSavedSessionSummary(scanDataList.size(),
                        "Save Session Successful",
                        "✅ Saved scans session"));

            } else {
                runOnUiThread(() -> showSavedSessionSummary(scanDataList.size(),
                        "Save Session Failed",
                        "❌ Failed to save scans session"));
            }
            clearScanSession();
        }).exceptionally(e -> {
            Log.e(TAG, "saveSession, error creating scan session: " + e.getMessage());
            runOnUiThread(() ->showSavedSessionSummary(scanDataList.size(),
                    "Save Session Failed",
                    "❌ Failed to save scans session, Contact Administrator"));
            clearScanSession();
            return null;
        });
    }

//    private void sendScanActivity(List<Map<String, Object>> scanDataToSend) {
//        if (scanDataToSend.isEmpty()) return;
//        int total = scanDataToSend.size();
//        progressBar.setMax(total);
//        progressBar.setProgress(0);
//        progressText.setText(R.string.progress_init + total);
//
//        List<Map<String, Object>> failedRecords = new ArrayList<>();
//        List<ScanRecord> recordsToDelete = new ArrayList<>();
//
//        executor.execute(() -> {
//            try (Connection connection = SqlServerConHandler.establishSqlServCon()) {
//                if (connection == null) {
//                    throw new SQLException("Connection to SQL Server failed");
//                }
//
//                runOnUiThread(() -> {
//                    showProgressBar(true);
//                    isSendingData = true;
//                    disableUserInteraction();
//                });
//
//                // Start transaction for the session
//                connection.setAutoCommit(false);
//                // Step 1: Insert into Scan_Session
//                String sessionQuery = SqlQueryUtils.INSERT_SCAN_SESSION;
//
//                try (PreparedStatement sessionStatement = connection.prepareStatement(sessionQuery)) {
//                    sessionStatement.setString(1, sessionId);
//                    sessionStatement.setString(2, sessionType);
//                    sessionStatement.setString(3, fromLocationId);
//                    sessionStatement.setString(4, toLocationId);
//                    sessionStatement.setString(5, getCurrentUtcDateTimeString());
//                    sessionStatement.executeUpdate();
//                    connection.commit();
//                } catch (SQLException e) {
//                    connection.rollback();
//                    throw e; // Re-throw the exception to be handled later
//                } finally {
//                    connection.setAutoCommit(true);
//                }
//
//                // Step 2: Insert into Scan_Reading (individual transactions)
//                String readingQuery = SqlQueryUtils.INSERT_SCAN_READING;
//
//                int successCount = 0;
//                for (int i = 0; i < scanDataToSend.size(); i++) {
//                    Map<String, Object> data = scanDataToSend.get(i);
//
//                    // Start a new transaction for each scan
//                    connection.setAutoCommit(false);
//                    try (PreparedStatement readingStatement = connection.prepareStatement(readingQuery)) {
//                        readingStatement.setString(1, (String) data.get(Constants.SCANNED_DATA));
//                        readingStatement.setString(2, (String) data.get(Constants.CODE_TYPE));
//                        Float scanCount = (Float) data.get(Constants.SCAN_COUNT);
//                        if (scanCount == null) {
//                            scanCount = 1.0f; // Default value
//                            Log.w(TAG, "scanCount is null or not a Float");
//                        }
//                        readingStatement.setFloat(3, scanCount);
//                        Utils.parseDateSqlServerFormat(data, readingStatement);
//                        readingStatement.setString(5, (String) data.get(Constants.DEVICE_SERIAL_NUMBER));
//                        readingStatement.setString(6, (String) data.get(Constants.FROM_LOCATION_ID));
//                        readingStatement.setString(7, (String) data.get(Constants.TO_LOCATION_ID));
//                        readingStatement.setString(8, (String) data.get(Constants.SCAN_SESSION_ID));
//                        readingStatement.executeUpdate();
//                        Log.d(TAG, "Scan Data sent to SQL Server " + data);
//
//                        // Commit the transaction for this scan
//                        connection.commit();
//
//                        // Add the record to the list for deletion later
//                        ScanRecord scanRecord = scanRecordDao.getScanRecordByScannedData(
//                                (String) data.get(Constants.SCANNED_DATA), sessionId);
//                        if (scanRecord != null) {
//                            recordsToDelete.add(scanRecord);
//                        }
//
//                        // Update the success count
//                        successCount++;
//
//                    } catch (SQLException e) {
//                        // Rollback the transaction for this scan
//                        connection.rollback();
//                        failedRecords.add(data);
//                    } finally {
//                        // Reset auto-commit for the next scan
//                        connection.setAutoCommit(true);
//                    }
//                    // Update the progress bar
//                    int progress = i + 1;
//                    String progressLabel = getString(R.string.progress);
//                    String formattedProgress = progressLabel + " " + progress + "/" + total;
//                    handler.post(() -> {
//                        progressBar.setProgress(progress);
//                        progressText.setText(formattedProgress);
//                    });
//                    try {
//                        Thread.sleep(500); // 500ms delay
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt(); // Restore interrupt status
//                    }
//                }
//                // Step 3: Update Scan_Session
//                try {
//                    connection.setAutoCommit(false);
//                    String updateSessionQuery = SqlQueryUtils.UPDATE_SCAN_SESSION;
//                    try (PreparedStatement updateSessionStatement = connection.prepareStatement(
//                            updateSessionQuery)) {
//                        updateSessionStatement.setString(1, sessionId);
//                        updateSessionStatement.executeUpdate();
//                        connection.commit();
//                    }
//                } catch (SQLException e) {
//                    connection.rollback();
//                } finally {
//                    connection.setAutoCommit(true);
//                }
//                // Delete records from Room after successful SQL Server operations
//                for (ScanRecord record : recordsToDelete) {
//                    scanRecordDao.deleteScanRecord(record);
//                }
//                // On success, update the UI
//                int finalSuccessCount = successCount;
//                handler.post(() -> {
//                    OnProcessComplete();
//                    if (finalSuccessCount == total) {
//                        String message = String.format(
//                                this.getString(R.string.scan_resubmitted_success_msg),
//                                finalSuccessCount);
//                        showToast(this, message, 1);
//                    } else {
//                        showSummary(finalSuccessCount, failedRecords.size(), total);
//                    }
//                    clearScanSession();
//                });
//            } catch (SQLException e) {
//                handler.post(() -> {
//                    OnProcessComplete();
//                    // To avoid showing both dialogs
//                    if (!scanDataList.isEmpty()) {
//                        showSummary(0, scanDataToSend.size(), total);
//                    }
//                    showToast(this, "Error sending data: " + e.getMessage(), 1);
//                    clearScanSession();
//                });
//                Log.e(TAG, "Error sending data: " + e.getMessage());
//            }
//        });
//    }

    private void sendScanActivity(List<Map<String, Object>> scanDataToSend) {
        if (scanDataToSend.isEmpty()) return;
        int total = scanDataToSend.size();
        progressBar.setMax(total);
        progressBar.setProgress(0);
        progressText.setText(R.string.progress_init + total);

        executor.execute(() -> {
            Connection connection = null;
            try {
                runOnUiThread(() -> {
                    showProgressBar(true);
                    isSendingData = true;
                    disableUserInteraction();
                });

                // Establish a single connection
                connection = SqlServerConHandler.establishSqlServCon();
                if (connection == null) {
                    throw new SQLException("Failed to establish database connection");
                }

                // Initialize success count
                int successCount = 0;

                // Process each scan record
                for (int i = 0; i < scanDataToSend.size(); i++) {
                    List<Map<String, Object>> singleScanData = new ArrayList<>();
                    singleScanData.add(scanDataToSend.get(i));

                    boolean success = Utils.executeScanDataDatabaseOperations(
                            singleScanData,
                            sessionId,
                            sessionType,
                            fromLocationId,
                            toLocationId,
                            scanRecordDao,
                            connection
                    );

                    if (success) {
                        successCount++;
                    }

                    // Update progress bar after each record
                    final int progress = i + 1;
                    Utils.updateProgressBar(progress, total, progressBar, progressText, handler,
                            this);

                    try {
                        Thread.sleep(500); // 500ms delay
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Restore interrupt status
                    }
                }

                // Handle overall success/failure after processing all records
                final int finalSuccessCount = successCount;
                if (finalSuccessCount == total) {
                    handler.post(() -> {
                        OnProcessComplete();
                        String message = String.format(
                                getString(R.string.scan_resubmitted_success_msg),
                                finalSuccessCount);
                        showToast(this, message, 1);
                        clearScanSession();
                    });
                } else {
                    handler.post(() -> {
                        OnProcessComplete();
                        showSummary(finalSuccessCount, total - finalSuccessCount, total);
                        clearScanSession();
                    });
                    Log.e(TAG, "Error sending data: Some records failed during database operations");
                }
            } catch (java.sql.SQLException e) {
                handler.post(() -> {
                    OnProcessComplete();
                    showSummary(0, scanDataToSend.size(), total);
                    showToast(this, "Error sending data: " + e.getMessage(), 1);
                    clearScanSession();
                });
                Log.e(TAG, "Error sending data: " + e.getMessage());
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        Log.e(TAG, "Error closing database connection: " + e.getMessage());
                    }
                }
                handler.post(() -> isSendingData = false);
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
        clearSessionButton.setEnabled(!show);
        sendSessionButton.setEnabled(!show);
        saveSessionButton.setEnabled(!show);
    }

    private void showSummary(int successCount, int failCount, int total) {
        DialogUtils.showInvalidSelectionDialog(
                this,
                "Upload Summary: " + successCount + "/" + total +" scans",
                "✅ Sent: " + successCount + "\n\n❌ Failed: " + failCount +
                        "\n\nFailed records are saved internally and can be sent again.",
                "Continue Scanning",
                (dialog, which) -> dialog.dismiss(),
                "View Saved Scans",
                (dialog, which) -> {
                    Intent intent = new Intent(ScanMainActivity.this,
                            FailedOrSavedScanActivity.class);
                    startActivity(intent);
                    dialog.dismiss();
                }
        );
    }

    private void showSavedSessionSummary(int totalScans, String title, String message){
        DialogUtils.showInvalidSelectionDialog(
                this,
                title + ": " + totalScans +" scans",
                message + ".",
                "Continue Scanning",
                (dialog, which) -> dialog.dismiss(),
                "View Saved Scans",
                (dialog, which) -> {
                    Intent intent = new Intent(ScanMainActivity.this,
                            FailedOrSavedScanActivity.class);
                    startActivity(intent);
                    dialog.dismiss();
                }
        );
    }

    private CompletableFuture<Boolean> saveScanDataToDatabase(List<ScanData> scanDataList,
                                                              int saveType, String sessionId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        executor.execute(() -> {
            try {
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
                    scanRecord.sessionId = sessionId; // Use the passed sessionId
                    scanRecords.add(scanRecord);
                }
                List<Long> result = scanRecordDao.insertScanRecords(scanRecords);
                Log.d(TAG, "Scan records inserted with IDs: " + result);
                future.complete(true); // Complete the future with true
            } catch (Exception e) {
                Log.e(TAG, "Error inserting scan records: " + e.getMessage());
                future.complete(false); // Complete the future with false
            }
        });
        return future;
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
        //DataLogicUtils.stopScanning();
        //DataLogicUtils.releaseScanner();

    }

    @Override
    protected void onPause() {
        super.onPause();
        DataLogicUtils.setTriggersEnabled(false);
    }

    @Override
    public void onItemClick(ScanData scanData) {
        int position = scanDataList.indexOf(scanData);
        if (position != -1) {
            DialogUtils.showItemDialog(this, scanData, position,
                    (scanData1, quantity, position1) ->
                            adapter.updateItem(position1, scanData1));
        }
    }

    // Implement the scanning UI state
    @Override
    protected void onResume(){
        super.onResume();
        if(isSessionCleared){
            scanDataList.clear();
            sendSessionButton.setVisibility(View.GONE);
            saveSessionButton.setVisibility(View.GONE);
        }
        DataLogicUtils.setTriggersEnabled(true);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void clearScanSession() {
        runOnUiThread(() -> {
            scanDataList.clear();
            adapter.notifyDataSetChanged();
            sendSessionButton.setVisibility(View.GONE);
            clearSessionButton.setVisibility(View.GONE);
            saveSessionButton.setVisibility(View.GONE);
            emptyHintMessage.setVisibility(View.VISIBLE);
            isSessionCleared = true;
        });
    }
    private void onSwipeDeleteEmpty(boolean isEmpty) {
     sendSessionButton.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
     saveSessionButton.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
     clearSessionButton.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
     emptyHintMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
    private void enableUserInteraction() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        sendSessionButton.setEnabled(true);
        saveSessionButton.setEnabled(true);
        clearSessionButton.setEnabled(true);
    }
    private void disableUserInteraction() {
        // disable validate button
        sendSessionButton.setEnabled(false);
        saveSessionButton.setEnabled(false);
        clearSessionButton.setEnabled(false);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}