package com.videoclub;

import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.leanback.media.PlaybackTransportControlGlue;
import androidx.leanback.widget.PlaybackControlsRow;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.videoclub.utilities.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * ChannelVideoPlayerActivity for Android TV
 * Optimized for modern Android TV interface using Leanback libraries
 */
public class ChannelVideoPlayerActivity extends FragmentActivity {
    private static final String TAG = "ChannelVideoPlayerActivity";

    // UI components
    private PlayerView playerView;
    private ProgressBar bufferingProgressBar;
    private TextView errorMessageView;

    // Player components
    private ExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    // Controls configuration
    private boolean disableFullscreen = false;
    private boolean disablePlayPause = false;
    private boolean disableSeekbar = false;
    private boolean disableVolume = false;
    private boolean disableTimer = false;

    // Content
    private String channelUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_video_player);

        // Initialize views
        playerView = findViewById(R.id.player_view);
        bufferingProgressBar = findViewById(R.id.buffering_progress_bar);
        errorMessageView = findViewById(R.id.error_message);

        // Get channel URL from intent extras
        if (getIntent() != null && getIntent().getExtras() != null) {
            channelUrl = getIntent().getStringExtra("channelUrl");
        }

        // Configure player view for TV
        configurePlayerForTV();

        // Set background color
        getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.colorBlack, null));

        // Configure control visibility based on settings
        configurePlayerControls();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUI();
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Enhanced key handling for TV remote
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (player != null) {
                    player.setPlayWhenReady(!player.getPlayWhenReady());
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (player != null && !disableSeekbar) {
                    long newPosition = player.getCurrentPosition() - 10000; // Rewind 10 seconds
                    player.seekTo(Math.max(0, newPosition));
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (player != null && !disableSeekbar) {
                    long newPosition = player.getCurrentPosition() + 10000; // Forward 10 seconds
                    long duration = player.getDuration();
                    player.seekTo(Math.min(duration, newPosition));
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                // Let the system handle volume keys
                return super.onKeyDown(keyCode, event);
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * Configure player specifically for TV display
     */
    private void configurePlayerForTV() {
        // Configure for TV focus handling
        playerView.setControllerAutoShow(true);
        playerView.setControllerShowTimeoutMs(5000); // 5 seconds before controls hide
        playerView.setUseController(true);

        // TV-specific focus handling
        playerView.setDefaultFocusHighlightEnabled(true);
        playerView.setFocusable(true);

        // Prevent controller hide on touch (TV doesn't use touch)
        playerView.setControllerHideOnTouch(false);
    }

    /**
     * Initialize the ExoPlayer
     */
    private void initializePlayer() {
        if (channelUrl == null || channelUrl.isEmpty()) {
            showErrorMessage(getString(R.string.no_channel_url));
            return;
        }

        // Create ExoPlayer instance with modern builder pattern
        player = new ExoPlayer.Builder(this)
                .setSeekBackIncrementMs(10000) // 10 seconds for seek back
                .setSeekForwardIncrementMs(10000) // 10 seconds for seek forward
                .build();

        // Attach player to view
        playerView.setPlayer(player);

        // Set player properties
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);

        // Create HTTP data source factory with custom headers
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", Constants.WEB_VIEW_USER_AGENT);

        DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                .setDefaultRequestProperties(headers)
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(15000)
                .setAllowCrossProtocolRedirects(true);

        // Create a data source factory
        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(
                this, httpDataSourceFactory);

        // Create a media source using the channel URL
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(channelUrl)));

        // Prepare the player with the media source
        player.setMediaSource(mediaSource);
        player.prepare();

        // Add player listeners
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_BUFFERING) {
                    bufferingProgressBar.setVisibility(View.VISIBLE);
                } else {
                    bufferingProgressBar.setVisibility(View.GONE);
                }

                if (state == Player.STATE_READY) {
                    errorMessageView.setVisibility(View.GONE);
                }

                if (state == Player.STATE_ENDED) {
                    // Video ended, handle as needed
                    finish();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                showErrorMessage(getString(R.string.player_error) + ": " + error.getMessage());
                bufferingProgressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Release the player resources
     */
    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentMediaItemIndex();
            player.release();
            player = null;
        }
    }

    /**
     * Hide system UI for immersive experience
     */
    private void hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(),
                getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    /**
     * Configure player control visibility based on settings
     */
    private void configurePlayerControls() {
        if (disableFullscreen) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        } else {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        }

        // Access the controller and configure buttons
        playerView.setShowNextButton(false);
        playerView.setShowPreviousButton(false);
        playerView.setShowFastForwardButton(!disableSeekbar);
        playerView.setShowRewindButton(!disableSeekbar);

        // Set controller visibility listener to manage custom controls
        playerView.setControllerVisibilityListener(visibility -> {
            if (visibility == View.VISIBLE) {
                configureVisibleControls();
            }
        });
    }

    /**
     * Configure visible controls when controller is shown
     */
    private void configureVisibleControls() {
        // Find control views by ID
        View playPauseButton = playerView.findViewById(com.google.android.exoplayer2.ui.R.id.exo_play_pause);
        View timeView = playerView.findViewById(com.google.android.exoplayer2.ui.R.id.exo_position);

        // Configure visibility based on settings
        if (playPauseButton != null) {
            playPauseButton.setVisibility(disablePlayPause ? View.GONE : View.VISIBLE);

            // Ensure play/pause button is focusable for TV remote
            playPauseButton.setFocusable(true);
            playPauseButton.setDefaultFocusHighlightEnabled(true);
        }

        if (timeView != null) {
            timeView.setVisibility(disableTimer ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Show error message to user
     */
    private void showErrorMessage(String message) {
        errorMessageView.setText(message);
        errorMessageView.setVisibility(View.VISIBLE);
        playerView.setVisibility(View.GONE);
    }
}