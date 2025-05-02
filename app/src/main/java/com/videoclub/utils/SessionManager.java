package com.videoclub.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Session manager to handle token storage and preferences
 */
public class SessionManager {
    private static final String PREF_NAME = "VideoClubPrefs";
    private static final String KEY_TOKEN = "token";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Save authentication token
     */
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    /**
     * Get saved token
     */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    /**
     * Clear all session data (logout)
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }


    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_APP_TYPE = "app_type";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_APP_BG_COLOR = "appbgcolor";
    private static final String KEY_APP_TEXT_COLOR = "apptextcolor";
    private static final String KEY_UNIQUE_ID = "uniqueid";
    private static final String KEY_COUPON_VERIFY = "couponVerify";



    /**
     * Save authentication token
     */


    /**
     * Get saved token
     */

    /**
     * Save user ID
     */
    public void saveUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    /**
     * Get user ID
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    /**
     * Save app type
     */
    public void saveAppType(String appType) {
        editor.putString(KEY_APP_TYPE, appType);
        editor.apply();
    }

    /**
     * Get app type
     */
    public String getAppType() {
        return prefs.getString(KEY_APP_TYPE, "");
    }

    /**
     * Save user credentials for "Remember Me" functionality
     */
    public void saveUserCredentials(String email, String password, boolean remember) {
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_REMEMBER, remember ? "yes" : "");
        editor.apply();
    }

    /**
     * Check if "Remember Me" is enabled
     */
    public boolean isRememberMeEnabled() {
        return "yes".equals(prefs.getString(KEY_REMEMBER, ""));
    }

    /**
     * Get saved email
     */
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    /**
     * Get saved password
     */
    public String getPassword() {
        return prefs.getString(KEY_PASSWORD, "");
    }

    /**
     * Save app background color
     */
    public void saveAppBgColor(String color) {
        editor.putString(KEY_APP_BG_COLOR, color);
        editor.apply();
    }

    /**
     * Get app background color
     */
    public String getAppBgColor() {
        return prefs.getString(KEY_APP_BG_COLOR, "#FFFFFF");
    }

    /**
     * Save app text color
     */
    public void saveAppTextColor(String color) {
        editor.putString(KEY_APP_TEXT_COLOR, color);
        editor.apply();
    }

    /**
     * Get app text color
     */
    public String getAppTextColor() {
        return prefs.getString(KEY_APP_TEXT_COLOR, "#000000");
    }

    /**
     * Save unique ID
     */
    public void saveUniqueId(String uniqueId) {
        editor.putString(KEY_UNIQUE_ID, uniqueId);
        editor.apply();
    }

    /**
     * Get unique ID
     */
    public String getUniqueId() {
        return prefs.getString(KEY_UNIQUE_ID, "");
    }

    /**
     * Save coupon verification status
     */
    public void saveCouponVerify(String status) {
        editor.putString(KEY_COUPON_VERIFY, status);
        editor.apply();
    }

    /**
     * Get coupon verification status
     */
    public String getCouponVerify() {
        return prefs.getString(KEY_COUPON_VERIFY, "");
    }

    /**
     * Save a generic preference
     */
    public void savePreference(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Get a generic preference
     */
    public String getPreference(String key) {
        return prefs.getString(key, "");
    }

}