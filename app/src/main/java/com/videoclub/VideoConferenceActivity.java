package com.videoclub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

/**
 * VideoConferenceActivity for Android TV
 * Handles video conferencing setup and joining meetings
 */
public class VideoConferenceActivity extends FragmentActivity {
    private static final String TAG = "VideoConferenceActivity";

    // UI components
    private EditText roomEditText;
    private Button joinButton;
    private ProgressBar progressBar;
    private TextView titleTextView;

    // Default Jitsi URL if none is provided
    private static final String DEFAULT_JITSI_URL = "https://meet.jit.si";

    private String jitsiUrl;
    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_conference);

        // Initialize views
        roomEditText = findViewById(R.id.room_edit_text);
        joinButton = findViewById(R.id.join_button);
        progressBar = findViewById(R.id.progress_bar);
        titleTextView = findViewById(R.id.title_text);

        // Get data from intent
        if (getIntent() != null && getIntent().getExtras() != null) {
            String url = getIntent().getStringExtra("url");

            // Parse URL to extract Jitsi server and room ID if available
            if (url != null && !url.isEmpty()) {
                parseUrl(url);
            }
        }

        // If no URL was provided or parsing failed, use defaults
        if (jitsiUrl == null || jitsiUrl.isEmpty()) {
            jitsiUrl = DEFAULT_JITSI_URL;
        }

        // Pre-fill room ID if it was extracted
        if (roomId != null && !roomId.isEmpty()) {
            roomEditText.setText(roomId);
        }

        // Set up join button click listener
        joinButton.setOnClickListener(v -> joinMeeting());

        // Set initial focus
        if (roomId == null || roomId.isEmpty()) {
            roomEditText.requestFocus();
        } else {
            joinButton.requestFocus();
        }
    }

    /**
     * Parse the URL to extract Jitsi server and room ID
     */
    private void parseUrl(String url) {
        try {
            Uri uri = Uri.parse(url);

            // Extract host as Jitsi URL
            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (scheme != null && host != null) {
                jitsiUrl = scheme + "://" + host;

                // Extract room ID from path
                String path = uri.getPath();
                if (path != null && path.length() > 1) {
                    // Remove leading slash
                    roomId = path.substring(1);
                }
            }
        } catch (Exception e) {
            // If parsing fails, use default values
        }
    }

    /**
     * Join the meeting
     */
    private void joinMeeting() {
        // Get room ID from EditText
        String room = roomEditText.getText().toString().trim();

        if (room.isEmpty()) {
            Toast.makeText(this, "Please enter a room ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        joinButton.setEnabled(false);

        // Launch JoinMeetingActivity
        Intent intent = new Intent(this, JoinMeetingActivity.class);
        intent.putExtra("room", room);
        intent.putExtra("jitsiUrl", jitsiUrl);
        startActivity(intent);

        // We'll finish this activity after a delay to allow the Jitsi session to start
        joinButton.postDelayed(() -> {
            finish();
        }, 1000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}