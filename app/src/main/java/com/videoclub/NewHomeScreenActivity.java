package com.videoclub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.VerticalGridView;

import com.bumptech.glide.Glide;
import com.videoclub.adapters.BottomMenuAdapter;
import com.videoclub.adapters.LeftMenuAdapter;
import com.videoclub.adapters.RightMenuAdapter;
import com.videoclub.models.MenuItem;
import com.videoclub.utils.ApiService;
import com.videoclub.utils.Constants;
import com.videoclub.utils.GridViewExtensions;
import com.videoclub.utils.Images;
import com.videoclub.utils.SessionManager;
import com.videoclub.utils.RestClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.videoclub.R;

/**
 * NewHomeScreenActivity for Android TV
 * Main home screen of the application
 */
public class NewHomeScreenActivity extends FragmentActivity {
    private static final String TAG = "NewHomeScreenActivity";

    // UI components
    private VerticalGridView leftMenuGridView;
    private HorizontalGridView bottomMenuGridView;
    private VerticalGridView rightMenuGridView;
    private WebView adWebView;
    private ImageView logo1ImageView;
    private ImageView logo2ImageView;
    private ImageView topMenu1ImageView;
    private ImageView topMenu2ImageView;
    private ImageView topMenu3ImageView;
    private ImageView scrollUpImageView;
    private ImageView scrollDownImageView;
    private ImageView rightScrollUpImageView;
    private ImageView rightScrollDownImageView;
    private ProgressBar progressBar;
    private TextView scrollTextView;
    private TextView rightScrollTextView;

    // Adapters
    private LeftMenuAdapter leftMenuAdapter;
    private RightMenuAdapter rightMenuAdapter;
    private BottomMenuAdapter bottomMenuAdapter;

    // Data
    private List<MenuItem> leftMenuItems = new ArrayList<>();
    private List<MenuItem> rightMenuItems = new ArrayList<>();
    private List<MenuItem> bottomMenuItems = new ArrayList<>();
    private List<MenuItem> topMenuItems = new ArrayList<>();
    private String appTitle = "";
    private String appLogo = "";
    private String appBgColor = "";
    private String appTextColor = "";
    private String buzzUrl = "";
    private String username = "username";
    private String currentLatitude = "";
    private String currentLongitude = "";

    // Services
    private SessionManager sessionManager;
    private ApiService apiService; // Instead of RestClient.ApiService

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_home_screen);

        // Initialize services
        sessionManager = new SessionManager(this);
        apiService = RestClient.getApiService();

        // Initialize views
        initializeViews();
        setupAdapters();
        setupListeners();

        // Load data
        loadStoredPreferences();
        getLocation();
        loadData();
    }

    /**
     * Initialize UI views
     */
    private void initializeViews() {
        leftMenuGridView = findViewById(R.id.left_menu_grid);
        bottomMenuGridView = findViewById(R.id.bottom_menu_grid);
        rightMenuGridView = findViewById(R.id.right_menu_grid);
        adWebView = findViewById(R.id.ad_webview);
        logo1ImageView = findViewById(R.id.logo1_image);
        logo2ImageView = findViewById(R.id.logo2_image);
        topMenu1ImageView = findViewById(R.id.top_menu1_image);
        topMenu2ImageView = findViewById(R.id.top_menu2_image);
        topMenu3ImageView = findViewById(R.id.top_menu3_image);
        scrollUpImageView = findViewById(R.id.scroll_up_image);
        scrollDownImageView = findViewById(R.id.scroll_down_image);
        rightScrollUpImageView = findViewById(R.id.right_scroll_up_image);
        rightScrollDownImageView = findViewById(R.id.right_scroll_down_image);
        progressBar = findViewById(R.id.progress_bar);
        scrollTextView = findViewById(R.id.scroll_text);
        rightScrollTextView = findViewById(R.id.right_scroll_text);

        // Configure WebView
        setupWebView();

        // Set up scroll text
        scrollTextView.setText(R.string.scroll);
        rightScrollTextView.setText(R.string.scroll);

        // Load logo images
        Glide.with(this)
                .load(Images.LOGO)
                .into(logo1ImageView);

        Glide.with(this)
                .load(Images.LOGO_1)
                .into(logo2ImageView);
    }

    /**
     * Set up WebView for advertisements
     */
    private void setupWebView() {
        adWebView.getSettings().setJavaScriptEnabled(true);
        adWebView.setWebViewClient(new WebViewClient());

        // Load ad HTML content
        String adHtml = "<iframe id='a66be93e' name='a66be93e' src='http://ads.msgnaa.info/delivery/afr.php?refresh=10&amp;zoneid=32&amp;target=_blank&amp;cb=INSERT_RANDOM_NUMBER_HERE' frameborder='0' scrolling='no' width='100%' height='100%'><a href='http://ads.msgnaa.info/delivery/ck.php?n=a80374e2&amp;cb=INSERT_RANDOM_NUMBER_HERE' target='_blank'><img src='http://ads.msgnaa.info/delivery/avw.php?zoneid=32&amp;cb=INSERT_RANDOM_NUMBER_HERE&amp;n=a80374e2' border='0' alt='' /></a></iframe>";
        adWebView.loadData(adHtml, "text/html", "utf-8");
    }

    /**
     * Set up grid view adapters
     */
    private void setupAdapters() {
        // Left menu adapter
        leftMenuAdapter = new LeftMenuAdapter(this, leftMenuItems);
        leftMenuGridView.setAdapter(leftMenuAdapter);

        // Right menu adapter
        rightMenuAdapter = new RightMenuAdapter(this, rightMenuItems);
        rightMenuGridView.setAdapter(rightMenuAdapter);

        // Bottom menu adapter
        bottomMenuAdapter = new BottomMenuAdapter(this, bottomMenuItems);
        bottomMenuGridView.setAdapter(bottomMenuAdapter);
    }

    /**
     * Set up event listeners
     */
    private void setupListeners() {
        // Left menu item click
        leftMenuAdapter.setOnItemClickListener(item -> {
            handleLeftMenuItemClick(item);
        });

        // Right menu item click
        rightMenuAdapter.setOnItemClickListener(item -> {
            handleRightMenuItemClick(item);
        });

        // Bottom menu item click
        bottomMenuAdapter.setOnItemClickListener(item -> {
            handleBottomMenuItemClick(item);
        });

        // Top menu item clicks
        topMenu1ImageView.setOnClickListener(v -> {
            if (topMenuItems.size() > 0) {
                handleTopMenuItemClick(topMenuItems.get(0));
            }
        });

        topMenu2ImageView.setOnClickListener(v -> {
            if (topMenuItems.size() > 1) {
                handleTopMenuItemClick(topMenuItems.get(1));
            }
        });

        topMenu3ImageView.setOnClickListener(v -> {
            if (topMenuItems.size() > 2) {
                handleTopMenuItemClick(topMenuItems.get(2));
            }
        });

        // Scroll buttons
        scrollUpImageView.setOnClickListener(v -> {
            scrollUpLeftMenu();
        });

        scrollDownImageView.setOnClickListener(v -> {
            scrollDownLeftMenu();
        });

        rightScrollUpImageView.setOnClickListener(v -> {
            scrollUpRightMenu();
        });

        rightScrollDownImageView.setOnClickListener(v -> {
            scrollDownRightMenu();
        });
    }

    /**
     * Load stored preferences
     */
    private void loadStoredPreferences() {
        appBgColor = sessionManager.getAppBgColor();
        appTextColor = sessionManager.getAppTextColor();
        appTitle = sessionManager.getAppType();
        appLogo = sessionManager.getPreference("app_logo");

        // Apply background color if available
        if (appBgColor != null && !appBgColor.isEmpty()) {
            try {
                int color = android.graphics.Color.parseColor(appBgColor);
                View rootView = findViewById(android.R.id.content);
                rootView.setBackgroundColor(color);
            } catch (Exception e) {
                Log.e(TAG, "Error setting background color: " + e.getMessage());
            }
        }
    }

    /**
     * Get device location
     */
    private void getLocation() {
        // In a real implementation, you would use LocationManager or FusedLocationProviderClient
        // For now, just get stored values
        currentLatitude = sessionManager.getPreference("userlat");
        currentLongitude = sessionManager.getPreference("userlong");
    }

    /**
     * Load data from APIs
     */
    private void loadData() {
        // Show progress
        progressBar.setVisibility(View.VISIBLE);

        // Load all data
        getAppColors();
        getBuzzUrl();
        getTopMenuData();
        getLeftMenuData();
        getRightMenuData();
        getBottomMenuData();
        getUserProfile();
    }

    /**
     * Get app colors from API
     */
    private void getAppColors() {
        String token = sessionManager.getToken();
        if (token.isEmpty()) return;

        Call<ResponseBody> call = apiService.getAppColors("Bearer " + token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);

                        JSONArray dataArray = jsonObject.optJSONArray("data");
                        if (dataArray != null) {
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject colorObj = dataArray.optJSONObject(i);
                                if (colorObj != null) {
                                    String fieldName = colorObj.optString("field_name");
                                    String colorCode = colorObj.optString("color_code");

                                    if ("App_bg_color".equals(fieldName)) {
                                        appBgColor = colorCode;
                                        sessionManager.saveAppBgColor(colorCode);
                                    } else if ("App_text_color".equals(fieldName)) {
                                        appTextColor = colorCode;
                                        sessionManager.saveAppTextColor(colorCode);
                                    }
                                }
                            }

                            // Apply background color
                            if (appBgColor != null && !appBgColor.isEmpty()) {
                                try {
                                    int color = android.graphics.Color.parseColor(appBgColor);
                                    View rootView = findViewById(android.R.id.content);
                                    rootView.setBackgroundColor(color);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error setting background color: " + e.getMessage());
                                }
                            }
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    /**
     * Get user profile data from API
     */
    private void getUserProfile() {
        String token = sessionManager.getToken();
        if (token.isEmpty()) return;

        Call<ResponseBody> call = apiService.getUserProfile("Bearer " + token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);

                        String status = jsonObject.optString("status");
                        if ("success".equalsIgnoreCase(status)) {
                            JSONObject dataObj = jsonObject.optJSONObject("data");
                            if (dataObj != null) {
                                username = dataObj.optString("name");
                            }
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    /**
     * Load top menu images
     */
    private void loadTopMenuImages() {
        if (topMenuItems.size() > 0) {
            String imageUrl1 = Constants.IMAGE_PATH_XXXHDPI + topMenuItems.get(0).getImage();
            Glide.with(this)
                    .load(imageUrl1)
                    .into(topMenu1ImageView);
        }

        if (topMenuItems.size() > 1) {
            String imageUrl2 = Constants.IMAGE_PATH_XXXHDPI + topMenuItems.get(1).getImage();
            Glide.with(this)
                    .load(imageUrl2)
                    .into(topMenu2ImageView);
        }

        if (topMenuItems.size() > 2) {
            String imageUrl3 = Constants.IMAGE_PATH_XXXHDPI + topMenuItems.get(2).getImage();
            Glide.with(this)
                    .load(imageUrl3)
                    .into(topMenu3ImageView);
        }
    }

    /**
     * Handle left menu item click
     */
    private void handleLeftMenuItemClick(MenuItem item) {
        if (item == null) return;

        if (item.getWebUrl() != null && !item.getWebUrl().isEmpty()) {
            // Open web URL
            Intent intent = new Intent(this, OpenWebViewActivity.class);
            intent.putExtra("url", item.getWebUrl());
            startActivity(intent);
        } else {
            // Handle special menu items
            switch (item.getName()) {
//                case "Settings":
//                    // Navigate to settings
//                    Intent settingsIntent = new Intent(this, SettingsActivity.class);
//                    startActivity(settingsIntent);
//                    break;
//
//                case "VOD":
//                    // Navigate to VOD screen
//                    Intent vodIntent = new Intent(this, VODScreenActivity.class);
//                    startActivity(vodIntent);
//                    break;

                case "My Channels":
                    // Navigate to my channels
                    Intent myChannelsIntent = new Intent(this, MyChannelsActivity.class);
                    startActivity(myChannelsIntent);
                    break;

                case "The Buzz":
                    // Open buzz URL
                    if (buzzUrl != null && !buzzUrl.isEmpty()) {
                        Intent buzzIntent = new Intent(this, OpenWebViewActivity.class);
                        buzzIntent.putExtra("url", buzzUrl);
                        startActivity(buzzIntent);
                    }
                    break;

                case "Cloud Star":
                    // Navigate to cloud star
                    Intent cloudStarIntent = new Intent(this, CloudStarActivity.class);
                    cloudStarIntent.putExtra("currentLatitude", currentLatitude);
                    cloudStarIntent.putExtra("currentLongitude", currentLongitude);
                    startActivity(cloudStarIntent);
                    break;

                default:
                    // Show under construction message
                    Toast.makeText(this, R.string.under_construction, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    /**
     * Handle right menu item click
     */
    private void handleRightMenuItemClick(MenuItem item) {
        if (item == null) return;

        // Navigate to nearby test screen
        Intent intent = new Intent(this, NearbyTestScreenActivity.class);
        intent.putExtra("storename", item.getName());
        intent.putExtra("currentLatitude", currentLatitude);
        intent.putExtra("currentLongitude", currentLongitude);
        intent.putExtra("alldata", item.toString());
        startActivity(intent);
    }

    /**
     * Handle bottom menu item click
     */
    private void handleBottomMenuItemClick(MenuItem item) {
        if (item == null) return;

        if ("Button 3".equals(item.getName())) {
            // Navigate to video conference
            Intent intent = new Intent(this, VideoConferenceActivity.class);
            intent.putExtra("url", item.getWebUrl());
            startActivity(intent);
        } else if (item.getWebUrl() != null && !item.getWebUrl().isEmpty()) {
            // Open web URL
            Intent intent = new Intent(this, OpenWebViewActivity.class);
            intent.putExtra("url", item.getWebUrl());
            startActivity(intent);
        } else {
            // Show message
            Toast.makeText(this, getString(R.string.app_name) + ": No URL found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle top menu item click
     */
    private void handleTopMenuItemClick(MenuItem item) {
        if (item == null) return;

        // Open web URL
        if (item.getWebUrl() != null && !item.getWebUrl().isEmpty()) {
            Intent intent = new Intent(this, OpenWebViewActivity.class);
            intent.putExtra("url", item.getWebUrl());
            startActivity(intent);
        }
    }

    /**
     * Scroll down in left menu
     */
//    private void scrollDownLeftMenu() {
//        if (leftMenuItems.size() > 1) {
//            int currentPosition = leftMenuGridView.getFirstVisiblePosition();
//            if (currentPosition > 1) {
//                leftMenuGridView.smoothScrollToPosition(currentPosition - 2);
//            } else {
//                leftMenuGridView.smoothScrollToPosition(0);
//            }
//        }
//    }
//
//    /**
//     * Scroll up in left menu
//     */
//    private void scrollUpLeftMenu() {
//        if (leftMenuItems.size() > 1) {
//            int currentPosition = leftMenuGridView.getFirstVisiblePosition();
//            if (currentPosition < leftMenuItems.size() - 3) {
//                leftMenuGridView.smoothScrollToPosition(currentPosition + 2);
//            } else {
//                leftMenuGridView.smoothScrollToPosition(leftMenuItems.size() - 1);
//            }
//        }
//    }
//
//    /**
//     * Scroll down in right menu
//     */
//    private void scrollDownRightMenu() {
//        if (rightMenuItems.size() > 1) {
//            int currentPosition = rightMenuGridView.getFirstVisiblePosition();
//            if (currentPosition > 1) {
//                rightMenuGridView.smoothScrollToPosition(currentPosition - 2);
//            } else {
//                rightMenuGridView.smoothScrollToPosition(0);
//            }
//        }
//    }
//
//    /**
//     * Scroll up in right menu
//     */
//    private void scrollUpRightMenu() {
//        if (rightMenuItems.size() > 1) {
//            int currentPosition = rightMenuGridView.getFirstVisiblePosition();
//            if (currentPosition < rightMenuItems.size() - 3) {
//                rightMenuGridView.smoothScrollToPosition(currentPosition + 2);
//            } else {
//                rightMenuGridView.smoothScrollToPosition(rightMenuItems.size() - 1);
//            }
//        }
//    }

    /**
     * Scroll down in left menu
     * Uses GridViewExtensions to get first visible position
     */
    private void scrollDownLeftMenu() {
        if (leftMenuItems.size() > 1) {
            int currentPosition = GridViewExtensions.getFirstVisiblePosition(leftMenuGridView);
            if (currentPosition > 1) {
                leftMenuGridView.smoothScrollToPosition(currentPosition - 2);
            } else {
                leftMenuGridView.smoothScrollToPosition(0);
            }
        }
    }

    /**
     * Scroll up in left menu
     * Uses GridViewExtensions to get first visible position
     */
    private void scrollUpLeftMenu() {
        if (leftMenuItems.size() > 1) {
            int currentPosition = GridViewExtensions.getFirstVisiblePosition(leftMenuGridView);
            if (currentPosition < leftMenuItems.size() - 3) {
                leftMenuGridView.smoothScrollToPosition(currentPosition + 2);
            } else {
                leftMenuGridView.smoothScrollToPosition(leftMenuItems.size() - 1);
            }
        }
    }

    /**
     * Scroll down in right menu
     * Uses GridViewExtensions to get first visible position
     */
    private void scrollDownRightMenu() {
        if (rightMenuItems.size() > 1) {
            int currentPosition = GridViewExtensions.getFirstVisiblePosition(rightMenuGridView);
            if (currentPosition > 1) {
                rightMenuGridView.smoothScrollToPosition(currentPosition - 2);
            } else {
                rightMenuGridView.smoothScrollToPosition(0);
            }
        }
    }

    /**
     * Scroll up in right menu
     * Uses GridViewExtensions to get first visible position
     */
    private void scrollUpRightMenu() {
        if (rightMenuItems.size() > 1) {
            int currentPosition = GridViewExtensions.getFirstVisiblePosition(rightMenuGridView);
            if (currentPosition < rightMenuItems.size() - 3) {
                rightMenuGridView.smoothScrollToPosition(currentPosition + 2);
            } else {
                rightMenuGridView.smoothScrollToPosition(rightMenuItems.size() - 1);
            }
        }
    }

    /**
     * Get Buzz URL from API
     */
    private void getBuzzUrl() {
        String token = sessionManager.getToken();
        if (token.isEmpty()) return;

        Call<ResponseBody> call = apiService.getBuzzUrl("Bearer " + token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);

                        String status = jsonObject.optString("status");
                        if ("success".equalsIgnoreCase(status)) {
                            JSONArray dataArray = jsonObject.optJSONArray("data");
                            if (dataArray != null && dataArray.length() > 0) {
                                JSONObject urlObj = dataArray.optJSONObject(0);
                                if (urlObj != null) {
                                    buzzUrl = urlObj.optString("url");
                                }
                            }
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    /**
     * Get top menu data from API
     */
    private void getTopMenuData() {
        String token = sessionManager.getToken();
        if (token.isEmpty()) return;

        Call<ResponseBody> call = apiService.getTopMenu("Bearer " + token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);

                        String status = jsonObject.optString("status");
                        if ("success".equalsIgnoreCase(status)) {
                            JSONArray dataArray = jsonObject.optJSONArray("data");
                            if (dataArray != null) {
                                topMenuItems.clear();

                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject menuObj = dataArray.optJSONObject(i);
                                    if (menuObj != null) {
                                        MenuItem menuItem = new MenuItem();
                                        menuItem.setId(menuObj.optString("id"));
                                        menuItem.setName(menuObj.optString("name"));
                                        menuItem.setImage(menuObj.optString("image"));
                                        menuItem.setWebUrl(menuObj.optString("web_url"));
                                        topMenuItems.add(menuItem);
                                    }
                                }

                                // Load top menu images
                                loadTopMenuImages();
                            }
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    /**
     * Get left menu data from API
     */
    private void getLeftMenuData() {
        String token = sessionManager.getToken();
        if (token.isEmpty()) return;

        Call<ResponseBody> call = apiService.getLeftMenu("Bearer " + token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);

                        String status = jsonObject.optString("status");
                        if ("success".equalsIgnoreCase(status)) {
                            JSONArray dataArray = jsonObject.optJSONArray("data");
                            if (dataArray != null) {
                                leftMenuItems.clear();

                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject menuObj = dataArray.optJSONObject(i);
                                    if (menuObj != null) {
                                        MenuItem menuItem = new MenuItem();
                                        menuItem.setId(menuObj.optString("id"));
                                        menuItem.setName(menuObj.optString("name"));
                                        menuItem.setImage(menuObj.optString("image"));
                                        menuItem.setWebUrl(menuObj.optString("web_url"));
                                        leftMenuItems.add(menuItem);
                                    }
                                }

                                // Update UI
                                runOnUiThread(() -> {
                                    leftMenuAdapter.notifyDataSetChanged();
                                    progressBar.setVisibility(View.GONE);
                                });
                            }
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Get right menu data from API
     */
    private void getRightMenuData() {
        String token = sessionManager.getToken();
        if (token.isEmpty()) return;

        Call<ResponseBody> call = apiService.getRightMenu("Bearer " + token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);

                        String status = jsonObject.optString("status");
                        if ("success".equalsIgnoreCase(status)) {
                            JSONArray dataArray = jsonObject.optJSONArray("data");
                            if (dataArray != null) {
                                rightMenuItems.clear();

                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject menuObj = dataArray.optJSONObject(i);
                                    if (menuObj != null) {
                                        MenuItem menuItem = new MenuItem();
                                        menuItem.setId(menuObj.optString("id"));
                                        menuItem.setName(menuObj.optString("name"));
                                        menuItem.setImage(menuObj.optString("image"));
                                        menuItem.setWebUrl(menuObj.optString("web_url"));
                                        rightMenuItems.add(menuItem);
                                    }
                                }

                                // Update UI
                                runOnUiThread(() -> {
                                    rightMenuAdapter.notifyDataSetChanged();
                                });
                            }
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    /**
     * Get bottom menu data from API
     */
    private void getBottomMenuData() {
        String token = sessionManager.getToken();
        if (token.isEmpty()) return;

        Call<ResponseBody> call = apiService.getBottomMenu("Bearer " + token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);

                        String status = jsonObject.optString("status");
                        if ("success".equalsIgnoreCase(status)) {
                            JSONArray dataArray = jsonObject.optJSONArray("data");
                            if (dataArray != null) {
                                bottomMenuItems.clear();

                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject menuObj = dataArray.optJSONObject(i);
                                    if (menuObj != null) {
                                        MenuItem menuItem = new MenuItem();
                                        menuItem.setId(menuObj.optString("id"));
                                        menuItem.setName(menuObj.optString("name"));
                                        menuItem.setImage(menuObj.optString("image"));
                                        menuItem.setWebUrl(menuObj.optString("web_url"));
                                        bottomMenuItems.add(menuItem);
                                    }
                                }

                                // Update UI
                                runOnUiThread(() -> {
                                    bottomMenuAdapter.notifyDataSetChanged();
                                });
                            }
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Exit app on back press from home screen
            finishAffinity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}