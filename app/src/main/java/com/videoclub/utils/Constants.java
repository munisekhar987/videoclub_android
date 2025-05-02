package com.videoclub.utils;

/**
 * Contains all application constants including URLs, preferences keys, and display types.
 */
public class Constants {
    // Preferences name
    public static final String PREFS_NAME = "VideoClubPrefs";

    // API and Media URLs
    public static final String API_BASE_URL = "http://api.msgnaa.info/";
    public static final String MEDIA_BASE_URL = "http://pmedia.msgnaa.info:8082/";
    public static final String IMAGE_PATH_XXXHDPI = MEDIA_BASE_URL + "images/apk/xxxhdpi/";
    public static String assetsUrl = "";

    // Preferences keys
    public static final String TOKEN_KEY = "token";
    public static final String PREF_TOKEN = "token";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_APP_TYPE = "app_type";
    public static final String PREF_UNIQUE_ID = "uniqueid";
    public static final String PREF_COUPON_VERIFY = "couponVerify";
    public static final String PREF_APP_BG_COLOR = "appbgcolor";
    public static final String PREF_APP_TEXT_COLOR = "apptextcolor";
    public static final String PREF_USER_DATA = "userData";
    public static final String PREF_ASSETS = "assets";

    // Display types
    public static final int DISPLAY_TYPE_PREMIUM = 11;
    public static final int DISPLAY_TYPE_TV = 22;
    public static final int DISPLAY_TYPE_KARAOKE = 33;

    // Timeouts
    public static final long SESSION_TIMEOUT = 1800000; // 30 minutes in milliseconds
    public static final long SPLASH_DISPLAY_TIME = 3000; // 3 seconds in milliseconds

    // Default values
    public static final String DEFAULT_MAC_ADDRESS = "02:00:00:00:00:00";
    public static final String WEB_VIEW_USER_AGENT = "igoTVExoplayerVideo-02:00:00:00:00:00";
}