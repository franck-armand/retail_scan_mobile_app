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
import android.widget.ImageButton;
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

public class ScanFromToActivity extends AppCompatActivity implements
        TabLayout.OnTabSelectedListener, DataLogicUtils.ScanListener {
    private String fromLocationId;
    private String fromLocationCode;
    private String toLocationId;
    private String toLocationCode;
    private Spinner fromSpinner, toSpinner;
    private TextView fromDescription, toDescription;
    private TabLayout tabLayout;
    private LinearLayout spinnerLayout, qrCodeLayout;
    private Button validateButton;
    private ImageButton fromDeleteButton, toDeleteButton;
    private TextInputEditText fromQrCodeEditText, toQrCodeEditText;
    private final HashMap<String, Map<String, String>> locationMap = new HashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_from_to);

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
        fromDeleteButton = findViewById(R.id.fromDeleteButton);
        toDeleteButton = findViewById(R.id.toDeleteButton);
        validateButton = findViewById(R.id.validateButton);
        tabLayout = findViewById(R.id.tabLayoutFromTo);
        tabLayout.addOnTabSelectedListener(this);

        // Loading data to spinners
        loadLocations();

        // Handle button click
        fromDeleteButton.setOnClickListener(v -> {
            fromQrCodeEditText.setText("");
            updateDeleteButtonVisibility();
            updateValidateButtonState();
        });
        toDeleteButton.setOnClickListener(v -> {
            toQrCodeEditText.setText("");
            updateDeleteButtonVisibility();
            updateValidateButtonState();
        });
        validateButton.setOnClickListener(v -> {
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
                callInvalidSelectionDialog();
            }
            else {
                // Check if the fromLocationId and toLocationId are the same
                if (Objects.equals(fromLocationId, toLocationId)) {
                    callInvalidSelectionDialog();
                } else {
                    Log.d(TAG, "Start Scanning: From Location: " + fromLocation + "," +
                            " To Location: " + toLocation);
                    // Navigate to scanning activity
                    Intent intent = new Intent(ScanFromToActivity.this, ScanMainActivity.class);
                    intent.putExtra("fromLocation", fromLocation);
                    intent.putExtra("toLocation", toLocation);
                    intent.putExtra("fromLocationId", fromLocationId);
                    intent.putExtra("fromLocationCode", fromLocationCode);
                    intent.putExtra("toLocationId", toLocationId);
                    intent.putExtra("toLocationCode", toLocationCode);
                    startActivity(intent);
                }
            }
        });

        // Select the second tab (Offline Mode) by default
        Objects.requireNonNull(tabLayout.getTabAt(1)).select();
        // Set the initial state of the tab
        onTabSelected(Objects.requireNonNull(tabLayout.getTabAt(1)));

        // Set initial validate button state
        updateDeleteButtonVisibility();
        updateValidateButtonState();
    }

    private void callInvalidSelectionDialog() {
        DialogUtils.showInvalidSelectionDialog(
                this,
                "Invalid Selection",
                "The 'From' and 'To' locations cannot be the same.",
                "Retry",
                (dialog, which) -> dialog.dismiss(),
                null,
                null
        );
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (tab.getPosition() == 0) { // Online Mode
            spinnerLayout.setVisibility(View.VISIBLE);
            qrCodeLayout.setVisibility(View.GONE);
            updateValidateButtonState();
            // Disable triggers for Online Mode
            DataLogicUtils.setTriggersEnabled(false);
        } else if (tab.getPosition() == 1) { // Offline Mode
            spinnerLayout.setVisibility(View.GONE);
            qrCodeLayout.setVisibility(View.VISIBLE);
            // Enable triggers for Offline Mode
            DataLogicUtils.setTriggersEnabled(true);
            clearSetAndDisableField();
            updateValidateButtonState();
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    private void clearSetAndDisableField() {
//        fromQrCodeEditText.setText("");
//        toQrCodeEditText.setText("");
        updateDeleteButtonVisibility();
    }

    private void updateValidateButtonState() {
        String currentFromLocation = "";
        String currentToLocation = "";
        if (spinnerLayout.getVisibility() == View.VISIBLE) {
            if (fromSpinner.getSelectedItem() != null && toSpinner.getSelectedItem() != null) {
                currentFromLocation = fromSpinner.getSelectedItem().toString();
                currentToLocation = toSpinner.getSelectedItem().toString();
            }
            validateButton.setEnabled(!currentFromLocation.isEmpty() && !currentToLocation.isEmpty());

        }
        else if (qrCodeLayout.getVisibility() == View.VISIBLE) {
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
                    Toast.makeText(ScanFromToActivity.this,
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
                updateValidateButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fromDescription.setText("");
                updateValidateButtonState();
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
                updateValidateButtonState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                toDescription.setText("");
                updateValidateButtonState();
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
            updateValidateButtonState();
            updateDeleteButtonVisibility();
        }
    };

    private void updateDeleteButtonVisibility() {
        // Check if the fromQrCodeEditText has text
        if (Objects.requireNonNull(fromQrCodeEditText.getText()).length() > 0) {
            fromDeleteButton.setVisibility(View.VISIBLE);
        } else {
            fromDeleteButton.setVisibility(View.GONE);
        }
        // Check if the toQrCodeEditText has text
        if (Objects.requireNonNull(toQrCodeEditText.getText()).length() > 0) {
            toDeleteButton.setVisibility(View.VISIBLE);
        } else {
            toDeleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onScan(String scannedData, String codeType) {
        Log.d(TAG, "Scanned Data: " + scannedData + ", Code Type: " + codeType);
        // Parse and Update the UI with the scan result
        parseScannedData(scannedData);
    }

    private void parseScannedData(String scannedData) {
        // Format: (00)LOC|(10)0F6B321C-8E4E-41BD-B9B3-06A0B5DC9352|(11)PNT-M1|(12)MALTE
        if(!scannedData.startsWith("(00)LOC")){
            Toast.makeText(this, "Invalid QR Code: Not a location code",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // Parse the scanned data
        Map<String, String> parsedData = new HashMap<>();
        String[] pairs = scannedData.trim().split("\\|");
        for (String pair : pairs) {
            if (pair.startsWith("(") && pair.contains(")")) {
                int keyEndIndex = pair.indexOf(')');
                String key = pair.substring(1, keyEndIndex);
                String value = pair.substring(keyEndIndex + 1);
                parsedData.put(key, value);
            }
        }
        // Extract location details
        String locationId = Objects.requireNonNull(parsedData.get("10")).trim();
        String locationCode = Objects.requireNonNull(parsedData.get("11")).trim();
        String locationName = Objects.requireNonNull(parsedData.get("12")).trim();

        // Which field to update
        if (Objects.requireNonNull(fromQrCodeEditText.getText()).toString().trim().isEmpty()) {
            fromQrCodeEditText.setText(locationName);
            fromLocationId = locationId;
            fromLocationCode = locationCode;
        }
        else if (Objects.requireNonNull(toQrCodeEditText.getText()).toString().trim().isEmpty()) {
            toQrCodeEditText.setText(locationName);
            toLocationId = locationId;
            toLocationCode = locationCode;
        }
        else {
            Toast.makeText(this, "Both 'From' and 'To' fields are full",
                    Toast.LENGTH_LONG).show();
        }
        updateDeleteButtonVisibility();
        updateValidateButtonState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tabLayout.getSelectedTabPosition() == 1) { // Offline Mode
            // Re-initialize the scanner
            Log.d(TAG, "Init Scanner: ");
            DataLogicUtils.initializeScanner(this);
            // Re-initialize the DataLogicUtils
            Log.d(TAG, "Set Listener: ");
            DataLogicUtils.setScanListener(this);
            Log.d(TAG, "Offline mode Scan resumed: ");
            DataLogicUtils.setTriggersEnabled(true);
        } else {
            Log.d(TAG, "Online mode Scan resumed: ");
            DataLogicUtils.setTriggersEnabled(false);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        DataLogicUtils.setTriggersEnabled(false);
    }
}