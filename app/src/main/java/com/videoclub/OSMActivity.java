package com.videoclub;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.videoclub.utils.Images;
import com.videoclub.utils.SessionManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

/**
 * OSMActivity for Android TV
 * Converted from React Native OSMScreen component
 */
public class OSMActivity extends FragmentActivity {
    private static final String TAG = "OSMActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    // UI Components
    private MapView mapView;
    private ProgressBar progressBar;
    private TextView statusTextView;
    private ImageView loadingImageView;
    private View mapContainer;
    private View loadingContainer;
    private Button refreshButton;

    // Map
    private IMapController mapController;
    private Marker userLocationMarker;

    // Location
    private LocationManager locationManager;
    private String currentLatitude = "";
    private String currentLongitude = "";

    // Services
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize OSMDroid configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue("com.videoclub");

        setContentView(R.layout.activity_osm);

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
        mapView = findViewById(R.id.mapView);
        progressBar = findViewById(R.id.progress_bar);
        statusTextView = findViewById(R.id.status_text);
        loadingImageView = findViewById(R.id.loading_image);
        mapContainer = findViewById(R.id.map_container);
        loadingContainer = findViewById(R.id.loading_container);
        refreshButton = findViewById(R.id.refresh_button);

        // Load loading image
        Glide.with(this)
                .load(Images.loadingmap)
                .into(loadingImageView);

        // Show loading view
        showLoading(true);
    }

    /**
     * Get location data from intent or saved preferences
     */
    private void getLocationData() {
        // Get location from intent
        if (getIntent() != null) {
            currentLatitude = getIntent().getStringExtra("currentLatitude");
            currentLongitude = getIntent().getStringExtra("currentLongitude");
        }

        // If no location from intent, try to get from preferences
        if (currentLatitude == null || currentLatitude.isEmpty() ||
                currentLongitude == null || currentLongitude.isEmpty()) {
            currentLatitude = sessionManager.getPreference("userlat");
            currentLongitude = sessionManager.getPreference("userlong");
        }

        Log.d(TAG, "Location data - Lat: " + currentLatitude + ", Long: " + currentLongitude);
    }

    /**
     * Initialize OSM MapView
     */
    private void initializeMap() {
        // Configure map
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);

        // Get map controller
        mapController = mapView.getController();
        mapController.setZoom(15.0);

        // Move to user location
        moveToUserLocation();
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

    /**
     * Move camera to user location
     */
    private void moveToUserLocation() {
        if (currentLatitude != null && !currentLatitude.isEmpty() &&
                currentLongitude != null && !currentLongitude.isEmpty()) {

            try {
                double lat = Double.parseDouble(currentLatitude);
                double lng = Double.parseDouble(currentLongitude);
                GeoPoint userLocation = new GeoPoint(lat, lng);

                // Add marker for user location
                addUserLocationMarker(userLocation);

                // Move camera to user location
                mapController.setCenter(userLocation);
                mapController.setZoom(15.0);

                // Apply rotation and tilt
                mapView.setMapOrientation(0);

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
     * Add a marker for user location
     */
    private void addUserLocationMarker(GeoPoint location) {
        // Remove existing marker if any
        if (userLocationMarker != null) {
            mapView.getOverlays().remove(userLocationMarker);
        }

        // Create new marker
        userLocationMarker = new Marker(mapView);
        userLocationMarker.setPosition(location);
        userLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        userLocationMarker.setTitle("Your Location");

        // Add marker to map
        mapView.getOverlays().add(userLocationMarker);
        mapView.invalidate();
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
        statusTextView.setText("Getting Location...");

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
            statusTextView.setText("Location permission denied");
            showLoading(false);
        } catch (Exception e) {
            Log.e(TAG, "Error getting location: " + e.getMessage());
            statusTextView.setText("Error getting location");
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
        loadingContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        mapContainer.setVisibility(show ? View.GONE : View.VISIBLE);
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
                statusTextView.setText("Location permission denied");
                showLoading(false);
            }
        }
    }

    /**
     * Handle key events for navigation
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                // Center button pressed - refresh location
                refreshUserLocation();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                // Up button pressed - pan up
                mapController.zoomIn();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                // Down button pressed - pan down
                mapController.zoomOut();
                return true;
            case KeyEvent.KEYCODE_BACK:
                // Back button pressed - go back
                finish();
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    // MapView lifecycle methods
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
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