package com.example.mafscan;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements
        TabLayout.OnTabSelectedListener, DataLogicUtils.ScanListener {

    TextInputEditText ldapUsernameEditText;
    TextInputEditText ldapPasswordEditText;
    TextInputEditText scanUsernameEditText;
    Button authenticationLdapButton;
    Button authenticationScanButton;
    LinearLayout ldapLayout;
    LinearLayout scanLayout;
    private static final String TAG = LoginActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize the scanner
        DataLogicUtils.initializeScanner(this);

        // Set the scan listener
        DataLogicUtils.setScanListener(this);

        ldapUsernameEditText = findViewById(R.id.ldapUsernameEditText);
        ldapPasswordEditText = findViewById(R.id.ldapPasswordEditText);
        authenticationLdapButton = findViewById(R.id.validateLdapButton);
        authenticationScanButton = findViewById(R.id.validateScanButton);
        scanUsernameEditText = findViewById(R.id.scanUsernameEditText);
        ldapUsernameEditText.addTextChangedListener(LoginTextWatcher);
        ldapPasswordEditText.addTextChangedListener(LoginTextWatcher);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(this);

        ldapLayout = findViewById(R.id.ldapLayout);
        scanLayout = findViewById(R.id.scanLayout);

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (tab.getPosition() == 0) { // LDAP Auth
            ldapLayout.setVisibility(View.VISIBLE);
            scanLayout.setVisibility(View.GONE);
            // Disable triggers for LDAP Auth
            DataLogicUtils.setTriggersEnabled(false);
        } else if (tab.getPosition() == 1) { // Scan Auth
            // Enable triggers for Scan Auth
            DataLogicUtils.setTriggersEnabled(true);
            // Clear field
            clearSetAndDisableField();
            updateScanButtonState();
        }
    }

    private void clearSetAndDisableField() {
        scanUsernameEditText.setText("");
        scanUsernameEditText.setEnabled(false);
        ldapLayout.setVisibility(View.GONE);
        scanLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }
    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    private final TextWatcher LoginTextWatcher = new TextWatcher() {
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
            // Check if both fields are filled and update the button state
            updateLoginButtonState();
        }
    };

    private void updateLoginButtonState() {
        String username = Objects.requireNonNull(ldapUsernameEditText.getText()).toString().trim();
        String password = Objects.requireNonNull(ldapPasswordEditText.getText()).toString().trim();
        authenticationLdapButton.setEnabled(!username.isEmpty() && !password.isEmpty());
    }

    @Override
    public void onScan(String scannedData, String codeType) {
        Log.d(TAG, "Scanned Data: " + scannedData + ", Code Type: " + codeType);
        // Parse and Update the UI with the scan result
        parseScannedData(scannedData);
        // Update the scan button state
        updateScanButtonState();

    }

    private void parseScannedData(String scannedData) {
        // TODO : Place holder to format the scan result accordingly later
        // Split the data into key-value pairs
        String[] pairs = scannedData.trim().split("\\|");
        scanUsernameEditText.setText(scannedData.trim());
    }

    private void updateScanButtonState() {
        String scanResult = scanUsernameEditText.getText().toString().trim();
        authenticationScanButton.setEnabled(!scanResult.isEmpty());
    }
}