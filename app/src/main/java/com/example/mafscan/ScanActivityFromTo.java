package com.example.mafscan;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ScanActivityFromTo extends AppCompatActivity {
    private Spinner fromSpinner, toSpinner;
    private TextView fromDescription, toDescription;
    private HashMap<String, String> locationMap = new HashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final String TAG = getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_0);

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

        // Loading data to spinners
        loadLocations();

        // Handle button click
        findViewById(R.id.validateButton).setOnClickListener(v -> {
            String fromlocation = fromSpinner.getSelectedItem().toString();
            String tolocation = toSpinner.getSelectedItem().toString();

            if (fromlocation.equals(tolocation))
            {
                DialogUtils.showInvalideSelectionDialog(
                        this,
                        "Invalid Selection",
                        "The 'From' and 'To' locations cannot be the same.",
                        "Retry",
                        (dialog, which) -> dialog.dismiss(),
                        null,
                        null
                );
            }else {
                Log.d(TAG, "Start Scanning: From Location: " + fromlocation + "," +
                        " To Location: " + tolocation);
                // Navigate to scanning activity
                Intent intent = new Intent(ScanActivityFromTo.this, ScanActivityMain.class);
                intent.putExtra("fromLocation", fromlocation);
                intent.putExtra("toLocation", tolocation);
                startActivity(intent);
            }
        });
    }

    private void loadLocations() {
        executor.execute(() -> {
            try (Connection connection = SqlServerConHandler.establishSqlServCon()) {
                if (connection == null) {
                    throw new SQLException("Connection to SQL Server failed.");
                }

                String query = "SELECT Loc_Name, Loc_Description FROM Scan_Location";
                try (PreparedStatement statement = connection.prepareStatement(query);
                     ResultSet resultSet = statement.executeQuery()) {

                    List<String> locations = new ArrayList<>();
                    while (resultSet.next()) {
                        String locationName = resultSet.getString("Loc_Name");
                        String locationDescription = resultSet.getString("Loc_Description");
                        locations.add(locationName);
                        locationMap.put(locationName, locationDescription);
                    }

                    // Post results to the main thread
                    handler.post(() -> populateSpinners(locations));
                }
            } catch (SQLException e) {
                // Post error message to the main thread
                handler.post(() -> {
                    Toast.makeText(ScanActivityFromTo.this,
                            "Error loading locations: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
                e.printStackTrace();
            }
        });
    }

    private void populateSpinners(List<String> locations) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // set up "From" Spinner
        fromSpinner.setAdapter(adapter);
        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLocation = locations.get(position);
                fromDescription.setText(locationMap.get(selectedLocation));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fromDescription.setText("");
            }
        });

        // set up "To" Spinner
        toSpinner.setAdapter(adapter);
        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLocation = locations.get(position);
                toDescription.setText(locationMap.get(selectedLocation));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                toDescription.setText("");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}

