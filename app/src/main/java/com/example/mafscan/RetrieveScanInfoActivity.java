package com.example.mafscan;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RetrieveScanInfoActivity extends AppCompatActivity {
    private final String TAG = getClass().getName();
    private TableLayout tableLayout;
    private TextView scanCount;
    private int count = 0;
    private final List<String> scanDataList = new ArrayList<>();
    private static final String SCAN_DATA_KEY = "scan_data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retieve_scan_info);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.retrieveScanInfoToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Initialize views
        tableLayout = findViewById(R.id.qr_table_layout);
        FloatingActionButton clearButton = findViewById(R.id.clear_scan_floating_action_Btn);
        scanCount = findViewById(R.id.qr_item_count);

        // Restore the saved data on resume or pause if present
        if (savedInstanceState != null) {
            Log.d(TAG, "savedInstanceState is NOT null");
            ArrayList<String> savedData = savedInstanceState.getStringArrayList(SCAN_DATA_KEY);
            if (savedData != null) {
//                 Reconstruct the scanDataList from the saved data
                scanDataList.clear();
                scanDataList.addAll(savedData);
//                 Re-add the data to the table
                for (String data : scanDataList) {
                    String[] pairs = data.trim().split("\\|");
                    addScanData(pairs);
                }
            }
        }
        else {
            Log.d(TAG, "savedInstanceState is null");
        }

        // Enable triggers
        DataLogicUtils.setTriggersEnabled(true);
        // Initialize scanner
        if (DataLogicUtils.initializeScanner(this)) {
            DataLogicUtils.setScanListener((scannedData, codeType) -> {
                runOnUiThread(() -> {
                    if (scannedData != null && !scannedData.isEmpty()) {
                        Log.d(TAG, "Scanned Data: " + scannedData + ", Code Type: " + codeType);
                        parseAndDisplayData(scannedData);
                        incrementCounter();
                    } else {
                        Toast.makeText(this, "Empty Scan", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        // Set up clear button
        clearButton.setOnClickListener(v -> {
            clearTable();
            resetCounter();
        });
    }

    // Google has blocked showing the icon on the toolbar, just a work around
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.inflateMenu(this, menu, R.menu.retrieve_scan_info_menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_Help) {
            Utils.showHelpDialog(getSupportFragmentManager());
            return true;
        } else if (id == R.id.action_scan_session) {
            Intent intent = new Intent(this, ScanFromToActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_failed_saved) {
            Intent intent = new Intent(this, FailedOrSavedScanActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }
    @Override
    protected void onPause() {
        super.onPause();
        DataLogicUtils.stopScanning();
        DataLogicUtils.setTriggersEnabled(false);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataLogicUtils.releaseScanner();
    }
    @Override
    protected void onResume() {
        super.onResume();
        DataLogicUtils.setTriggersEnabled(true);
    }
    private void resetCounter() {
        count = 0;
        scanCount.setText(String.valueOf(count));
    }
    private void incrementCounter() {
        count++;
        scanCount.setText(String.valueOf(count));
    }
    private void parseAndDisplayData(String data) {
        // Split the data into key-value pairs
        String[] pairs = data.trim().split("\\|");
        // Adding the split data to the table
        addScanData(pairs);
        // Add the data to the list to save state
        scanDataList.add(data);
    }
    private void addScanData(String[] pairs){
        // Add a timestamp row first
        TableRow timestampRow = createTimestampRow();
        tableLayout.addView(timestampRow, 0);
        // Add data rows
        for (String pair : pairs) {
            if (pair.startsWith("(") && pair.contains(")")) {
                int keyEndIndex = pair.indexOf(')');
                String key = pair.substring(1, keyEndIndex);
                String value = pair.substring(keyEndIndex + 1);

                TableRow dataRow = createDataRow(key, value);
                tableLayout.addView(dataRow, 1); // Insert below timestamp
                Log.d(TAG, "Key: " + key + ", Value: " + value);
            } else {
                TableRow dataRow = createDataRow("Unknown Structure", pair);
                tableLayout.addView(dataRow, 1);
            }
        }
        // Add a divider below the group
        View divider = createDivider();
        tableLayout.addView(divider, pairs.length + 1); // Position after data rows
    }
    private TableRow createTimestampRow() {
        TableRow timestampRow = new TableRow(this);
        timestampRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        TextView timestampTextView = new TextView(this);
        timestampTextView.setText(getCurrentUTCTime());
        timestampTextView.setPadding(8, 8, 8, 8);
        timestampTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        timestampTextView.setTextSize(14);
        timestampTextView.setTypeface(null, Typeface.BOLD);

        timestampTextView.setBackgroundColor(getResources().getColor(R.color.light_gray,
                    getTheme()));


        timestampRow.addView(timestampTextView);
        return timestampRow;
    }
    private TableRow createDataRow(String key, String value) {
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        tableRow.setBackgroundColor(getResources().getColor(R.color.white,
                getTheme()));

        TextView keyTextView = new TextView(this);
        keyTextView.setText(key);
        keyTextView.setTypeface(null, Typeface.BOLD);
        keyTextView.setPadding(8, 8, 8, 8);
        keyTextView.setGravity(Gravity.START);
        keyTextView.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                0.2f
        ));

        TextView valueTextView = new TextView(this);
        valueTextView.setText(value);
        valueTextView.setPadding(8, 8, 8, 8);
        valueTextView.setGravity(Gravity.START);
        valueTextView.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                0.8f
        ));

        tableRow.addView(keyTextView);
        tableRow.addView(valueTextView);
        return tableRow;
    }
    private View createDivider() {
        View divider = new View(this);
        TableLayout.LayoutParams dividerLayoutParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, 2);
        dividerLayoutParams.setMargins(0, 8, 0, 8);
        divider.setLayoutParams(dividerLayoutParams);
        divider.setBackgroundColor(getResources().getColor(R.color.black, getTheme()));
        return divider;
    }
    private void clearTable() {
        tableLayout.removeAllViews();
        scanDataList.clear();
    }
    private String getCurrentUTCTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        //dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the scan data
        outState.putStringArrayList(SCAN_DATA_KEY, new ArrayList<>(scanDataList));
//        outState.putStringArrayList(SCAN_DATA_KEY, new ArrayList<>());

    }

}
