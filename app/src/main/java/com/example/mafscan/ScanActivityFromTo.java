package com.example.mafscan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanActivityFromTo extends AppCompatActivity implements
        TabLayout.OnTabSelectedListener, DataLogicUtils.ScanListener {
    private String fromLocationId;
    private String fromLocationCode;
    private String toLocationId;
    private String toLocationCode;
    private Spinner fromSpinner, toSpinner;
    private TextView fromDescription, toDescription;
    private LinearLayout spinnerLayout, qrCodeLayout;
    private Button validateButton;
    private TextInputEditText fromQrCodeEditText, toQrCodeEditText;
    private final HashMap<String, Map<String, String>> locationMap = new HashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_0);

        // Initialize the scanner
        DataLogicUtils.initializeScanner(this);

        // Set the scan listener
        DataLogicUtils.setScanListener(this);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_scan0);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            String title = "Provenance" + " → " + "Destination";
            getSupportActionBar().setTitle(title);
        }

        // Initialize views
        fromSpinner = findViewById(R.id.fromSpinner);
        toSpinner = findViewById(R.id.toSpinner);
        fromDescription = findViewById(R.id.fromDescription);
        toDescription = findViewById(R.id.toDescription);
        spinnerLayout = findViewById(R.id.spinnerLayout);
        qrCodeLayout = findViewById(R.id.qrCodeLayout);
        fromQrCodeEditText = findViewById(R.id.fromQrCodeEditText);
        toQrCodeEditText = findViewById(R.id.toQrCodeEditText);
        fromQrCodeEditText.addTextChangedListener(OfflineTextWatcher);
        toQrCodeEditText.addTextChangedListener(OfflineTextWatcher);
        validateButton = findViewById(R.id.validateButton);

        // Loading data to spinners
        loadLocations();

        // Handle button click
        findViewById(R.id.validateButton).setOnClickListener(v -> {
            String fromLocation;
            String toLocation;
            if (spinnerLayout.getVisibility() == View.VISIBLE) {
                fromLocation = fromSpinner.getSelectedItem().toString();
                toLocation = toSpinner.getSelectedItem().toString();
            } else {
                fromLocation = Objects.requireNonNull(fromQrCodeEditText.getText()).toString().trim();
                toLocation = Objects.requireNonNull(toQrCodeEditText.getText()).toString().trim();
            }

            if (fromLocation.equals(toLocation)) {
                DialogUtils.showInvalidSelectionDialog(
                        this,
                        "Invalid Selection",
                        "The 'From' and 'To' locations cannot be the same.",
                        "Retry",
                        (dialog, which) -> dialog.dismiss(),
                        null,
                        null
                );
            } else {
                Log.d(TAG, "Start Scanning: From Location: " + fromLocation + "," +
                        " To Location: " + toLocation);
                // Navigate to scanning activity
                Intent intent = new Intent(ScanActivityFromTo.this, ScanActivityMain.class);
                intent.putExtra("fromLocation", fromLocation);
                intent.putExtra("toLocation", toLocation);
                intent.putExtra("fromLocationId", fromLocationId);
                intent.putExtra("fromLocationCode", fromLocationCode);
                intent.putExtra("toLocationId", toLocationId);
                intent.putExtra("toLocationCode", toLocationCode);
                startActivity(intent);
            }
        });
        // Set up TabLayout
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(this);

        // Set initial validate button state
        updateScanButtonState();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (tab.getPosition() == 0) { // Online Mode
            spinnerLayout.setVisibility(View.VISIBLE);
            qrCodeLayout.setVisibility(View.GONE);
            updateScanButtonState();
            // Disable triggers for Online Mode
            DataLogicUtils.setTriggersEnabled(false);
        } else if (tab.getPosition() == 1) { // Offline Mode
            spinnerLayout.setVisibility(View.GONE);
            qrCodeLayout.setVisibility(View.VISIBLE);
            // Enable triggers for Offline Mode
            DataLogicUtils.setTriggersEnabled(true);
            clearSetAndDisableField();
            updateScanButtonState();
        }
    }
    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    private void clearSetAndDisableField() {
        fromQrCodeEditText.setText("");
        toQrCodeEditText.setText("");
        fromQrCodeEditText.setEnabled(false);
        toQrCodeEditText.setEnabled(false);
    }

    private void updateScanButtonState() {
        String currentFromLocation = "";
        String currentToLocation = "";
        if (spinnerLayout.getVisibility() == View.VISIBLE) {
            if (fromSpinner.getSelectedItem() != null && toSpinner.getSelectedItem() != null) {
                currentFromLocation = fromSpinner.getSelectedItem().toString();
                currentToLocation = toSpinner.getSelectedItem().toString();
            }
            validateButton.setEnabled(!currentFromLocation.isEmpty() && !currentToLocation.isEmpty());

        } else if (qrCodeLayout.getVisibility() == View.VISIBLE) {
            if (fromQrCodeEditText.getText() != null && toQrCodeEditText.getText() != null) {
                currentFromLocation = Objects.requireNonNull(fromQrCodeEditText.getText()).toString().trim();
                currentToLocation = Objects.requireNonNull(toQrCodeEditText.getText()).toString().trim();
            }
            validateButton.setEnabled(!currentFromLocation.isEmpty() && !currentToLocation.isEmpty());
        }
    }

    private void loadLocations() {
        executor.execute(() -> {
            try (Connection connection = SqlServerConHandler.establishSqlServCon()) {
                if (connection == null) {
                    throw new SQLException("Connection to SQL Server failed.");
                }

                String query = "SELECT Loc_Id, Loc_Code, Loc_Name, Loc_Description FROM Scan_Location";
                try (PreparedStatement statement = connection.prepareStatement(query);
                     ResultSet resultSet = statement.executeQuery()) {

                    List<String> locations = new ArrayList<>();
                    while (resultSet.next()) {
                        String locationName = resultSet.getString("Loc_Name");
                        String locationId = resultSet.getString("Loc_Id");
                        String locationCode = resultSet.getString("Loc_Code");
                        String locationDescription = resultSet.getString("Loc_Description");
                        locations.add(locationName);
                        //locationMap.put(locationName, locationDescription);
                        Map<String, String> locationDetails = new HashMap<>();
                        locationDetails.put("Loc_Id", locationId);
                        locationDetails.put("Loc_Code", locationCode);
                        locationDetails.put("Loc_Description", locationDescription);

                        locationMap.put(locationName, locationDetails);
                    }
                    // Post results to the main thread
                    handler.post(() -> populateSpinners(locations));
                    Log.d(TAG, "From/To locations loaded.");
                }
            } catch (SQLException e) {
                // Post error message to the main thread
                handler.post(() -> {
                    Toast.makeText(ScanActivityFromTo.this,
                            "Error loading locations: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
                Log.e(TAG, "Error loading locations: " + e.getMessage());
            }
        });
    }

    private void populateSpinners(List<String> locations) {
        // Empty item to the beginning of the list
        locations.add(0, "");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // set up "From" Spinner
        fromSpinner.setAdapter(adapter);
        fromSpinner.setSelection(0, false); // initial empty position is selected
        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLocation = locations.get(position);
                if (!selectedLocation.isEmpty()) {
                    Map<String, String> locationDetails = locationMap.get(selectedLocation);
                    fromDescription.setText(locationDetails.get("Loc_Description"));
                    fromLocationId = locationDetails.get("Loc_Id");
                    fromLocationCode = locationDetails.get("Loc_Code");
                } else {
                    fromDescription.setText("");
                    fromLocationId = null;
                    fromLocationCode = null;
                }
                updateScanButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fromDescription.setText("");
                updateScanButtonState();
            }
        });

        // set up "To" Spinner
        toSpinner.setAdapter(adapter);
        toSpinner.setSelection(0, false); // initial empty position is selected
        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLocation = locations.get(position);
                if (!selectedLocation.isEmpty()) {
                    Map<String, String> locationDetails = locationMap.get(selectedLocation);
                    toDescription.setText(locationDetails.get("Loc_Description"));
                    toLocationId = locationDetails.get("Loc_Id");
                    toLocationCode = locationDetails.get("Loc_Code");
                } else {
                    toDescription.setText("");
                    toLocationId = null;
                    toLocationCode = null;
                }
                updateScanButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                toDescription.setText("");
                updateScanButtonState();
            }
        });
    }

    private final TextWatcher OfflineTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not needed yet
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Not needed yet
        }
        @Override
        public void afterTextChanged(Editable s) {
            updateScanButtonState();
        }
    };

    @Override
    public void onScan(String scannedData, String codeType) {
        Log.d(TAG, "Scanned Data: " + scannedData + ", Code Type: " + codeType);
        // Parse and Update the UI with the scan result
        parseScannedData(scannedData);
    }

    private void parseScannedData(String scannedData) {
        // TODO : Place holder to format the scan result accordingly later
        // Split the data into key-value pairs
        String[] pairs = scannedData.trim().split("\\|");
        fromQrCodeEditText.setText(scannedData.trim());
        toQrCodeEditText.setText(scannedData.trim());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataLogicUtils.setTriggersEnabled(qrCodeLayout.getVisibility() == View.VISIBLE);
    }
}