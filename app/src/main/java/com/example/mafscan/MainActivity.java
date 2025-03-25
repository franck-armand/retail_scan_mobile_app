//package com.example.mafscan;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.VibrationEffect;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.LinearLayout;
//
//import androidx.activity.OnBackPressedCallback;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.core.splashscreen.SplashScreen;
//import androidx.core.view.GravityCompat;
//import androidx.drawerlayout.widget.DrawerLayout;
//import androidx.appcompat.app.ActionBarDrawerToggle;
//import android.os.Vibrator;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.android.material.navigation.NavigationView;
//public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
//    private DrawerLayout drawerLayout;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//
//        // SplashScreen
//        SplashScreen.installSplashScreen(this);
//
//        // Disable triggers initially
//        DataLogicUtils.setTriggersEnabled(false);
//
//        // initialize Vibrator
//        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
//        final VibrationEffect clickVibration = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);
//
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_home3);
//
//        // Get app version
//        String appVersion = Utils.getAppVersion(this);
//
//        // Set up the custom Toolbar
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            String title = getString(R.string.app_name);
//            getSupportActionBar().setTitle(title);
//        }
//
//        // Set up the app version
//        TextView appVersionTextView = findViewById(R.id.appVersionTextView);
//        appVersionTextView.setText(appVersion);
//
//        drawerLayout = findViewById(R.id.drawer_layout);
//        NavigationView navigationView = findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
//
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
//                drawerLayout, toolbar,
//                R.string.open_nav,
//                R.string.close_nav);
//        drawerLayout.addDrawerListener(toggle);
//        toggle.syncState();
//
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
//            Intent intent = new Intent(MainActivity.this, ScanFromToActivity.class);
//            startActivity(intent);
//        });
//        // set up Retrieve QR code info button navigation
//        LinearLayout retrieveButton = findViewById(R.id.retrieve_scan_info);
//        retrieveButton.setOnClickListener(v -> {
//            vibrator.vibrate(clickVibration);
//            Intent intent = new Intent(MainActivity.this, RetrieveScanInfoActivity.class);
//            startActivity(intent);
//        });
//        // Set up the failed or saved scans button navigation
//        LinearLayout failedOrSavedScansButton = findViewById(R.id.failed_or_saved_scans);
//        failedOrSavedScansButton.setOnClickListener(v -> {
//            vibrator.vibrate(clickVibration);
//            Intent intent = new Intent(MainActivity.this, FailedOrSavedScanActivity.class);
//            startActivity(intent);
//        });
//        // Set up the user guide button navigation
//        LinearLayout scanHistoryButton = findViewById(R.id.btn_action4);
//        scanHistoryButton.setOnClickListener(v -> {
//            vibrator.vibrate(clickVibration);
//            //showToast(this, "Not implemented yet!", 0);
//            Intent intent = new Intent(MainActivity.this, UserGuideActivity.class);
//            startActivity(intent);
//        });
//        // Set up the back press callback
//        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//                    drawerLayout.closeDrawer(GravityCompat.START);
//                } else {
//                    setEnabled(false);
//                    finish();
//                }
//            }
//        };
//        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
//    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu){
//        Utils.inflateMenu(this, menu, R.menu.home_menu);
//        return true;
//    }
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.home_action_auth) {
//            Intent intent = new Intent(this, LoginActivity.class);
//            startActivity(intent);
//            return true;
//        }else if (id == R.id.home_action_Help) {
//            Utils.showHelpDialog(getSupportFragmentManager());
//            return true;
//        } else if (id == R.id.home_action_settings) {
//            Utils.showToast(this, "Not implemented yet!", 0);
//        } else if (id == R.id.home_action_about) {
//            Intent intent = new Intent(this, AboutActivity.class);
//            startActivity(intent);
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        DataLogicUtils.setTriggersEnabled(false);
//    }
//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == R.id.nav_home) {
//            Toast.makeText(this, "Not implemented yet!", Toast.LENGTH_SHORT).show();
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                    new fragment_home()).commit();
//        } else if (item.getItemId() == R.id.nav_settings) {
//            Toast.makeText(this, "Not implemented yet!", Toast.LENGTH_SHORT).show();
//            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                    new fragment_settings()).commit();
//        } else if (item.getItemId() == R.id.nav_about) {
//            Intent intent = new Intent(this, AboutActivity.class);
//            startActivity(intent);
//        } else if (item.getItemId() == R.id.nav_login) {
//            Intent intent = new Intent(this, LoginActivity.class);
//            startActivity(intent);
//        }
//        drawerLayout.closeDrawer(GravityCompat.START);
//        return true;
//    }
//}


package com.example.mafscan;

import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, OnButtonClickListener {
    private DrawerLayout drawerLayout;
    private Vibrator vibrator;
    private VibrationEffect clickVibration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // SplashScreen
        SplashScreen.installSplashScreen(this);

        // Disable triggers initially
        DataLogicUtils.setTriggersEnabled(false);

        // initialize Vibrator
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        clickVibration = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home4);

        // Get app version
        String appVersion = Utils.getAppVersion(this);

        // Set up the custom Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
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

        // Set up the RecyclerView
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        // Get a reference to the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view_home);

        // Create sample HomeCard data
        List<HomeCard> cardList = new ArrayList<>();

        // Card 1
        List<String> buttonTitles1 = Arrays.asList(
                getString(R.string.transfer),
                getString(R.string.expedition),
                getString(R.string.reception),
                getString(R.string.shuttle),
                getString(R.string.inventory));
        List<Integer> buttonIcons1 = Arrays.asList(
                R.drawable.transfert,
                R.drawable.outbox_32,
                R.drawable.inbox_32,
                R.drawable.shuttle,
                R.drawable.inventory);
        cardList.add(new HomeCard(getString(R.string.card_1), buttonTitles1, buttonIcons1));

        // Card 2
        List<String> buttonTitles2 = Arrays.asList(
                getString(R.string.failed_or_saved_scans),
                getString(R.string.qr_info),
                getString(R.string.user_guide)
                );
        List<Integer> buttonIcons2 = Arrays.asList(
                R.drawable.ic_save,
                R.drawable.qr_info,
                R.drawable.user_guide);
        cardList.add(new HomeCard(getString(R.string.card_2), buttonTitles2, buttonIcons2));

        // Create an instance of HomeCardAdapter
        HomeCardAdapter adapter = new HomeCardAdapter(cardList, this);

        // Set the Layout Manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set the Adapter
        recyclerView.setAdapter(adapter);

        // Set the button click listener
        adapter.setOnButtonClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.inflateMenu(this, menu, R.menu.home_menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home_action_auth) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.home_action_Help) {
            Utils.showHelpDialog(getSupportFragmentManager());
            return true;
        } else if (id == R.id.home_action_settings) {
            Utils.showToast(this, "Not implemented yet!", 0);
        } else if (id == R.id.home_action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataLogicUtils.setTriggersEnabled(false);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_home) {
            Utils.showToast(this, "Not implemented yet!", 0);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new fragment_home()).commit();
        } else if (item.getItemId() == R.id.nav_settings) {
            Utils.showToast(this, "Not implemented yet!", 0);
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

    @Override
    public void onButtonClick(int cardIndex, int buttonIndex) {
        vibrator.vibrate(clickVibration);
        Intent intent;
        switch (cardIndex) {
            case 0: // Card 1
                switch (buttonIndex) {
                    case 0: // Transfer button
                        intent = new Intent(this, ScanFromToActivity.class);
                        startActivity(intent);
                        break;
                    case 1: // Expedition button
                        intent = new Intent(this, ScanReceptionExpeditionActivity.class);
                        intent.putExtra(getString(R.string.selectedTab), Constants.TAB_EXPEDITION_ID);
                        startActivity(intent);
                        break;
                    case 2: // Reception button
                        intent = new Intent(this, ScanReceptionExpeditionActivity.class);
                        intent.putExtra(getString(R.string.selectedTab), Constants.TAB_RECEPTION_ID);
                        startActivity(intent);
                        break;
                    case 3: // Empty Shuttle button
                        intent = new Intent(this, ScanReceptionExpeditionActivity.class);
                        intent.putExtra(getString(R.string.selectedTab), Constants.TAB_SHUTTLE_ID);
                        startActivity(intent);
                        break;
                    case 4: // Inventory button
                        intent = new Intent(this, ScanReceptionExpeditionActivity.class);
                        intent.putExtra(getString(R.string.selectedTab), Constants.TAB_INVENTORY_ID);
                        startActivity(intent);
                        break;
                }
                break;
            case 1: // Card 2
                switch (buttonIndex) {
                    case 0: // Failed/Saved button
                        intent = new Intent(this, FailedOrSavedScanActivity.class);
                        startActivity(intent);
                        break;
                    case 1: // QR info
                        intent = new Intent(this, RetrieveScanInfoActivity.class);
                        startActivity(intent);
                        break;
                    case 2: // User Guide button
                        intent = new Intent(this, UserGuideActivity.class);
                        startActivity(intent);
                        break;
                }
                break;
        }
    }
}