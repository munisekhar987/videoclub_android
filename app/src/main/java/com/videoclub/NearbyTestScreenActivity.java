package com.videoclub;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.videoclub.utils.ApiService;
import com.videoclub.utils.Constants;
import com.videoclub.utils.RestClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import com.videoclub.R;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * NearbyTestScreenActivity
 * Shows nearby locations based on user's location
 * Converted from React Native's Nearbytestscreen component
 */
public class NearbyTestScreenActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "NearbyTestScreen";

    // Google Maps
    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;

    // Data
    private String storeName = "";
    private String currentLatitude = "";
    private String currentLongitude = "";
    private String searchTerm = "";
    private String searchIcon = "";
    private List<PlaceData> nearbyPlaces = new ArrayList<>();

    // API
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyBZUE4_bbnG9gPUpUWPPuZUOdJEKpJMGkU";
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_test_screen);

        // Initialize API service
        apiService = RestClient.getApiService();

        // Get data from intent
        if (getIntent() != null) {
            storeName = getIntent().getStringExtra("storename");
            currentLatitude = getIntent().getStringExtra("currentLatitude");
            currentLongitude = getIntent().getStringExtra("currentLongitude");

            String allData = getIntent().getStringExtra("alldata");
            if (allData != null && !allData.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject(allData);
                    searchTerm = jsonObject.optString("search_term", "");
                    searchIcon = jsonObject.optString("search_icon", "");
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON data: " + e.getMessage());
                }
            }
        }

        // Initialize map
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize refresh button
        Button refreshButton = findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(v -> refreshLocation());

        // Show loading state
        updateLoadingState(true);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Check if we have location data
        if (currentLatitude != null && !currentLatitude.isEmpty() &&
                currentLongitude != null && !currentLongitude.isEmpty()) {

            try {
                // Parse latitude and longitude
                double lat = Double.parseDouble(currentLatitude);
                double lng = Double.parseDouble(currentLongitude);

                // Add user location marker
                LatLng userLocation = new LatLng(lat, lng);
                googleMap.addMarker(new MarkerOptions()
                        .position(userLocation)
                        .title("Your Location"));

                // Move camera to user location
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14f));

                // Search for nearby places
                if (searchTerm != null && !searchTerm.isEmpty()) {
                    fetchNearbyPlaces(lat, lng, searchTerm);
                }

                updateLoadingState(false);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing coordinates: " + e.getMessage());
                Toast.makeText(this, "Invalid location data", Toast.LENGTH_SHORT).show();
                updateLoadingState(false);
            }
        } else {
            Toast.makeText(this, "Location data not available", Toast.LENGTH_SHORT).show();
            updateLoadingState(false);
        }
    }

    /**
     * Fetch nearby places using Google Places API
     */
    private void fetchNearbyPlaces(double latitude, double longitude, String keyword) {
        // Build URL for Places API request
        @SuppressLint("DefaultLocale") String url = String.format(
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=1000&key=%s&keyword=%s",
                latitude, longitude, GOOGLE_MAPS_API_KEY, keyword);

        // Make API request
        Call<ResponseBody> call = apiService.getUrl(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        processPlacesResponse(jsonResponse);
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "API call unsuccessful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    /**
     * Process the Places API response and add markers to the map
     */
    private void processPlacesResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            String status = jsonObject.optString("status");

            if ("OK".equals(status)) {
                JSONArray results = jsonObject.getJSONArray("results");

                // Clear previous places
                nearbyPlaces.clear();

                // Process each place
                for (int i = 0; i < results.length(); i++) {
                    JSONObject place = results.getJSONObject(i);

                    String placeId = place.optString("place_id");
                    String name = place.optString("name");

                    JSONObject geometry = place.getJSONObject("geometry");
                    JSONObject location = geometry.getJSONObject("location");
                    double lat = location.getDouble("lat");
                    double lng = location.getDouble("lng");

                    // Create place data object
                    PlaceData placeData = new PlaceData(placeId, name, lat, lng);
                    nearbyPlaces.add(placeData);

                    // Add marker to map
                    LatLng placeLocation = new LatLng(lat, lng);
                    googleMap.addMarker(new MarkerOptions()
                            .position(placeLocation)
                            .title(name));
                }

                runOnUiThread(() -> {
                    Toast.makeText(NearbyTestScreenActivity.this,
                            "Found " + nearbyPlaces.size() + " nearby places",
                            Toast.LENGTH_SHORT).show();
                });
            } else {
                Log.e(TAG, "Places API error: " + status);
                runOnUiThread(() -> {
                    Toast.makeText(NearbyTestScreenActivity.this,
                            "Error fetching nearby places",
                            Toast.LENGTH_SHORT).show();
                });
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing places data: " + e.getMessage());
        }
    }

    /**
     * Refresh user location and nearby places
     */
    private void refreshLocation() {
        // In a real app, you would use LocationManager or FusedLocationProviderClient
        // For now, we'll just show a toast
        Toast.makeText(this, "Refreshing location...", Toast.LENGTH_SHORT).show();
    }

    /**
     * Update loading state UI
     */
    private void updateLoadingState(boolean isLoading) {
        View loadingView = findViewById(R.id.loading_container);
        if (loadingView != null) {
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        View mapContainer = findViewById(R.id.map_container);
        if (mapContainer != null) {
            mapContainer.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Simple data class to hold place information
     */
    private static class PlaceData {
        private final String id;
        private final String name;
        private final double latitude;
        private final double longitude;

        public PlaceData(String id, String name, double latitude, double longitude) {
            this.id = id;
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}