package com.videoclub;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.videoclub.utils.SessionManager;

/**
 * CloudStarTestActivity for Android TV
 * Shows Google Maps instead of OpenStreetMap
 */
public class CloudStarTestActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "CloudStarTestActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    // Map
    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;

    // Location
    private LocationManager locationManager;
    private String currentLatitude = "";
    private String currentLongitude = "";

    // UI Components
    private View loadingView;
    private View mapContainerView;
    private Button refreshButton;

    // Services
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_star_test);

        // Initialize session manager
        sessionManager = new SessionManager(this);

        // Initialize UI components
        initializeViews();

        // Get location from intent or saved preferences
        getLocationData();

        // Initialize map
        initializeMap();

        // Set up refresh button
        setupRefreshButton();
    }

    /**
     * Initialize UI views
     */
    private void initializeViews() {
        loadingView = findViewById(R.id.loading_container);
        mapContainerView = findViewById(R.id.map_container);
        refreshButton = findViewById(R.id.refresh_button);

        // Show loading view
        showLoading(true);
    }

    /**
     * Get location data from intent or saved preferences
     */
    private void getLocationData() {
        // Try to get from preferences
        currentLatitude = sessionManager.getPreference("userlat");
        currentLongitude = sessionManager.getPreference("userlong");

        Log.d(TAG, "Location data - Lat: " + currentLatitude + ", Long: " + currentLongitude);
    }

    /**
     * Initialize MapView
     */
    private void initializeMap() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Set up refresh button
     */
    private void setupRefreshButton() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshUserLocation();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Configure map settings
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        // Move to user location
        moveToUserLocation();
    }

    /**
     * Move camera to user location
     */
    private void moveToUserLocation() {
        if (currentLatitude != null && !currentLatitude.isEmpty() &&
                currentLongitude != null && !currentLongitude.isEmpty()) {

            try {
                double lat = Double.parseDouble(currentLatitude);
                double lng = Double.parseDouble(currentLongitude);
                LatLng userLocation = new LatLng(lat, lng);

                // Add marker for user location
                googleMap.addMarker(new MarkerOptions()
                        .position(userLocation)
                        .title("Your Location"));

                // Move camera to user location
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));

                // Hide loading view
                showLoading(false);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing coordinates: " + e.getMessage());
                Toast.makeText(this, "Invalid location coordinates", Toast.LENGTH_SHORT).show();
                refreshUserLocation();
            }
        } else {
            // No location available, request fresh location
            refreshUserLocation();
        }
    }

    /**
     * Refresh user's location
     */
    private void refreshUserLocation() {
        // Clear existing location
        currentLatitude = "";
        currentLongitude = "";

        // Show loading
        showLoading(true);

        // Request location permission if needed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            // Permission already granted, get location
            getLocation();
        }
    }

    /**
     * Get user's current location
     */
    private void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // Check if GPS or network provider is enabled
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Toast.makeText(this, "Please enable GPS or network location", Toast.LENGTH_SHORT).show();
                return;
            }

            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, locationListener);

            // Try to get last known location
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (lastKnownLocation != null) {
                handleNewLocation(lastKnownLocation);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception: " + e.getMessage());
            showLoading(false);
        } catch (Exception e) {
            Log.e(TAG, "Error getting location: " + e.getMessage());
            showLoading(false);
        }
    }

    /**
     * Handle new location
     */
    private void handleNewLocation(Location location) {
        currentLatitude = String.valueOf(location.getLatitude());
        currentLongitude = String.valueOf(location.getLongitude());

        // Save to preferences
        sessionManager.savePreference("userlat", currentLatitude);
        sessionManager.savePreference("userlong", currentLongitude);

        // Update map
        moveToUserLocation();

        // Remove location updates to save battery
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException e) {
                Log.e(TAG, "Error removing location updates: " + e.getMessage());
            }
        }
    }

    /**
     * Location listener
     */
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            handleNewLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    /**
     * Show or hide loading view
     */
    private void showLoading(boolean show) {
        loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        mapContainerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                getLocation();
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                showLoading(false);
            }
        }
    }

    /**
     * Handle key events for Android TV navigation
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                // Center button pressed - refresh location
                refreshUserLocation();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                // Up button pressed - zoom in
                if (googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.zoomIn());
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                // Down button pressed - zoom out
                if (googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.zoomOut());
                }
                return true;
            case KeyEvent.KEYCODE_BACK:
                // Back button pressed - go back
                finish();
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove location updates
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException e) {
                Log.e(TAG, "Error removing location updates: " + e.getMessage());
            }
        }
    }
}