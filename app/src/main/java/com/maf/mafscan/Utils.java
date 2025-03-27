package com.maf.mafscan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.content.ContextCompat;
import androidx.core.net.ParseException;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();
    public static class SwipeToDeleteCallback extends
            ItemTouchHelper.SimpleCallback {

        public interface SwipeToDeleteListener {
            void onDelete(int position);
        }
        public interface SwipeToUpdateListener {
            void onUpdate(int position);
        }

        private final SwipeToDeleteListener mDeleteListener;
        private final SwipeToUpdateListener mUpdateListener;
        private final Drawable iconDelete;
        private final Drawable iconUpdate;
        private final ColorDrawable backgroundDelete;
        private final ColorDrawable backgroundUpdate;

        public SwipeToDeleteCallback(Context context,
                                     SwipeToDeleteListener deleteListener,
                                     SwipeToUpdateListener updateListener) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            mDeleteListener = deleteListener;
            mUpdateListener = updateListener;
            iconDelete = ContextCompat.getDrawable(context, R.drawable.outline_delete_forever_24_w);
            iconUpdate = ContextCompat.getDrawable(context, R.drawable.baseline_edit_note_24);
            backgroundDelete = new ColorDrawable(Color.RED);
            backgroundUpdate = new ColorDrawable(Color.BLUE);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false; // We don't support drag and drop
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            if(direction == ItemTouchHelper.LEFT) {
                mDeleteListener.onDelete(position);
            } else if(direction == ItemTouchHelper.RIGHT) {
                mUpdateListener.onUpdate(position);
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20; // So background is behind the rounded corners of itemView

            int iconMargin = (itemView.getHeight() - iconDelete.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - iconDelete.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + iconDelete.getIntrinsicHeight();

            if (dX > 0) { // Swiping to the right
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = itemView.getLeft() + iconMargin + iconUpdate.getIntrinsicWidth();
                iconUpdate.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                backgroundUpdate.setBounds(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                        itemView.getBottom());
                backgroundUpdate.draw(c);
                iconUpdate.draw(c);

            } else if (dX < 0) { // Swiping to the left
                int iconLeft = itemView.getRight() - iconMargin - iconDelete.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                iconDelete.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                backgroundDelete.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                        itemView.getTop(), itemView.getRight(), itemView.getBottom());
                backgroundDelete.draw(c);
                iconDelete.draw(c);

            } else { // view is unSwiped
                backgroundDelete.setBounds(0, 0, 0, 0);
                backgroundUpdate.setBounds(0, 0, 0, 0);
            }
        }
    }
    public static String getAppVersion(Context context) {
        try {

            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Error getting app version", e);
            return "Unknown";
        }
    }

    public static List<Map<String, Object>> formatScanData(List<?> dataList, String fromLocationId,
                                                           String toLocationId,
                                                           String sessionId) {
        List<Map<String, Object>> formattedData = new ArrayList<>();
        for (Object item : dataList) {
            Map<String, Object> data = new HashMap<>();
            if (item instanceof ScanData) {
                ScanData scanData = (ScanData) item;
                data.put(Constants.SCANNED_DATA, scanData.getScannedData());
                data.put(Constants.CODE_TYPE, scanData.getCodeType());
                data.put(Constants.SCAN_COUNT, scanData.getScanCount());
                data.put(Constants.SCAN_DATE, scanData.getFormattedScanDate());
                data.put(Constants.DEVICE_SERIAL_NUMBER, DataLogicUtils.getDeviceInfo());
                if (fromLocationId != null) {
                    data.put(Constants.FROM_LOCATION_ID, fromLocationId);
                }
                if (toLocationId != null) {
                    data.put(Constants.TO_LOCATION_ID, toLocationId);
                }
                if (sessionId != null) {
                    data.put(Constants.SCAN_SESSION_ID, sessionId);
                }
            } else if (item instanceof ScanRecord) {
                ScanRecord scanRecord = (ScanRecord) item;
                data.put(Constants.SCAN_SESSION_ID, scanRecord.sessionId);
                data.put(Constants.SCANNED_DATA, scanRecord.scannedData);
                data.put(Constants.SCAN_COUNT, scanRecord.scanCount);
                data.put(Constants.FROM_LOCATION_ID, scanRecord.fromLocationId);
                data.put(Constants.TO_LOCATION_ID, scanRecord.toLocationId);
                data.put(Constants.SCAN_DATE, scanRecord.scanDate);
                data.put(Constants.CODE_TYPE, scanRecord.codeType);
                data.put(Constants.DEVICE_SERIAL_NUMBER, DataLogicUtils.getDeviceInfo());
            }
            formattedData.add(data);
            Log.d(TAG, "Formatted Scan Data to be sent: " + formattedData);
        }
        return formattedData;
    }
    // Overload the method for cases where fromLocationId, toLocationId, and sessionId are not available
    public static List<Map<String, Object>> formatScanData(List<?> dataList) {
        return formatScanData(dataList, null, null, null);
    }

    public static boolean executeScanDataDatabaseOperations(
            List<Map<String, Object>> scanDataToSend,
            String sessionId,
            String sessionType,
            String fromLocationId,
            String toLocationId,
            ScanRecordDao scanRecordDao,
            Connection connection
    ) throws SQLException, ParseException, java.sql.SQLException {
        if (scanDataToSend.isEmpty()) {
            return true;
        }

        List<Map<String, Object>> failedRecords = new ArrayList<>();
        List<ScanRecord> recordsToDelete = new ArrayList<>();
        boolean overallSuccess = true; // Initialize overall success

        if (connection == null) {
            throw new SQLException("Connection is null. Please provide a valid database connection.");
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
            sessionStatement.setString(5, Utils.getCurrentUtcDateTimeString());
            sessionStatement.executeUpdate();
            connection.commit();
        } catch (SQLException | ParseException e) {
            connection.rollback();
            overallSuccess = false; // Set overall success to false on failure
            throw e;
        }

        // Step 2: Insert into Scan_Reading (individual transactions)
        String readingQuery = SqlQueryUtils.INSERT_SCAN_READING;
        for (int i = 0; i < scanDataToSend.size(); i++) {
            Map<String, Object> data = scanDataToSend.get(i);
            // Start a new transaction for each scan
            connection.setAutoCommit(false);
            try (PreparedStatement readingStatement = connection.prepareStatement(readingQuery)) {
                readingStatement.setString(1, (String) data.get(Constants.SCANNED_DATA));
                readingStatement.setString(2, (String) data.get(Constants.CODE_TYPE));
                Float scanCount = (Float) data.get(Constants.SCAN_COUNT);
                if (scanCount == null) {
                    scanCount = 1.0f;
                    Log.w(TAG, "scanCount is null or not a Float");
                }
                readingStatement.setFloat(3, scanCount);
                Utils.parseDateSqlServerFormat(data, readingStatement);
                readingStatement.setString(5, (String) data.get(Constants.DEVICE_SERIAL_NUMBER));
                readingStatement.setString(6, (String) data.get(Constants.FROM_LOCATION_ID));
                readingStatement.setString(7, (String) data.get(Constants.TO_LOCATION_ID));
                readingStatement.setString(8, (String) data.get(Constants.SCAN_SESSION_ID));
                readingStatement.executeUpdate();
                Log.d(TAG, "Scan Data sent to SQL Server " + data);

                // Commit the transaction for this scan
                connection.commit();

                // Add the record to the list for deletion later
                ScanRecord scanRecord = scanRecordDao.getScanRecordByScannedData(
                        (String) data.get(Constants.SCANNED_DATA), sessionId);
                if (scanRecord != null) {
                    recordsToDelete.add(scanRecord);
                }

            } catch (SQLException e) {
                // Rollback the transaction for this scan
                connection.rollback();
                failedRecords.add(data);
            } finally {
                // Reset auto-commit for the next scan
                connection.setAutoCommit(true);
            }
        }

        // Step 3: Update Scan_Session
        try {
            connection.setAutoCommit(false);
            String updateSessionQuery = SqlQueryUtils.UPDATE_SCAN_SESSION;
            try (PreparedStatement updateSessionStatement = connection.prepareStatement(
                    updateSessionQuery)) {
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

        // Check if any records failed
        if (!failedRecords.isEmpty()) {
            overallSuccess = false;
        }

        return overallSuccess; // Return the overall success status
    }

    public static void updateProgressBar(
            int progress,
            int total,
            ProgressBar progressBar,
            TextView progressText,
            Handler handler,
            Context context
    ) {
        String progressLabel = context.getString(R.string.progress);
        String formattedProgress = progressLabel + " " + progress + "/" + total;
        handler.post(() -> {
            progressBar.setProgress(progress);
            progressText.setText(formattedProgress);
        });
    }

    public static void parseDateSqlServerFormat(Map<String, Object> data, PreparedStatement statement)
            throws java.sql.SQLException {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_LONG,
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

    public static void showToast(Context context, String message, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    @SuppressLint("RestrictedApi")
    public static void inflateMenu(AppCompatActivity activity, Menu menu, int menuResId) {
        MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(menuResId, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder menuBuilder = (MenuBuilder) menu;
            menuBuilder.setOptionalIconsVisible(true);
        }
    }

    public static void showHelpDialog(FragmentManager fragmentManager) {
        HelpDialogFragment dialog = new HelpDialogFragment();
        dialog.show(fragmentManager, "ScanCodeHelpDialogFragment");
    }

    public static String getCurrentUtcDateTimeString() {
        DateTime utcDateTime = DateTime.now(DateTimeZone.UTC);
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        return fmt.print(utcDateTime);
    }
}

