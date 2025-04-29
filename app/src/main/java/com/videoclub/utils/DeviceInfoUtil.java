package com.videoclub.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for getting device information
 * Similar to the DeviceInfo functionality in React Native
 */
public class DeviceInfoUtil {
    private static final String TAG = "DeviceInfoUtil";

    /**
     * Get device unique identifier
     *
     * @param context Application context
     * @return String unique identifier
     */
    public static String getUniqueId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Get device IP address
     *
     * @param context Application context
     * @return String IP address or "0.0.0.0" if not available
     */
    public static String getIpAddress(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            int ipInt = wifiInfo.getIpAddress();
            return String.format("%d.%d.%d.%d",
                    (ipInt & 0xff),
                    (ipInt >> 8 & 0xff),
                    (ipInt >> 16 & 0xff),
                    (ipInt >> 24 & 0xff));
        } catch (Exception e) {
            Log.e(TAG, "Error getting IP address: " + e.getMessage());
            return "0.0.0.0";
        }
    }

    /**
     * Get device MAC address
     *
     * @param context Application context
     * @return String MAC address or default value if not available
     */
    public static String getMacAddress(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For Android 6.0+, get MAC address from network interfaces
                List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface networkInterface : networkInterfaces) {
                    if (!networkInterface.getName().equalsIgnoreCase("wlan0")) continue;

                    byte[] macBytes = networkInterface.getHardwareAddress();
                    if (macBytes == null) {
                        return "02:00:00:00:00:00"; // Default MAC for when unavailable
                    }

                    StringBuilder macAddressBuilder = new StringBuilder();
                    for (byte b : macBytes) {
                        macAddressBuilder.append(String.format("%02X:", b));
                    }

                    if (macAddressBuilder.length() > 0) {
                        macAddressBuilder.deleteCharAt(macAddressBuilder.length() - 1);
                    }

                    return macAddressBuilder.toString();
                }
                return "02:00:00:00:00:00";
            } else {
                // For older Android versions
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                return wifiInfo.getMacAddress();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting MAC address: " + e.getMessage());
            return "02:00:00:00:00:00";
        }
    }

    /**
     * Get network connection type
     *
     * @param context Application context
     * @return String network connection type
     */
    public static String getNetworkType(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            return activeNetwork.getTypeName();
        }

        return "Not connected";
    }

    /**
     * Check if device is connected to network
     *
     * @param context Application context
     * @return boolean true if connected
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Get WiFi BSSID if available
     *
     * @param context Application context
     * @return String BSSID or empty string if not available
     */
    public static String getBSSID(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            if (wifiInfo != null) {
                return wifiInfo.getBSSID();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting BSSID: " + e.getMessage());
        }

        return "";
    }
}