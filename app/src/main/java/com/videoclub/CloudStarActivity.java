package com.videoclub;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.FragmentActivity;

import com.videoclub.utilities.Colors;

/**
 * CloudStarActivity for Android TV
 * Converted from React Native CloudStarScreen component
 */
public class CloudStarActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_star);

        // Initialize buttons
        Button openStreetMapButton = findViewById(R.id.open_street_map_button);
        Button googleMapButton = findViewById(R.id.google_map_button);

        // Set up click listeners
        openStreetMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToOSMScreen();
            }
        });

        googleMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToGoogleMapScreen();
            }
        });
    }

    /**
     * Navigate to OpenStreetMap screen
     */
    private void navigateToOSMScreen() {
        Intent intent = new Intent(CloudStarActivity.this, OSMActivity.class);
        startActivity(intent);
    }

    /**
     * Navigate to Google Maps screen
     */
    private void navigateToGoogleMapScreen() {
        Intent intent = new Intent(CloudStarActivity.this, CloudStarTestActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}