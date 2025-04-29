package com.videoclub;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.BrowseSupportFragment;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * CheckStatusActivity for Android TV
 * Converted from React Native CheckStatusScreen
 */
public class CheckStatusActivity extends FragmentActivity {
    private static final String TAG = "CheckStatusActivity";
    private static final int REQUEST_BLUETOOTH_PERMISSION = 101;

    private TextView deviceIdTextView;
    private ImageView logoImageView;
    private Button enableBluetoothButton;

    private String uniqueId;
    private String macAddress = "02:00:00:00:00:00";
    private String ipAddress = "";
    private String networkType = "";
    private boolean isNetworkConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_status);

        // Initialize views
        logoImageView = findViewById(R.id.logo_image);
        deviceIdTextView = findViewById(R.id.device_id_text);
        enableBluetoothButton = findViewById(R.id.enable_bluetooth_button);

        // Set up logo
        logoImageView.setImageResource(R.drawable.newlogo);

        // Set up button click listener
        enableBluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableBluetooth();
            }
        });

        // Get device info
        getDeviceInformation();
        checkNetworkStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh network status when activity resumes
        checkNetworkStatus();
    }

    /**
     * Get device unique identifier and other information
     */
    private void getDeviceInformation() {
        // Get unique device ID
        uniqueId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        deviceIdTextView.setText("Device ID: " + uniqueId);

        // Get IP and MAC address
        getMacAddress();
        getIpAddress();
    }

    /**
     * Get device MAC address with appropriate permission handling
     */
    private void getMacAddress() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For Android 6.0+, we need to get MAC address from network interfaces
                List<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface networkInterface : networkInterfaces) {
                    if (!networkInterface.getName().equalsIgnoreCase("wlan0")) continue;

                    byte[] macBytes = networkInterface.getHardwareAddress();
                    if (macBytes == null) {
                        macAddress = "02:00:00:00:00:00"; // Default MAC for when unavailable
                        continue;
                    }

                    StringBuilder macAddressBuilder = new StringBuilder();
                    for (byte b : macBytes) {
                        macAddressBuilder.append(String.format("%02X:", b));
                    }

                    if (macAddressBuilder.length() > 0) {
                        macAddressBuilder.deleteCharAt(macAddressBuilder.length() - 1);
                    }

                    macAddress = macAddressBuilder.toString();
                    break;
                }
            } else {
                // For older Android versions
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                macAddress = wifiInfo.getMacAddress();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting MAC address: " + e.getMessage());
            macAddress = "02:00:00:00:00:00"; // Default fallback
        }

        Log.d(TAG, "MAC Address: " + macAddress);
    }

    /**
     * Get device IP address
     */
    private void getIpAddress() {
        try {
            // Get WiFi service and info
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            // Convert IP integer to string format
            int ipInt = wifiInfo.getIpAddress();
            ipAddress = String.format("%d.%d.%d.%d",
                    (ipInt & 0xff),
                    (ipInt >> 8 & 0xff),
                    (ipInt >> 16 & 0xff),
                    (ipInt >> 24 & 0xff));

            Log.d(TAG, "IP Address: " + ipAddress);
        } catch (Exception e) {
            Log.e(TAG, "Error getting IP address: " + e.getMessage());
            ipAddress = "0.0.0.0";
        }
    }

    /**
     * Check network connection status
     */
    private void checkNetworkStatus() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        isNetworkConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isNetworkConnected && activeNetwork != null) {
            networkType = activeNetwork.getTypeName();
            Log.d(TAG, "Network connected. Type: " + networkType);
        } else {
            networkType = "Not connected";
            Log.d(TAG, "Network not connected");
        }
    }

    /**
     * Enable Bluetooth if available and with appropriate permissions
     */
    private void enableBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check for Bluetooth permissions on newer Android versions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                        REQUEST_BLUETOOTH_PERMISSION);
                return;
            }
        }

        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBtIntent);
                Log.d(TAG, "Bluetooth enable request sent");
            } else if (bluetoothAdapter == null) {
                Log.d(TAG, "Device does not support Bluetooth");
            } else {
                Log.d(TAG, "Bluetooth is already enabled");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error enabling Bluetooth: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable Bluetooth
                enableBluetooth();
            } else {
                Log.d(TAG, "Bluetooth permission denied");
            }
        }
    }
}