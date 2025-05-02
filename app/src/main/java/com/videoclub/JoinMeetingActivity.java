package com.videoclub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

import androidx.fragment.app.FragmentActivity;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.net.MalformedURLException;
import java.net.URL;

import timber.log.Timber;

/**
 * JoinMeetingActivity for Android TV
 * This activity handles launching the Jitsi Meet SDK for video conferencing
 */
public class JoinMeetingActivity extends FragmentActivity {
    private static final String TAG = "JoinMeetingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_meeting);

        // Get meeting parameters from intent
        String room = getIntent().getStringExtra("room");
        String jitsiUrl = getIntent().getStringExtra("jitsiUrl");

        if (room == null || room.isEmpty()) {
            room = "defaultRoom";
        }

        if (jitsiUrl == null || jitsiUrl.isEmpty()) {
            jitsiUrl = "https://meet.jit.si";
        }

        // Launch Jitsi meeting
        try {
            JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
            userInfo.setDisplayName("Android TV User");

            URL serverURL = new URL(jitsiUrl);
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(serverURL)
                    .setRoom(room)
                    .setUserInfo(userInfo)
                    .setFeatureFlag("pip.enabled", false) // Disable PIP to prevent issues
                    .setFeatureFlag("chat.enabled", false) // Simplify UI for TV
                    .setFeatureFlag("invite.enabled", false) // Simplify UI for TV
                    .setFeatureFlag("recording.enabled", false) // Simplify UI for TV
                    .setFeatureFlag("live-streaming.enabled", false) // Simplify UI for TV
                    .setFeatureFlag("toolbox.enabled", true)
                    .setFeatureFlag("fullscreen.enabled", true)
                    .build();

            JitsiMeetActivity.launch(this, options);

            // Set a delayed navigation back to home screen when meeting is closed
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Check if activity is still active before navigating
                    if (!isFinishing() && !isDestroyed()) {
                        navigateToHomeScreen();
                    }
                }
            }, 500); // Small delay to ensure Jitsi is launched

        } catch (MalformedURLException e) {
            Log.e(TAG, "Invalid server URL", e);
            navigateToHomeScreen();
        }
    }

    /**
     * Navigate back to the home screen
     */
    private void navigateToHomeScreen() {
        Intent intent = new Intent(JoinMeetingActivity.this, NewHomeScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            navigateToHomeScreen();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ensure we navigate back to home screen
        navigateToHomeScreen();
    }
}