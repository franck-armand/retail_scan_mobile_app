package com.maf.mafscan;

import static com.maf.mafscan.Utils.getCurrentUtcDateTimeString;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ShuttleServerManager {
    private final String TAG = getClass().getSimpleName();
    private final Context context;
    private final Executor executor;
    private final Handler handler;
    private final ScanSessionDao scanSessionDao;
    private String sessionId;
    private String sessionType;
    private String fromLocationId;
    private String toLocationId;

    public ShuttleServerManager(Context context, Executor executor, ScanSessionDao scanSessionDao) {
        this.context = context;
        this.executor = executor;
        this.scanSessionDao = scanSessionDao;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public void setFromLocationId(String fromLocationId) {
        this.fromLocationId = fromLocationId;
    }

    public void setToLocationId(String toLocationId) {
        this.toLocationId = toLocationId;
    }

    public void sendShuttleDataToServer() {
        createNewScanSession().thenAccept(
                isSaveSuccessful -> {
                    // Establish database connection and send data
                    sendEmptyShuttleRequest();
                }).exceptionally(e -> {
            Log.e(TAG, "sendShuttleData, error creating scan session: " + e.getMessage());
            handler.post(() -> showShuttleSessionSummary(
                    "Send Session Failed",
                    "❌ Failed to send shuttle session, Contact Administrator"));
            return null;
        });
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

                Log.d(TAG, "Scan session created with ID: " + sessionId);
                future.complete(sessionId); // Complete the future with the sessionId
            } catch (Exception e) {
                Log.e(TAG, "Error creating scan session: " + e.getMessage());
                future.completeExceptionally(e); // Complete the future exceptionally
            }
        });
        return future;
    }

    private void sendEmptyShuttleRequest() {
        executor.execute(() -> {
            try (Connection connection = SqlServerConHandler.establishSqlServCon()) {
                if (connection == null) {
                    throw new SQLException("Connection to SQL Server failed");
                }

                // Start transaction for the session
                connection.setAutoCommit(false);
                // Step 1: Insert into Scan_Session
                String sessionQuery = SqlQueryUtils.INSERT_SCAN_SESSION;

                try (PreparedStatement sessionStatement = connection.prepareStatement(sessionQuery)) {
                    sessionStatement.setString(1, sessionId);
                    sessionStatement.setString(2, sessionType);
                    sessionStatement.setString(3, fromLocationId);
                    sessionStatement.setString(4, toLocationId);
                    sessionStatement.setString(5, getCurrentUtcDateTimeString());
                    sessionStatement.executeUpdate();
                    connection.commit();
                    handler.post(() -> showShuttleSessionSummary(
                            "Empty Successfully",
                            "✅ Shuttle has been emptied"));
                } catch (SQLException e) {
                    connection.rollback();
                    handler.post(() -> showShuttleSessionSummary(
                            "Empty action failed",
                            "❌ Shuttle has not been emptied an error occurred." +
                                    " Contact Administrator"));
                    throw e; // Re-throw the exception to be handled later
                } finally {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                handler.post(() -> showShuttleSessionSummary(
                        "Network Error",
                        "❌ You are currently offline, Shuttle can not be emptied." +
                                " Contact Administrator"));
                Log.e(TAG, "Error sending data: " + e.getMessage());
            }
        });
    }

    private void showShuttleSessionSummary(String title, String message) {
        DialogUtils.showInvalidSelectionDialog(
                (AppCompatActivity) context,
                title,
                message,
                "OK",
                (dialog, which) -> {dialog.dismiss();
                    ((ScanReceptionExpeditionActivity) context).clearShuttleData();
                    },
                null,
                null
        );
    }
}