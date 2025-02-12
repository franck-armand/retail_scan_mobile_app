package com.example.mafscan;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements
        TabLayout.OnTabSelectedListener {

    TextInputEditText usernameEditText;
    TextInputEditText passwordEditText;
    Button validateLdapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        validateLdapButton = findViewById(R.id.validateLdapButton);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(this);

        usernameEditText.addTextChangedListener(LoginTextWatcher);
        passwordEditText.addTextChangedListener(LoginTextWatcher);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        LinearLayout ldapLayout = findViewById(R.id.ldapLayout);
        LinearLayout scanLayout = findViewById(R.id.scanLayout);
        if (tab.getPosition() == 0) { // LDAP Auth
            ldapLayout.setVisibility(View.VISIBLE);
            scanLayout.setVisibility(View.GONE);
        } else if (tab.getPosition() == 1) { // Scan Auth
            EditText scanUsernameResult = findViewById(R.id.scanUsernameResult);
            scanUsernameResult.setEnabled(false);
            ldapLayout.setVisibility(View.GONE);
            scanLayout.setVisibility(View.VISIBLE);
        }
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
        String username = Objects.requireNonNull(usernameEditText.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();

        validateLdapButton.setEnabled(!username.isEmpty() && !password.isEmpty());
    }
}