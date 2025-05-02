package com.videoclub;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.videoclub.models.DropdownItem;
import com.videoclub.utilities.Colors;
import com.videoclub.utilities.Images;
import com.videoclub.utilities.RestClient;
import com.videoclub.utilities.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * CouponVerificationActivity for Android TV
 * Converted from React Native CouponVerification component
 */
public class CouponVerificationActivity extends FragmentActivity {
    private static final String TAG = "CouponVerificationActivity";

    // UI components
    private ImageView backButton;
    private EditText serialNumberEditText;
    private EditText pinNumberEditText;
    private Spinner couponTypeSpinner;
    private Button verifyButton;

    // Data
    private String serialNumber = "";
    private String pinNumber = "";
    private String couponType = "";
    private List<DropdownItem> dropdownData;

    // Services
    private SessionManager sessionManager;
    private RestClient.ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_verification);

        // Initialize services
        sessionManager = new SessionManager(this);
        apiService = RestClient.getApiService();

        // Initialize UI components
        initializeViews();
        setupDropdown();
        setupListeners();

        // Load app colors and info
        getAppColors();
        getAppInfo();
    }

    /**
     * Initialize UI views
     */
    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        serialNumberEditText = findViewById(R.id.serial_number_edit_text);
        pinNumberEditText = findViewById(R.id.pin_number_edit_text);
        couponTypeSpinner = findViewById(R.id.coupon_type_spinner);
        verifyButton = findViewById(R.id.verify_button);

        // Set initial focus to serial number field for better TV navigation
        serialNumberEditText.requestFocus();
    }

    /**
     * Setup coupon type dropdown
     */
    private void setupDropdown() {
        // Create dropdown data
        dropdownData = new ArrayList<>();
        dropdownData.add(new DropdownItem("1", "Package"));
        dropdownData.add(new DropdownItem("2", "Channel"));
        dropdownData.add(new DropdownItem("3", "Vod"));
        dropdownData.add(new DropdownItem("4", "Viewers Club"));

        // Create and set adapter
        ArrayAdapter<DropdownItem> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner,
                dropdownData);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        couponTypeSpinner.setAdapter(adapter);
    }

    /**
     * Setup event listeners
     */
    private void setupListeners() {
        // Back button click
        backButton.setOnClickListener(v -> {
            navigateToLogin();
        });

        // Dropdown selection
        couponTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DropdownItem item = (DropdownItem) parent.getItemAtPosition(position);
                handleSelectedItem(item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Verify button click
        verifyButton.setOnClickListener(v -> {
            validateAndVerify();
        });
    }

    /**
     * Handle selected dropdown item
     */
    private void handleSelectedItem(DropdownItem item) {
        if ("Viewers Club".equals(item.getLabel())) {
            couponType = "viewersclub";
        } else {
            couponType = item.getLabel().toLowerCase();
        }
    }

    /**
     * Validate inputs and verify coupon
     */
    private void validateAndVerify() {
        serialNumber = serialNumberEditText.getText().toString().trim();
        pinNumber = pinNumberEditText.getText().toString().trim();

        if (couponType.isEmpty() || pinNumber.isEmpty() || serialNumber.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
        } else {
            verifyApi();
        }
    }

    /**
     * Send verification API request
     */
    private void verifyApi() {
        String token = sessionManager.getToken();
        if (token.isEmpty()) {
            navigateToLogin();
            return;
        }

        String userId = sessionManager.getUserId();
        String uniqueId = sessionManager.getUniqueId();

        Map<String, String> params = new HashMap<>();
        params.put("pin", pinNumber);
        params.put("serial_number", serialNumber);
        params.put("coupon_type", couponType);
        params.put("user_id", userId);
        params.put("unique_id", uniqueId);

        Call<ResponseBody> call = apiService.verifyCoupons("Bearer " + token, params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        String status = jsonObject.optString("status");
                        String message = jsonObject.optString("message");

                        if ("success".equalsIgnoreCase(status)) {
                            sessionManager.saveCouponVerify("yes");
                            navigateToHome();
                        } else {
                            sessionManager.saveCouponVerify("no");
                            Toast.makeText(CouponVerificationActivity.this, message, Toast.LENGTH_LONG).show();
                            navigateToHome(); // Navigate to home even on failure as per original code
                        }
                    } catch (IOException | JSONException e) {
                        handleApiError(e.getMessage());
                    }
                } else {
                    handleApiError("Verification failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleApiError(t.getMessage());
            }
        });
    }

    /**
     * Handle API errors
     */
    private void handleApiError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
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

                        // Process color data
                        // Code to parse color data would go here

                    } catch (IOException | JSONException e) {
                        // Error parsing response
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // API call failed
            }
        });
    }

    /**
     * Get app info from API
     */
    private void getAppInfo() {
        // Similar implementation as getAppColors
    }

    /**
     * Navigate to login screen
     */
    private void navigateToLogin() {
        sessionManager.saveCouponVerify("");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Navigate to home screen
     */
    private void navigateToHome() {
        Intent intent = new Intent(this, NewHomeScreenActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            navigateToLogin();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}