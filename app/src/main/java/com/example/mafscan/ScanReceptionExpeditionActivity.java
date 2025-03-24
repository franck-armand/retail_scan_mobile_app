package com.example.mafscan;

import static com.example.mafscan.Utils.showToast;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ScanReceptionExpeditionActivity extends AppCompatActivity implements
        TabLayout.OnTabSelectedListener, DataLogicUtils.ScanListener {
    private final ReceptionData receptionData = new ReceptionData();
    private final ExpeditionData expeditionData = new ExpeditionData();
    private TabLayout tabLayoutRecExp;
    private LinearLayout receptionLayout, expeditionLayout;
    private Button validateButton;
    private ImageButton expFromDeleteButton, recToDeleteButton;
    private TextInputEditText expFromQrCodeEditText, recToQrCodeEditText;
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expedition_reception);

        // Initialize the scanner
        DataLogicUtils.initializeScanner(this);

        // Set the scan listener
        DataLogicUtils.setScanListener(this);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_rec_exp);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            String title = getString(R.string.reception_expedition_activity_title);
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        initialization();

        // Handle button click
        expFromDeleteButton.setOnClickListener(v -> {
            expFromQrCodeEditText.setText("");
            updateDeleteButtonVisibility();
            updateValidateButtonState();
        });
        recToDeleteButton.setOnClickListener(v -> {
            recToQrCodeEditText.setText("");
            updateDeleteButtonVisibility();
            updateValidateButtonState();
        });
        validateButton.setOnClickListener(v -> {
            String expFromLocation = Objects.requireNonNull(
                    expFromQrCodeEditText.getText()).toString().trim();
            String recToLocation = Objects.requireNonNull(
                    recToQrCodeEditText.getText()).toString().trim();
            // Check the selected tab
            int selectedTabPosition = tabLayoutRecExp.getSelectedTabPosition();
            if (selectedTabPosition == 0) { // Reception
                Log.d(TAG, "Reception -- RecToLocation: " + recToLocation);
                Intent intent = getTransferIntent(null, recToLocation,
                        selectedTabPosition);
                startActivity(intent);
            } else if (selectedTabPosition == 1) { // Expedition
                Log.d(TAG, "Expedition -- ExpFromLocation: " + expFromLocation);
                Intent intent = getTransferIntent(expFromLocation, null,
                        selectedTabPosition);
                startActivity(intent);
            }
        });

        // Set initial validate button state
        updateDeleteButtonVisibility();
        updateValidateButtonState();

        // Define the initial tab
        initOnTabSelection();
    }

    private void initOnTabSelection() {
        // Check if a tab was specified in the Intent (Default is Reception)
        int selectedTab = getIntent().getIntExtra(
                getString(R.string.selectedTab), Constants.TAB_RECEPTION);
        // Select the specified tab
        Objects.requireNonNull(tabLayoutRecExp.getTabAt(selectedTab)).select();
        // Set the initial state of the tab
        onTabSelected(Objects.requireNonNull(tabLayoutRecExp.getTabAt(selectedTab)));
    }

    @NonNull
    private Intent getTransferIntent(String fromLocation, String toLocation, int selectedTabPosition) {
        Intent intent = new Intent(ScanReceptionExpeditionActivity.this,
                ScanMainActivity.class);
        if (selectedTabPosition == 0) {
            intent.putExtra("sessionType", Constants.SCAN_SESSION_RECEPTION);
            intent.putExtra("toLocation", toLocation);
            intent.putExtra("toLocationId", receptionData.recToLocationId);
            intent.putExtra("toLocationCode", receptionData.recToLocationCode);
        } else if (selectedTabPosition == 1) {
            intent.putExtra("sessionType", Constants.SCAN_SESSION_EXPEDITION);
            intent.putExtra("fromLocation", fromLocation);
            intent.putExtra("fromLocationId", expeditionData.expFromLocationId);
            intent.putExtra("fromLocationCode", expeditionData.expFromLocationCode);
        }
        return intent;
    }

    private void initialization() {
        receptionLayout = findViewById(R.id.receptionLayout);
        expeditionLayout = findViewById(R.id.expeditionLayout);
        expFromQrCodeEditText = findViewById(R.id.expFromQrCodeEditText);
        recToQrCodeEditText = findViewById(R.id.recToQrCodeEditText);
        expFromQrCodeEditText.addTextChangedListener(OfflineTextWatcher);
        recToQrCodeEditText.addTextChangedListener(OfflineTextWatcher);
        expFromDeleteButton = findViewById(R.id.expFromDeleteButton);
        recToDeleteButton = findViewById(R.id.recToDeleteButton);
        validateButton = findViewById(R.id.validateButton);
        tabLayoutRecExp = findViewById(R.id.tabLayoutRecExp);
        tabLayoutRecExp.addOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (tab.getPosition() == 0) { // Reception Mode
            receptionLayout.setVisibility(View.VISIBLE);
            expeditionLayout.setVisibility(View.GONE);
            updateValidateButtonState();
            // Disable triggers for Reception Mode
            DataLogicUtils.setTriggersEnabled(false);
        } else if (tab.getPosition() == 1) { // Expedition Mode
            receptionLayout.setVisibility(View.GONE);
            expeditionLayout.setVisibility(View.VISIBLE);
        }
        // Enable triggers for both Modes
        DataLogicUtils.setTriggersEnabled(true);
        updateDeleteButtonVisibility();
        updateValidateButtonState();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    private void updateValidateButtonState() {
        String currentFromLocation = "";
        String currentToLocation = "";
        int selectedTabPosition = tabLayoutRecExp.getSelectedTabPosition();
        if (selectedTabPosition == 0) { // Reception
            if (recToQrCodeEditText.getText() != null) {
                currentToLocation = Objects.requireNonNull(
                        recToQrCodeEditText.getText()).toString().trim();
            }
            validateButton.setEnabled(!currentToLocation.isEmpty());
        } else if (selectedTabPosition == 1) { // Expedition
            if (expFromQrCodeEditText.getText() != null) {
                currentFromLocation = Objects.requireNonNull(
                        expFromQrCodeEditText.getText()).toString().trim();
            }
            validateButton.setEnabled(!currentFromLocation.isEmpty());
        }
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
        // Check if the expFromQrCodeEditText has text
        if (Objects.requireNonNull(expFromQrCodeEditText.getText()).length() > 0) {
            expFromDeleteButton.setVisibility(View.VISIBLE);
        } else {
            expFromDeleteButton.setVisibility(View.GONE);
        }
        // Check if the recToQrCodeEditText has text
        if (Objects.requireNonNull(recToQrCodeEditText.getText()).length() > 0) {
            recToDeleteButton.setVisibility(View.VISIBLE);
        } else {
            recToDeleteButton.setVisibility(View.GONE);
        }
    }

    private void parseScannedData(String scannedData) {
        // Format: (00)LOC|(10)0F6B321C-8E4E-41BD-B9B3-06A0B5DC9352|(11)PNT-M1|(12)MALTE
        if(!scannedData.startsWith(Constants.LOCATION_CODE_PREFIX)){
            showToast(this, "Invalid QR Code: Not a location code", 0);
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
        int selectedTabPosition = tabLayoutRecExp.getSelectedTabPosition();
        if (selectedTabPosition == 0) { // Reception
            recToQrCodeEditText.setText(locationName);
            receptionData.recToLocationId = locationId;
            receptionData.recToLocationCode = locationCode;
        } else if (selectedTabPosition == 1) { // Expedition
            expFromQrCodeEditText.setText(locationName);
            expeditionData.expFromLocationId = locationId;
            expeditionData.expFromLocationCode = locationCode;
        }
        updateDeleteButtonVisibility();
        updateValidateButtonState();
    }
    @Override
    public void onScan(String scannedData, String codeType) {
        Log.d(TAG, "Scanned Data: " + scannedData + ", Code Type: " + codeType);
        // Parse and Update the UI with the scan result
        parseScannedData(scannedData);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Re-initialize the scanner
        Log.d(TAG, "Init Scanner: ");
        DataLogicUtils.initializeScanner(this);
        // Re-initialize the DataLogicUtils
        Log.d(TAG, "Set Listener: ");
        DataLogicUtils.setScanListener(this);
        // Re-enable triggers
        Log.d(TAG, "Reception/Expedition mode Scan resumed: ");
        DataLogicUtils.setTriggersEnabled(true);
    }
    @Override
    protected void onPause() {
        super.onPause();
        DataLogicUtils.setTriggersEnabled(false);
    }
}