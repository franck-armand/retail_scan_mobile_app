package com.example.mafscan;


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
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener { // Implement the interface
    private DrawerLayout drawerLayout;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // SplashScreen
        SplashScreen.installSplashScreen(this);

        // initialize Vibrator
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        final VibrationEffect clickVibration = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home3);

        // Set up the custom Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
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
            Intent intent = new Intent(MainActivity.this, ScanActivityFromTo.class);
            startActivity(intent);
        });

        // set up Retrieve QR code info button navigation
        LinearLayout retrieveButton = findViewById(R.id.retrieve_scan_info);
        retrieveButton.setOnClickListener(v -> {
            vibrator.vibrate(clickVibration);
            Intent intent = new Intent(MainActivity.this, RetrieveScanInfo.class);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_home) {
            Toast.makeText(this, "Not implemented yet!", Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new fragment_home()).commit();
        } else if (item.getItemId() == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new fragment_settings()).commit();
        } else if (item.getItemId() == R.id.nav_about) {
            Toast.makeText(this, "Not implemented yet!", Toast.LENGTH_SHORT).show();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new fragment_about()).commit();
        } else if (item.getItemId() == R.id.nav_login) {
//            Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                    new fragment_login()).commit();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

}

//public class MainActivity extends AppCompatActivity {
//    private DrawerLayout drawerLayout;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        // SplashScreen
//        SplashScreen.installSplashScreen(this);
//
//        // initialize Vibrator
//        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//        final VibrationEffect clickVibration = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
//
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_home3);
//
//        // Set up the custom Toolbar
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        drawerLayout = findViewById(R.id.drawer_layout);
//        NavigationView navigationView = findViewById(R.id.nav_view);
//        //navigationView.setNavigationItemSelectedListener(this);
//
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
//                R.string.open_nav,
//                R.string.close_nav);
//        drawerLayout.addDrawerListener(toggle);
//        toggle.syncState();
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                    new fragment_home()).commit();
//            navigationView.setCheckedItem(R.id.nav_home);
//        }
//
//        // Set up Scan button navigation
//        LinearLayout scanButton = findViewById(R.id.btn_scan);
//        scanButton.setOnClickListener(v -> {
//            vibrator.vibrate(clickVibration);
//            Intent intent = new Intent(MainActivity.this, ScanActivityFromTo.class);
//            startActivity(intent);
//        });
//
//        // set up Retrieve QR code info button navigation
//        LinearLayout retrieveButton = findViewById(R.id.retrieve_scan_info);
//        retrieveButton.setOnClickListener(v -> {
//            vibrator.vibrate(clickVibration);
//            Intent intent = new Intent(MainActivity.this, RetrieveScanInfo.class);
//            startActivity(intent);
//        });
//    }
//
//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.nav_home:
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                        new fragment_home()).commit();
//                break;
//            case R.id.nav_settings:
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                        new fragment_settings()).commit();
//                break;
//            case R.id.nav_about:
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                        new fragment_about()).commit();
//                break;
//            case R.id.nav_logout:
//                Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                        new fragment_login()).commit();
//                break;
//        }
//        drawerLayout.closeDrawer(GravityCompat.START);
//        return true;
//    }
//    @Override
//    public void onBackPressed() {
//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            drawerLayout.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }
//}
