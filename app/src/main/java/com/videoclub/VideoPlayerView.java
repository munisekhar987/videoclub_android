package com.videoclub;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayerView extends VideoView {

    private MediaController mediaController;
    private OnTimeUpdateListener timeUpdateListener;
    private OnPauseListener pauseListener;
    private OnResumeListener resumeListener;
    private MediaPlayer.OnBufferingUpdateListener bufferingUpdateListener;
    private android.os.Handler handler = new android.os.Handler();
    private Runnable timeUpdateRunnable;

    public interface OnTimeUpdateListener {
        void onTimeUpdate(float currentTime);
    }

    public interface OnPauseListener {
        void onPause();
    }

    public interface OnResumeListener {
        void onResume();
    }

    public VideoPlayerView(Context context) {
        super(context);
        init();
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mediaController = new MediaController(getContext());
        mediaController.setAnchorView(this);
        setMediaController(mediaController);

        // Set up a listener for when the media player is prepared
        setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // Once the player is prepared, we can set our buffering listener
                if (bufferingUpdateListener != null) {
                    mp.setOnBufferingUpdateListener(bufferingUpdateListener);
                }
            }
        });
    }

    public void setUserAgent(String userAgent) {
        // This method doesn't do anything as Android VideoView doesn't support custom user agent
        // To implement this properly, you would need ExoPlayer or another video player library
    }

    public void setMediaController(boolean enabled) {
        if (enabled) {
            super.setMediaController(mediaController);
        } else {
            super.setMediaController(null);
        }
    }

    public void setOnTimeUpdateListener(OnTimeUpdateListener listener) {
        this.timeUpdateListener = listener;

        // Start time update tracking
        if (listener != null) {
            startTimeUpdates();
        } else {
            stopTimeUpdates();
        }
    }

    public void setOnBufferingUpdateListener(final MediaPlayer.OnBufferingUpdateListener listener) {
        this.bufferingUpdateListener = listener;

        // If we already have a MediaPlayer instance (i.e., we're prepared), set the listener immediately
        setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (listener != null) {
                    mp.setOnBufferingUpdateListener(listener);
                }
            }
        });
    }

    private void startTimeUpdates() {
        if (timeUpdateRunnable == null) {
            timeUpdateRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isPlaying() && timeUpdateListener != null) {
                        timeUpdateListener.onTimeUpdate(getCurrentPosition());
                    }
                    handler.postDelayed(this, 100); // Update every 100ms
                }
            };
            handler.post(timeUpdateRunnable);
        }
    }

    private void stopTimeUpdates() {
        if (timeUpdateRunnable != null) {
            handler.removeCallbacks(timeUpdateRunnable);
            timeUpdateRunnable = null;
        }
    }

    public void setOnPauseListener(OnPauseListener listener) {
        this.pauseListener = listener;
    }

    public void setOnResumeListener(OnResumeListener listener) {
        this.resumeListener = listener;
    }

    @Override
    public void pause() {
        super.pause();
        if (pauseListener != null) {
            pauseListener.onPause();
        }
    }

    @Override
    public void start() {
        super.start();
        if (resumeListener != null) {
            resumeListener.onResume();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        stopTimeUpdates();
        super.onDetachedFromWindow();
    }
}