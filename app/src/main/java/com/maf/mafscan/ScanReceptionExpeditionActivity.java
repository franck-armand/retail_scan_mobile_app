package com.maf.mafscan;

import static com.maf.mafscan.Utils.showToast;

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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ScanReceptionExpeditionActivity extends AppCompatActivity implements
        TabLayout.OnTabSelectedListener, DataLogicUtils.ScanListener {
    private final ReceptionData receptionData = new ReceptionData();
    private final ExpeditionData expeditionData = new ExpeditionData();
    private final InventoryData inventoryData = new InventoryData();
    private final ShuttleData shuttleData = new ShuttleData();
    private TabLayout tabLayoutRecExp;
    private LinearLayout receptionLayout, expeditionLayout, inventoryLayout, shuttleLayout;
    private Button validateButton;
    private ImageButton expFromDeleteButton, recToDeleteButton, invFromDeleteButton,
            shuttleFromDeleteButton;
    private TextInputEditText expFromQrCodeEditText, recToQrCodeEditText, invFromQrCodeEditText,
            shuttleFromQrCodeEditText;
    private ShuttleServerManager shuttleServerManager;
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expedition_reception);
        Executor executor = Executors.newSingleThreadExecutor();
        AppDatabase db = AppDatabase.getDatabase(this);
        shuttleServerManager = new ShuttleServerManager(this,
                executor, db.scanSessionDao());

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
        handleClickButton();

        // Set initial validate button state
        updateDeleteButtonVisibility();
        updateValidateButtonState();

        // Define the initial tab
        initOnTabSelection();
    }

    private void handleClickButton() {
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
        invFromDeleteButton.setOnClickListener(v -> {
            invFromQrCodeEditText.setText("");
            updateDeleteButtonVisibility();
            updateValidateButtonState();
        });
        shuttleFromDeleteButton.setOnClickListener(v -> {
            shuttleFromQrCodeEditText.setText("");
            updateDeleteButtonVisibility();
            updateValidateButtonState();
        });
        validateButton.setOnClickListener(v -> {
            // Check the selected tab
            int selectedTabPosition = tabLayoutRecExp.getSelectedTabPosition();
            if (selectedTabPosition == Constants.TAB_RECEPTION_ID) {
                Log.d(TAG, "Reception -- RecToLocation: " + receptionData.recToLocationName);
                Intent intent = getTransferIntent(null, receptionData.recToLocationName,
                        selectedTabPosition);
                startActivity(intent);
            } else if (selectedTabPosition == Constants.TAB_EXPEDITION_ID) {
                Log.d(TAG, "Expedition -- ExpFromLocation: " + expeditionData.expFromLocationName);
                Intent intent = getTransferIntent(expeditionData.expFromLocationName, null,
                        selectedTabPosition);
                startActivity(intent);
            } else if (selectedTabPosition == Constants.TAB_INVENTORY_ID) {
                Log.d(TAG, "Inventory -- InvFromLocation: " + inventoryData.invFromLocationName);
                Intent intent = getTransferIntent(inventoryData.invFromLocationName, null,
                        selectedTabPosition);
                startActivity(intent);
            } else if (selectedTabPosition == Constants.TAB_SHUTTLE_ID) {
                Log.d(TAG, "Shuttle -- ShuttleFromLocation: " + shuttleData.shuttleFromLocationName);
                shuttleServerManager.setSessionType(Constants.SCAN_SESSION_SHUTTLE);
                shuttleServerManager.setFromLocationId(shuttleData.shuttleFromLocationId);
                shuttleServerManager.setToLocationId(null);
                shuttleServerManager.sendShuttleDataToServer();
            }
        });
    }

    private void initOnTabSelection() {
        // Check if a tab was specified in the Intent (Default is Reception)
        int selectedTab = getIntent().getIntExtra(
                getString(R.string.selectedTab), Constants.TAB_RECEPTION_ID);
        // Select the specified tab
        Objects.requireNonNull(tabLayoutRecExp.getTabAt(selectedTab)).select();
        // Set the initial state of the tab
        onTabSelected(Objects.requireNonNull(tabLayoutRecExp.getTabAt(selectedTab)));
    }

    @NonNull
    private Intent getTransferIntent(String fromLocation, String toLocation, int selectedTabPosition) {
        Intent intent = new Intent(ScanReceptionExpeditionActivity.this,
                ScanMainActivity.class);
        if (selectedTabPosition == Constants.TAB_RECEPTION_ID) {
            intent.putExtra("sessionType", Constants.SCAN_SESSION_RECEPTION);
            intent.putExtra("toLocation", toLocation);
            intent.putExtra("toLocationId", receptionData.recToLocationId);
            intent.putExtra("toLocationCode", receptionData.recToLocationCode);
        } else if (selectedTabPosition == Constants.TAB_EXPEDITION_ID) {
            intent.putExtra("sessionType", Constants.SCAN_SESSION_EXPEDITION);
            intent.putExtra("fromLocation", fromLocation);
            intent.putExtra("fromLocationId", expeditionData.expFromLocationId);
            intent.putExtra("fromLocationCode", expeditionData.expFromLocationCode);
        } else if (selectedTabPosition == Constants.TAB_INVENTORY_ID) {
            intent.putExtra("sessionType", Constants.SCAN_SESSION_INVENTORY);
            intent.putExtra("fromLocation", fromLocation);
            intent.putExtra("fromLocationId", inventoryData.invFromLocationId);
            intent.putExtra("fromLocationCode", inventoryData.invFromLocationCode);
        }
        return intent;
    }

    private String getFullTabName(int tabId) {
        switch (tabId) {
            case Constants.TAB_RECEPTION_ID:
                return getString(R.string.reception_activity_title);
            case Constants.TAB_EXPEDITION_ID:
                return getString(R.string.expedition_activity_title);
            case Constants.TAB_INVENTORY_ID:
                return getString(R.string.inventory_activity_title);
            case Constants.TAB_SHUTTLE_ID:
                return getString(R.string.empty_shuttle_activity_title);
            default:
                return "";
        }
    }

    private void initialization() {
        receptionLayout = findViewById(R.id.receptionLayout);
        expeditionLayout = findViewById(R.id.expeditionLayout);
        inventoryLayout = findViewById(R.id.inventoryLayout);
        shuttleLayout = findViewById(R.id.shuttleLayout);

        invFromQrCodeEditText = findViewById(R.id.invFromQrCodeEditText);
        invFromDeleteButton = findViewById(R.id.invFromDeleteButton);
        invFromQrCodeEditText.addTextChangedListener(OfflineTextWatcher);

        expFromQrCodeEditText = findViewById(R.id.expFromQrCodeEditText);
        expFromDeleteButton = findViewById(R.id.expFromDeleteButton);
        expFromQrCodeEditText.addTextChangedListener(OfflineTextWatcher);

        recToQrCodeEditText = findViewById(R.id.recToQrCodeEditText);
        recToDeleteButton = findViewById(R.id.recToDeleteButton);
        recToQrCodeEditText.addTextChangedListener(OfflineTextWatcher);

        shuttleFromQrCodeEditText = findViewById(R.id.shuttleFromQrCodeEditText);
        shuttleFromDeleteButton = findViewById(R.id.shuttleFromDeleteButton);
        shuttleFromQrCodeEditText.addTextChangedListener(OfflineTextWatcher);

        validateButton = findViewById(R.id.validateButton);
        tabLayoutRecExp = findViewById(R.id.tabLayoutRecExp);
        tabLayoutRecExp.addOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        // Set the app bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getFullTabName(tab.getPosition()));
        }
        if (tab.getPosition() == Constants.TAB_RECEPTION_ID) {
            receptionLayout.setVisibility(View.VISIBLE);
            expeditionLayout.setVisibility(View.GONE);
            inventoryLayout.setVisibility(View.GONE);
            shuttleLayout.setVisibility(View.GONE);
        } else if (tab.getPosition() == Constants.TAB_EXPEDITION_ID) {
            expeditionLayout.setVisibility(View.VISIBLE);
            receptionLayout.setVisibility(View.GONE);
            inventoryLayout.setVisibility(View.GONE);
            shuttleLayout.setVisibility(View.GONE);
        } else if (tab.getPosition() == Constants.TAB_INVENTORY_ID) {
            inventoryLayout.setVisibility(View.VISIBLE);
            receptionLayout.setVisibility(View.GONE);
            expeditionLayout.setVisibility(View.GONE);
            shuttleLayout.setVisibility(View.GONE);
        } else if (tab.getPosition() == Constants.TAB_SHUTTLE_ID) {
            shuttleLayout.setVisibility(View.VISIBLE);
            receptionLayout.setVisibility(View.GONE);
            expeditionLayout.setVisibility(View.GONE);
            inventoryLayout.setVisibility(View.GONE);
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
        if (selectedTabPosition == Constants.TAB_RECEPTION_ID) {
            if (recToQrCodeEditText.getText() != null) {
                currentToLocation = Objects.requireNonNull(
                        recToQrCodeEditText.getText()).toString().trim();
            }
            validateButton.setEnabled(!currentToLocation.isEmpty());
        } else if (selectedTabPosition == Constants.TAB_EXPEDITION_ID) {
            if (expFromQrCodeEditText.getText() != null) {
                currentFromLocation = Objects.requireNonNull(
                        expFromQrCodeEditText.getText()).toString().trim();
            }
            validateButton.setEnabled(!currentFromLocation.isEmpty());
        } else if (selectedTabPosition == Constants.TAB_INVENTORY_ID) {
            if (invFromQrCodeEditText.getText() != null) {
                currentFromLocation = Objects.requireNonNull(
                        invFromQrCodeEditText.getText()).toString().trim();
            }
            validateButton.setEnabled(!currentFromLocation.isEmpty());
        } else if (selectedTabPosition == Constants.TAB_SHUTTLE_ID) {
            if (shuttleFromQrCodeEditText.getText() != null) {
                currentFromLocation = Objects.requireNonNull(
                        shuttleFromQrCodeEditText.getText()).toString().trim();
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
        int selectedTabPosition = tabLayoutRecExp.getSelectedTabPosition();
        // Check if the expFromQrCodeEditText has text
        if (selectedTabPosition == Constants.TAB_EXPEDITION_ID &&
                Objects.requireNonNull(expFromQrCodeEditText.getText()).length() > 0) {
            expFromDeleteButton.setVisibility(View.VISIBLE);
        } else {
            expFromDeleteButton.setVisibility(View.GONE);
        }
        // Check if the recToQrCodeEditText has text
        if (selectedTabPosition == Constants.TAB_RECEPTION_ID &&
                Objects.requireNonNull(recToQrCodeEditText.getText()).length() > 0) {
            recToDeleteButton.setVisibility(View.VISIBLE);
        } else {
            recToDeleteButton.setVisibility(View.GONE);
        }
        if (selectedTabPosition == Constants.TAB_INVENTORY_ID &&
                Objects.requireNonNull(invFromQrCodeEditText.getText()).length() > 0) {
            invFromDeleteButton.setVisibility(View.VISIBLE);
        } else {
            invFromDeleteButton.setVisibility(View.GONE);
        } if (selectedTabPosition == Constants.TAB_SHUTTLE_ID &&
                Objects.requireNonNull(shuttleFromQrCodeEditText.getText()).length() > 0) {
            shuttleFromDeleteButton.setVisibility(View.VISIBLE);
        } else {
            shuttleFromDeleteButton.setVisibility(View.GONE);
        }
    }

//    private void updateDeleteButtonVisibility() {
//        int selectedTabPosition = tabLayoutRecExp.getSelectedTabPosition();
//        // Check if the expFromQrCodeEditText has text
//        if (selectedTabPosition == Constants.TAB_EXPEDITION_ID &&
//                expeditionData.expFromLocationName != null) {
//            expFromDeleteButton.setVisibility(View.VISIBLE);
//        } else {
//            expFromDeleteButton.setVisibility(View.GONE);
//        }
//        // Check if the recToQrCodeEditText has text
//        if (selectedTabPosition == Constants.TAB_RECEPTION_ID &&
//                receptionData.recToLocationName != null) {
//            recToDeleteButton.setVisibility(View.VISIBLE);
//        } else {
//            recToDeleteButton.setVisibility(View.GONE);
//        }
//        // Check if the invFromQrCodeEditText has text
//        if (selectedTabPosition == Constants.TAB_INVENTORY_ID &&
//                inventoryData.invFromLocationName != null) {
//            invFromDeleteButton.setVisibility(View.VISIBLE);
//        } else {
//            invFromDeleteButton.setVisibility(View.GONE);
//        }
//        // Check if the shuttleFromQrCodeEditText has text
//        if (selectedTabPosition == Constants.TAB_SHUTTLE_ID &&
//                shuttleData.shuttleFromLocationName != null) {
//            shuttleFromDeleteButton.setVisibility(View.VISIBLE);
//        } else {
//            shuttleFromDeleteButton.setVisibility(View.GONE);
//        }
//    }

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
        if (selectedTabPosition == Constants.TAB_RECEPTION_ID) {
            recToQrCodeEditText.setText(locationName);
            receptionData.recToLocationId = locationId;
            receptionData.recToLocationCode = locationCode;
            receptionData.recToLocationName = locationName;
        } else if (selectedTabPosition == Constants.TAB_EXPEDITION_ID) {
            expFromQrCodeEditText.setText(locationName);
            expeditionData.expFromLocationId = locationId;
            expeditionData.expFromLocationCode = locationCode;
            expeditionData.expFromLocationName = locationName;
        } else if (selectedTabPosition == Constants.TAB_INVENTORY_ID) {
            invFromQrCodeEditText.setText(locationName);
            inventoryData.invFromLocationId = locationId;
            inventoryData.invFromLocationCode = locationCode;
            inventoryData.invFromLocationName = locationName;
        } else if (selectedTabPosition == Constants.TAB_SHUTTLE_ID) {
            shuttleFromQrCodeEditText.setText(locationName);
            shuttleData.shuttleFromLocationId = locationId;
            shuttleData.shuttleFromLocationCode = locationCode;
            shuttleData.shuttleFromLocationName = locationName;
        }
        updateDeleteButtonVisibility();
        updateValidateButtonState();
    }

    void clearShuttleData() {
        shuttleData.shuttleFromLocationId = null;
        shuttleData.shuttleFromLocationCode = null;
        shuttleData.shuttleFromLocationName = null;
        shuttleFromQrCodeEditText.setText("");
        updateValidateButtonState();
        updateDeleteButtonVisibility();
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