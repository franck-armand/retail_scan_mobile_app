package com.example.mafscan;
import static com.example.mafscan.Utils.showToast;

import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.os.Vibrator;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // SplashScreen
        SplashScreen.installSplashScreen(this);

        // Disable triggers initially
        DataLogicUtils.setTriggersEnabled(false);

        // initialize Vibrator
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        final VibrationEffect clickVibration = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home3);

        // Get app version
        String appVersion = Utils.getAppVersion(this);

        // Set up the custom Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            //String title = getString(R.string.app_name) + " _ " + appVersion;
            String title = getString(R.string.app_name);
            getSupportActionBar().setTitle(title);
        }

        // Set up the app version
        TextView appVersionTextView = findViewById(R.id.appVersionTextView);
        appVersionTextView.setText(appVersion);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawerLayout, toolbar,
                R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new fragment_home()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        // Set up Scan button navigation
        LinearLayout scanButton = findViewById(R.id.btn_scan);
        scanButton.setOnClickListener(v -> {
            vibrator.vibrate(clickVibration);
            Intent intent = new Intent(MainActivity.this, ScanFromToActivity.class);
            startActivity(intent);
        });

        // set up Retrieve QR code info button navigation
        LinearLayout retrieveButton = findViewById(R.id.retrieve_scan_info);
        retrieveButton.setOnClickListener(v -> {
            vibrator.vibrate(clickVibration);
            Intent intent = new Intent(MainActivity.this, RetrieveScanInfoActivity.class);
            startActivity(intent);
        });

        // Set up the failed or saved scans button navigation
        LinearLayout failedOrSavedScansButton = findViewById(R.id.failed_or_saved_scans);
        failedOrSavedScansButton.setOnClickListener(v -> {
            vibrator.vibrate(clickVibration);
            Intent intent = new Intent(MainActivity.this, FailedOrSavedScanActivity.class);
            startActivity(intent);
        });

        // Set up the user guide button navigation
        LinearLayout scanHistoryButton = findViewById(R.id.btn_action4);
        scanHistoryButton.setOnClickListener(v -> {
            vibrator.vibrate(clickVibration);
            //showToast(this, "Not implemented yet!", 0);
            Intent intent = new Intent(MainActivity.this, UserGuideActivity.class);
            startActivity(intent);
        });


        // Set up the back press callback
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataLogicUtils.setTriggersEnabled(false);

    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_home) {
            Toast.makeText(this, "Not implemented yet!", Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new fragment_home()).commit();
        } else if (item.getItemId() == R.id.nav_settings) {
            Toast.makeText(this, "Not implemented yet!", Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new fragment_settings()).commit();
        } else if (item.getItemId() == R.id.nav_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_login) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
