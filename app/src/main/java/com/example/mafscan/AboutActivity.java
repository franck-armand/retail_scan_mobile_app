package com.example.mafscan;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.aboutToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get the app version
        String appVersion = Utils.getAppVersion(this);
        TextView appVersionTextView = findViewById(R.id.app_version);
        appVersionTextView.setText(appVersion);

        // Get and set app release date and version
        TextView releaseNoteTitle = findViewById(R.id.release_notes_title);
        String title = getString(R.string.release_notes) + " " + appVersion;
        releaseNoteTitle.setText(title);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavUtils.navigateUpFromSameTask(this);
        return true;
    }
}
