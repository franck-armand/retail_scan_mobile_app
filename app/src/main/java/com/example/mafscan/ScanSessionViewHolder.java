package com.example.mafscan;

import static com.example.mafscan.Utils.showToast;

import android.content.Context;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.net.ParseException;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanSessionViewHolder extends RecyclerView.ViewHolder {
    //TextView sessionIdTextView;
    private final TextView sessionFromToTextView;
    private final TextView sessionDateTextView;
    private final TextView scanCountTextView;
    private final TextView sessionTypeTextView;
    private final ImageView expandCollapseImageView;
    private final RecyclerView scansRecyclerView;
    private final Button resendButton;
    private final Button deleteButton;
    private final View statusIndicatorView;
    ScanSession currentScanSession;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors. newSingleThreadExecutor();
    private final FailedOrSavedScanActivity failedOrSavedScanActivity;
    private boolean isSendingData = false;
    private final View progressOverlay;
    private final ProgressBar progressBar;
    private final TextView progressText;
    private final ScanRecordAdapter scanRecordAdapter;
    private final String TAG = getClass().getSimpleName();
    private boolean isExpanded = false;
    private final FailedOrSavedScanRepository repository;
    private final Context context;
    private final ScanSessionAdapter adapter;
    private final AppCompatActivity activity;

    public ScanSessionViewHolder(@NonNull View itemView, Context context,
                                 FailedOrSavedScanRepository repository,
                                 ScanSessionAdapter adapter, AppCompatActivity activity) {
        super(itemView);
        this.context = context;
        this.activity = activity;
        this.repository = repository;
        this.adapter = adapter;

        // Get the FailedOrSavedScanActivity instance
        if (context instanceof FailedOrSavedScanActivity) {
            failedOrSavedScanActivity = (FailedOrSavedScanActivity) context;
        } else {
            throw new IllegalStateException("Context must be an instance of FailedOrSavedScanActivity");
        }

        //sessionIdTextView = itemView.findViewById(R.id.sessionIdTextView);
        sessionTypeTextView = itemView.findViewById(R.id.sessionTypeTextView);
        sessionFromToTextView = itemView.findViewById(R.id.sessionFromToTextView);
        sessionDateTextView = itemView.findViewById(R.id.sessionDateTextView);
        scanCountTextView = itemView.findViewById(R.id.scanCountTextView);
        expandCollapseImageView = itemView.findViewById(R.id.expandCollapseImageView);
        scansRecyclerView = itemView.findViewById(R.id.scansRecyclerView);
        resendButton = itemView.findViewById(R.id.resendButton);
        deleteButton = itemView.findViewById(R.id.deleteButton);
        statusIndicatorView = itemView.findViewById(R.id.statusIndicatorView);
        scansRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        scanRecordAdapter = new ScanRecordAdapter();
        scansRecyclerView.setAdapter(scanRecordAdapter);

        // Initialize the progress bar and overlay
        progressOverlay = itemView.findViewById(R.id.resubmit_progress_overlay);
        progressBar = itemView.findViewById(R.id.resubmit_progress_bar);
        progressText = itemView.findViewById(R.id.resubmit_progress_text);

        // Setting click listeners
        itemView.setOnClickListener(v -> toggleExpandCollapse());
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
        resendButton.setOnClickListener(v -> resendScanSession());
    }

    public void bind(ScanSession scanSession, List<ScanRecord> scanRecords) {
        currentScanSession = scanSession;
        //sessionIdTextView.setText(scanSession.sessionId);
        if (!scanRecords.isEmpty()){
            ScanRecord firstScanRecord = scanRecords.get(0);
            sessionFromToTextView.setText(
                context.getString(R.string.session_from_to_code,
                        firstScanRecord.fromLocationCode,
                        firstScanRecord.toLocationCode));
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        try {
            Date date = dateFormat.parse(scanSession.sessionCreationDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault());
            assert date != null;
            sessionDateTextView.setText(outputFormat.format(date));
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date: " + e.getMessage());
        }
        // Set the session type
        sessionTypeTextView.setText(scanSession.sessionType);
        // Set the session scan count
        scanCountTextView.setText(context.getString(R.string.scan_count, scanRecords.size()));
        scanRecordAdapter.submitList(scanRecords);
        boolean isFailed = false;
        boolean isSaved = false;
        for (ScanRecord scanRecord : scanRecords) {
            if (scanRecord.saveType == 0) {
                isFailed = true;
            } else if (scanRecord.saveType == 1) {
                isSaved = true;
            }
        }
        if (isFailed) {
            statusIndicatorView.setBackgroundColor(Color.RED);
        } else if (isSaved) {
            statusIndicatorView.setBackgroundColor(Color.BLUE);
        }
        updateUI();
        // On process fails re-enable buttons
        reActivateCardButtonsOnBinding();
    }

    private void reActivateCardButtonsOnBinding() {
        resendButton.setEnabled(true);
        deleteButton.setEnabled(true);
        resendButton.setOnClickListener(v -> {
            if (!isSendingData) {
                isSendingData = true;
                resendScanSession();
            }
        });
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void toggleExpandCollapse() {
        isExpanded = !isExpanded;
        updateUI();
    }

    private void updateUI() {
        if (isExpanded) {
            scansRecyclerView.setVisibility(View.VISIBLE);
            expandCollapseImageView.setImageResource(R.drawable.ic_expand_less);
        } else {
            scansRecyclerView.setVisibility(View.GONE);
            expandCollapseImageView.setImageResource(R.drawable.ic_expand_more);
        }
    }

    private void resendScanSession(){
        //Utils.showToast(context, "Not implemented yet", 0);
        String sessionId = currentScanSession.sessionId;
        String sessionType = currentScanSession.sessionType;
        repository.getAllScanRecordsBySessionId(sessionId,
                scanRecords -> {
                    List<Map<String, Object>> formattedScanData = Utils.formatScanData(scanRecords);
                    Log.d(TAG, "Formatted Scan Data: " + formattedScanData);
                    if (Objects.equals(sessionType, Constants.SCAN_SESSION_TRANSFER)) {
                        resendScanActivity(formattedScanData, sessionId, scanRecords, sessionType);
                    }
                });
    }

    public void resendScanActivity(List<Map<String, Object>> scanDataToSend,String sessionId,
                                   List<ScanRecord> scanRecords, String sessionType){
        if (scanDataToSend.isEmpty()) return;
        int total = scanDataToSend.size();
        progressBar.setMax(total);
        progressBar.setProgress(0);
        progressText.setText(R.string.progress_init + total);

        List<Map<String, Object>> failedRecords = new ArrayList<>();
        List<ScanRecord> recordsToDelete = new ArrayList<>();

        CompletableFuture<Void> deletionCompleteFuture = new CompletableFuture<>();

        executor.execute(() -> {
            try (Connection connection = SqlServerConHandler.establishSqlServCon()) {
                if (connection == null) {
                    throw new SQLException("Connection to SQL Server failed");
                }

                handler.post(() -> {
                    showProgressBar(true);
                    disableUserInteraction();
                });

                // Start transaction for the session
                connection.setAutoCommit(false);

                // Step 1: Insert into Scan_Session
                String sessionQuery = SqlQueryUtils.INSERT_SCAN_SESSION;

                try (PreparedStatement sessionStatement = connection.prepareStatement(sessionQuery)) {
                    if (!scanRecords.isEmpty()) {
                        ScanRecord firstScanRecord = scanRecords.get(0);
                        sessionStatement.setString(1, firstScanRecord.sessionId.trim());
                        sessionStatement.setString(2, sessionType);
                        sessionStatement.setString(3, firstScanRecord.fromLocationId.trim());
                        sessionStatement.setString(4, firstScanRecord.toLocationId.trim());
                        String Session_CreationDate = currentScanSession.sessionCreationDate;
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss",
                                Locale.getDefault());
                        Date date = dateFormat.parse(Session_CreationDate);
                        assert date != null;
                        sessionStatement.setTimestamp(5, new Timestamp(date.getTime()));
                    }
                    sessionStatement.executeUpdate();
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e; // Re-throw the exception to be handled later
                } catch (ParseException | java.text.ParseException e) {
                    throw new RuntimeException(e);
                } finally {
                    connection.setAutoCommit(true);
                }

                // Step 2: Insert into Scan_Reading (individual transactions)
                String readingQuery = SqlQueryUtils.INSERT_SCAN_READING;

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
                        // Parsing date to dateTime (sql server format)
                        Utils.parseDateSqlServerFormat(data, readingStatement);
                        readingStatement.setString(5, (String) data.get("deviceSerialNumber"));
                        readingStatement.setString(6, (String) data.get("fromLocationId"));
                        readingStatement.setString(7, (String) data.get("toLocationId"));
                        readingStatement.setString(8, (String) data.get("sessionId"));
                        readingStatement.executeUpdate();
                        Log.d(TAG, "Scan Data sent to SQL Server " + data);
                        // Commit the transaction for this scan
                        connection.commit();
                        // Add the record to the list for deletion later
                        ScanRecord scanRecord = repository.getScanRecordByScannedData(
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
                    String progressLabel = context.getString(R.string.progress);
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
                    String updateSessionQuery = SqlQueryUtils.UPDATE_SCAN_SESSION;
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
                    repository.deleteScanRecord(record);
                }
                // Complete the future after all deletions are done
                deletionCompleteFuture.complete(null);
                // On success, update the UI
                int finalSuccessCount = successCount;
                handler.post(() -> {
                    OnProcessComplete();
                    if (finalSuccessCount == total) {
                        String message = String.format(
                                context.getString(R.string.scan_resubmitted_success_msg),
                                finalSuccessCount);
                        showToast(context, message, 1);
                    } else {
                        showSummary(finalSuccessCount, failedRecords.size(), total, activity);
                    }
                    adapter.remove(currentScanSession);
                    // Check if the list is now empty and update the empty state
                    if (adapter.getCurrentList().isEmpty()) {
                        ((FailedOrSavedScanActivity) context).updateEmptyState();
                    }
                });
            } catch (SQLException | java.sql.SQLException e) {
                handler.post(() -> {
                    OnProcessComplete();
                    showSummary(0, scanDataToSend.size(), total, activity);
                    showToast(context, "Error: " + e.getMessage(), 0);
                    adapter.notifyItemChanged(getAdapterPosition());
                });
                Log.e(TAG, "Error sending data: " + e.getMessage());
            } finally {
                handler.post(() -> isSendingData = false);
            }
        });
        deletionCompleteFuture.thenAccept(v -> deleteScanSessionsWithoutRecords());
    }
    private void OnProcessComplete() {
        showProgressBar(false);
        enableUserInteraction();
        resendButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }
    private void showProgressBar(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressText.setVisibility(show ? View.VISIBLE : View.GONE);
        resendButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    private void showSummary(int successCount, int failedCount, int total,
                             AppCompatActivity activity) {
        String title;
        String message;
        if (failedCount == 0) {
            title = "Send Session Successful";
            message = "✅ Sent " + successCount + " of " + total + " scans";
        } else {
            title = "Send Session Failed";
            message = "❌ Sent " + successCount + " of " + total + " scans, " + failedCount + " failed";
        }
        DialogUtils.showInvalidSelectionDialog(
                activity,
                title,
                message,
                "OK",
                (dialog, which) -> dialog.dismiss(),
                null,
                null
        );
    }
    private void enableUserInteraction() {
        if (failedOrSavedScanActivity != null) {
            failedOrSavedScanActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
    private void deleteScanSessionsWithoutRecords() {
        executor.execute(() -> {
            List<ScanSession> sessionsToDelete = repository.getScanSessionsWithoutRecords();
            for (ScanSession session : sessionsToDelete) {
                Log.d(TAG, "Deleting session with ID: " + session.sessionId);
                repository.deleteSessionWithScanBySessionId(session.sessionId);
            }
        });
    }
    private void disableUserInteraction() {
        // disable buttons
        resendButton.setEnabled(false);
        deleteButton.setEnabled(false);
        if (failedOrSavedScanActivity != null) {
            failedOrSavedScanActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
    private void showDeleteConfirmationDialog() {
        DialogUtils.showInvalidSelectionDialog(
            activity,
            "Delete Scan Session",
            "Are you sure you want to delete this session and all its records?",
            "Yes",
            (dialog, which) -> deleteScanSession(),
            "No",
            (dialog, which) -> dialog.dismiss()
        );
    }
    private void deleteScanSession() {
        repository.deleteScanSessionWithRecordsCallBack(currentScanSession, () ->
                ((FailedOrSavedScanActivity) itemView.getContext()).runOnUiThread(() -> {
        adapter.remove(currentScanSession);
        ((FailedOrSavedScanActivity) itemView.getContext()).updateEmptyState();
    }));
    }

}