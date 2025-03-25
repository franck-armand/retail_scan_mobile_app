package com.maf.mafscan;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class UserGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_guide);

        Toolbar toolbar = findViewById(R.id.user_guide_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        UserGuidePagerAdapter pagerAdapter = new UserGuidePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Scan Code");
                            break;
                        case 1:
                            tab.setText("Maf Scan");
                            break;
                    }
                }).attach();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.inflateMenu(this, menu, R.menu.user_guide_menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.user_guide_action_Help) {
            Utils.showHelpDialog(getSupportFragmentManager());
            return true;
        } else if (id == R.id.user_guide_action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.user_guide_action_auth) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.user_guide_action_settings) {
            Utils.showToast(this, "Settings not implemented yet!", 0);
            return true;
        } else if (id == R.id.user_guide_new_session) {
            Intent intent = new Intent(this, ScanFromToActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.user_guide_scan_info){
            Intent intent = new Intent(this, RetrieveScanInfoActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}