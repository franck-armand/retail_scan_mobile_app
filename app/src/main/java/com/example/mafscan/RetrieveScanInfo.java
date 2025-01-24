package com.example.mafscan;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RetrieveScanInfo extends AppCompatActivity {
    private final String TAG = getClass().getName();
    private TableLayout tableLayout;
    private TextView hintMessage;
    private FloatingActionButton clearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retieve_scan_info);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.scanSearchToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("QR Code Information");
        }

        // Initialize views
        tableLayout = findViewById(R.id.qr_table_layout);
        hintMessage = findViewById(R.id.qr_table_label);
        clearButton = findViewById(R.id.clear_scan_floating_action_Btn);

        // Initialize scanner
        if (DatalogicUtils.initializeScanner(this)) {
            DatalogicUtils.setScanListener((scannedData, codeType) -> {
                runOnUiThread(() -> {
                    if (scannedData != null && !scannedData.isEmpty()) {
                        Log.d(TAG, "Scanned Data: " + scannedData + ", Code Type: " + codeType);
                        parseAndDisplayData(scannedData);
                    } else {
                        Toast.makeText(this, "Empty Scan", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        // Set up clear button
        clearButton.setOnClickListener(v -> {
            clearTable();
            showHintMessage(true);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        DatalogicUtils.stopScanning();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatalogicUtils.releaseScanner();
    }

    private void parseAndDisplayData(String data) {
        if (data == null || data.isEmpty()) {
            showHintMessage(true);
            return;
        }

        // Hide hint message
        hideHintMessage();

        // Split the data into key-value pairs
        String[] pairs = data.split("\\|");
        for (String pair : pairs) {
            if (pair.startsWith("(") && pair.contains(")")) {
                int keyEndIndex = pair.indexOf(')');
                String key = pair.substring(1, keyEndIndex);
                String value = pair.substring(keyEndIndex + 1);
                // Add the key-value pair to the table
                addTableRow(key, value);
            } else {
                addTableRow("Unknown Structure", pair);
            }
        }
    }

    private void addTableRow(String key, String value) {
        // Creating a new row
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        // Create the key TextView
        TextView keyTextView = new TextView(this);
        keyTextView.setText(key);
        // bold the text
        keyTextView.setTypeface(null, android.graphics.Typeface.BOLD);
        keyTextView.setGravity(Gravity.START);
        keyTextView.setPadding(8, 8, 8, 8);
        keyTextView.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
        ));

        // Create the value TextView
        TextView valueTextView = new TextView(this);
        valueTextView.setText(value);
        valueTextView.setGravity(Gravity.START);
        valueTextView.setPadding(8, 8, 8, 8);
        valueTextView.setLayoutParams(new TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
        ));

        // Add TextViews to the row
        tableRow.addView(keyTextView);
        tableRow.addView(valueTextView);

        // Add the row to the table
        tableLayout.addView(tableRow);

        // Add a divider
        View divider = new View(this);
        TableLayout.LayoutParams dividerLayoutParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, 2 );
        dividerLayoutParams.setMargins(0, 8, 0, 8);
        divider.setLayoutParams(dividerLayoutParams);
        divider.setBackgroundColor(getResources().getColor(R.color.black, getTheme()));
        tableLayout.addView(divider);

        // Update hint message
        showHintMessage(false);
    }

    private void showHintMessage(boolean isTableEmpty) {
        if (isTableEmpty) {
            hintMessage.setText(R.string.scan_hint_message);
            clearButton.setVisibility(View.GONE);
        } else {
            hintMessage.setText(R.string.scanned_hint_message);
            clearButton.setVisibility(View.VISIBLE);
        }
        hintMessage.setVisibility(isTableEmpty ? View.VISIBLE : View.GONE);
    }

    private void hideHintMessage() {
        hintMessage.setVisibility(View.GONE);
        clearButton.setVisibility(View.VISIBLE);
    }

    private void clearTable() {
        tableLayout.removeAllViews();
        showHintMessage(true);
    }

    private String getCurrentUTCTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }
}
