package com.videoclub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;

import com.videoclub.utils.Images;
import com.videoclub.utils.SessionManager;

/**
 * SplashActivity - Displays splash screen and handles navigation to appropriate screen
 * Converted from React Native Splash component
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends FragmentActivity {
    private static final int SPLASH_TIMEOUT = 3000; // 3 seconds
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize session manager
        sessionManager = new SessionManager(this);

        // Set up logo image
        ImageView logoImageView = findViewById(R.id.logo_image);
        logoImageView.setImageResource(Images.LOGO);

        // Handler to delay navigation
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToNextScreen();
            }
        }, SPLASH_TIMEOUT);
    }

    /**
     * Navigates to the appropriate screen based on login status
     */
    private void navigateToNextScreen() {
        String token = sessionManager.getToken();

        if (token != null && !token.isEmpty()) {
            // User is logged in, check coupon verification status
            String couponVerified = sessionManager.getPreference("couponVerify");

            if ("yes".equals(couponVerified)) {
                // Coupon verified, go to home screen
                Intent intent = new Intent(SplashActivity.this, NewHomeScreenActivity.class);
                startActivity(intent);
            } else if ("no".equals(couponVerified) || couponVerified == null) {
                // Coupon not verified or verification failed, go to coupon verification
                Intent intent = new Intent(SplashActivity.this, CouponVerificationActivity.class);
                startActivity(intent);
            } else {
                // Default fallback
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        } else {
            // User is not logged in, go to login screen
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        // Close this activity
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Prevent back navigation from splash screen
        return true;
    }
}