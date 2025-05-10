package com.videoclub;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.videoclub.utils.ApiService;
import com.videoclub.utils.DeviceInfoUtil;
import com.videoclub.utils.Images;
import com.videoclub.utils.RestClient;
import com.videoclub.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * LoginActivity for Android TV
 * Converted from React Native Login component
 */
public class LoginActivity extends FragmentActivity {
    private static final String TAG = "LoginActivity";

    // UI components
    private ImageView logoImageView;
    private TextView titleTextView;
    private EditText emailEditText;
    private EditText passwordEditText;
    private CheckBox rememberMeCheckBox;
    private Button loginButton;
    private TextView signUpTextView;
    private TextView termsTextView;

    // Data
    private String email = "";
    private String password = "";
    private boolean rememberMe = false;
    private String uniqueId = "";
    private String ipAddress = "";
    private String macAddress = "02:00:00:00:00:00";

    // Services
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize services
        sessionManager = new SessionManager(this);
        apiService = RestClient.getApiService();

        // Initialize UI components
        initializeViews();
        setupListeners();

        // Get device information
        getDeviceInfo();

        // Auto-fill email and password if remember me is enabled
        if (sessionManager.isRememberMeEnabled()) {
            emailEditText.setText(sessionManager.getEmail());
            passwordEditText.setText(sessionManager.getPassword());
            rememberMeCheckBox.setChecked(true);
            rememberMe = true;
        }
    }

    /**
     * Initialize UI views
     */
    private void initializeViews() {
        logoImageView = findViewById(R.id.logo_image);
        titleTextView = findViewById(R.id.title_text);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        rememberMeCheckBox = findViewById(R.id.remember_me_checkbox);
        loginButton = findViewById(R.id.login_button);
        signUpTextView = findViewById(R.id.signup_text);
        termsTextView = findViewById(R.id.terms_text);

        // Set logo
        logoImageView.setImageResource(Images.LOGO);

        // Set initial focus to email field for better TV navigation
        emailEditText.requestFocus();
    }

    /**
     * Setup event listeners
     */
    private void setupListeners() {
        // Password editor action (done/enter key)
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    loginUser();
                    return true;
                }
                return false;
            }
        });

        // Remember me checkbox
        rememberMeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rememberMe = isChecked;
        });

        // Login button click
        loginButton.setOnClickListener(v -> {
            loginUser();
        });

        // Sign up text click
        signUpTextView.setOnClickListener(v -> {
            navigateToRegistration();
        });
    }

    /**
     * Get device information
     */
    private void getDeviceInfo() {
        // Get unique device ID
        uniqueId = DeviceInfoUtil.getUniqueId(this);

        // Get IP address
        ipAddress = DeviceInfoUtil.getIpAddress(this);

        // Get MAC address
        macAddress = DeviceInfoUtil.getMacAddress(this);
    }

    /**
     * Login user with provided credentials
     */
    private void loginUser() {
        // Get input values
        email = emailEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        // Hide keyboard
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
        }

        // Prepare login parameters
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
        params.put("unique_id", uniqueId);
        params.put("ip_address", ipAddress);
        params.put("package_name", "com.videoclub");

        // Make API call
        Call<ResponseBody> call = apiService.login(params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        String status = jsonObject.optString("status");

                        if ("success".equalsIgnoreCase(status)) {
                            // Save session data
                            JSONObject data = jsonObject.optJSONObject("data");
                            if (data != null) {
                                String token = data.optString("token");
                                String userId = String.valueOf(data.optInt("user_id"));
                                String appType = data.optString("app_type");

                                sessionManager.saveToken(token);
                                sessionManager.saveUserId(userId);
                                sessionManager.saveAppType(appType);
                                sessionManager.saveUniqueId(uniqueId);

                                // Save user credentials if remember me is checked
                                if (rememberMe) {
                                    sessionManager.saveUserCredentials(email, password, true);
                                } else {
                                    sessionManager.saveUserCredentials("", "", false);
                                }

                                // Save user data for later use
                                sessionManager.savePreference("userData", jsonResponse);

                                // Get assets and continue with navigation
                                getAssets(data.toString());
                            }
                        } else {
                            String message = jsonObject.optString("message");
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException | JSONException e) {
                        handleApiError(e.getMessage());
                    }
                } else {
                    handleApiError("Login failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleApiError(t.getMessage());
            }
        });
    }

    /**
     * Get app assets
     */
    private void getAssets(String userData) {
        Map<String, String> params = new HashMap<>();
        params.put("folder", "xxhdpi");

        Call<ResponseBody> call = apiService.getAssets(params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        boolean status = jsonObject.optBoolean("status");

                        if (status) {
                            JSONObject data = jsonObject.optJSONObject("data");
                            if (data != null) {
                                String folder = data.optString("folder");
                                sessionManager.savePreference("assets", folder);

                                // Navigate to coupon verification
                                navigateToCouponVerification();
                            }
                        }
                    } catch (IOException | JSONException e) {
                        handleApiError(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleApiError(t.getMessage());
                // Still navigate to next screen on failure
                navigateToCouponVerification();
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
     * Navigate to registration screen
     */
    private void navigateToRegistration() {
        // Intent intent = new Intent(this, RegistrationActivity.class);
        // startActivity(intent);

        // Just show toast for now
        Toast.makeText(this, "Registration not implemented yet", Toast.LENGTH_SHORT).show();
    }

    /**
     * Navigate to coupon verification screen
     */
    private void navigateToCouponVerification() {
        Intent intent = new Intent(this, CouponVerificationActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Close app on back press from login screen
            finishAffinity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}