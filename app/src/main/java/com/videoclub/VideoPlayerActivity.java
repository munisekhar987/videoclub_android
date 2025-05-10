package com.videoclub;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.videoclub.models.Channel;
import com.videoclub.models.KaraokeItem;
import com.videoclub.utils.AudioRecorder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class VideoPlayerActivity extends FragmentActivity {

    private static final String TAG = "VideoPlayerActivity";
    private static final String PREFS_NAME = "MembershipPrefs";
    private static final String TOKEN_KEY = "token";
    private static final String KARAOKE_RECORDS_KEY = "karaoke_listing";

    // UI Elements
    private VideoPlayerView videoPlayerView;
    private TextView titleTextView;
    private ProgressBar progressBar;
    private ImageView backButton;
    private TextView recordingButton;
    private TextView noDataTextView;

    // Media and Recording
    private String videoUrl;
    private String audioPath;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private AudioRecorder audioRecorder;
    private MediaPlayer audioPlayer;
    private long recordingStartTime = 0;
    private float currentVideoPosition = 0;
    private String videoName = "";
    private android.os.Handler handler = new android.os.Handler();

    // State data
    private String token;
    private String displayType;
    private Channel channelData;
    private KaraokeItem karaokeData;
    private boolean isKaraokeMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // Initialize views
        videoPlayerView = findViewById(R.id.video_player_view);
        titleTextView = findViewById(R.id.title_text);
        progressBar = findViewById(R.id.progress_bar);
        backButton = findViewById(R.id.back_button);
        recordingButton = findViewById(R.id.recording_button);
        noDataTextView = findViewById(R.id.no_data_text);

        // Get token
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        token = prefs.getString(TOKEN_KEY, "");

        // Setup click listeners
        setupClickListeners();

        // Process the intent data
        processIntentData();
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isKaraokeMode) {
                    if (isRecording) {
                        stopRecording();
                    } else {
                        startRecording();
                    }
                }
            }
        });

        videoPlayerView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (audioPlayer != null && audioPlayer.isPlaying()) {
                    audioPlayer.stop();
                    audioPlayer.release();
                    audioPlayer = null;
                }
            }
        });
    }

    private void processIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            displayType = intent.getStringExtra("displayType");

            if (displayType != null) {
                switch (displayType) {
                    case "1": // Premium
                        channelData = intent.getParcelableExtra("detailContent");
                        if (channelData != null) {
                            processPremiumChannel();
                        }
                        break;
                    case "2": // TV Channels
                        channelData = intent.getParcelableExtra("detailContent");
                        if (channelData != null) {
                            processTVChannel();
                        }
                        break;
                    case "4": // Karaoke
                        isKaraokeMode = true;
                        karaokeData = intent.getParcelableExtra("detailContent");
                        if (karaokeData != null) {
                            videoName = karaokeData.getVideoName();
                            processKaraokeVideo();
                            recordingButton.setVisibility(View.VISIBLE);
                            recordingButton.setText(R.string.start_recording);
                        }
                        break;
                    default:
                        showNoDataMessage();
                        break;
                }
            } else {
                showNoDataMessage();
            }

            // If there's an audio URL passed (for playback of recorded karaoke)
            if (intent.hasExtra("audioUrl")) {
                String audioUrl = intent.getStringExtra("audioUrl");
                float startTime = intent.getFloatExtra("startRecordTime", 0);
                playRecordedAudio(audioUrl, startTime);

                // We disable recording controls when playing a recording
                recordingButton.setVisibility(View.GONE);
            }
        } else {
            showNoDataMessage();
        }
    }

    private void processPremiumChannel() {
        // Since your Channel model doesn't have getData() method, we need to adjust the approach
        // We'll use the data directly from the Channel object
        if (channelData != null) {
            String liveUrl = channelData.getVideoUrl();
            if (liveUrl != null && liveUrl.contains("?notoken")) {
                // For this case, we'll have to make assumptions or adjust the code
                // based on your actual app implementation
                setVideoUrl(channelData.getImageUrl()); // Using imageUrl as fallback
            } else {
                setVideoUrl(channelData.getVideoUrl());
            }
        } else {
            showNoDataMessage();
        }
    }

    private void processTVChannel() {
        String liveUrl = channelData.getVideoUrl();
        if (liveUrl != null && !liveUrl.endsWith("?notoken")) {
            setVideoUrl(liveUrl);
        } else {
            Toast.makeText(this, getString(R.string.app_name) + ": No Video url found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void processKaraokeVideo() {
        // Since your KaraokeItem model doesn't have getCodec() method,
        // we'll use the videoUrl directly
        String videoUrl = karaokeData.getVideoUrl();
        if (videoUrl != null && !videoUrl.isEmpty()) {
            // Based on your React code, for karaoke it usually prepends a URL
            String fullUrl = "http://pedge.msgnaa.info:8081/" + videoUrl;
            setVideoUrl(fullUrl);
        } else {
            showNoDataMessage();
        }
    }

    private void setVideoUrl(String url) {
        this.videoUrl = url;

        // Setting up the video player - adjust these based on your VideoPlayerView implementation
        videoPlayerView.setVideoURI(Uri.parse(videoUrl));
        videoPlayerView.setMediaController(true);
        videoPlayerView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                progressBar.setVisibility(View.GONE);
                videoPlayerView.start();

                // When we're in karaoke mode, we might start recording or playing
                if (isKaraokeMode && audioPlayer != null) {
                    // This would be for playback of a recorded session
                    videoPlayerView.setOnTimeUpdateListener(new VideoPlayerView.OnTimeUpdateListener() {
                        @Override
                        public void onTimeUpdate(float currentTime) {
                            currentVideoPosition = currentTime;
                            if (recordingStartTime > 0 && currentTime >= recordingStartTime && !isPlaying) {
                                playAudioAtCurrentPosition();
                            }
                        }
                    });
                }
            }
        });

        videoPlayerView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                if (percent < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void startRecording() {
        try {
            // Check permissions - in a real app this would be done more thoroughly
            // with runtime permission checks

            // Initialize audio recorder
            audioRecorder = new AudioRecorder();
            audioPath = getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/sound.mp4";
            audioRecorder.startRecording(audioPath);
            isRecording = true;
            recordingButton.setText(R.string.save_recording);

            // Save current video position as start time
            recordingStartTime = (long) videoPlayerView.getCurrentPosition();
        } catch (Exception e) {
            Log.e(TAG, "Error starting recording", e);
            Toast.makeText(this, "Error starting recording", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (audioRecorder != null) {
            audioRecorder.stopRecording();

            // Generate a new file name and copy the recording
            String newFileName = "new_sound_" + System.currentTimeMillis() + ".mp4";
            String newPath = getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + newFileName;

            try {
                // Copy file to a new location with unique name - using pre-API 26 compatible method
                copyFile(audioPath, newPath);

                // Save recording metadata to shared preferences
                saveRecordingMetadata(newPath);

                Toast.makeText(this, "Recording saved successfully", Toast.LENGTH_SHORT).show();
                finish(); // Return to previous screen

            } catch (IOException e) {
                Log.e(TAG, "Error saving recording", e);
                Toast.makeText(this, "Error saving recording", Toast.LENGTH_SHORT).show();
            }

            isRecording = false;
            recordingButton.setText(R.string.start_recording);
        }
    }

    private void copyFile(String sourcePath, String destPath) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            File sourceFile = new File(sourcePath);
            File destFile = new File(destPath);

            if (!destFile.exists()) {
                destFile.createNewFile();
            }

            sourceChannel = new FileInputStream(sourceFile).getChannel();
            destChannel = new FileOutputStream(destFile).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            if (sourceChannel != null) {
                sourceChannel.close();
            }
            if (destChannel != null) {
                destChannel.close();
            }
        }
    }

    private void saveRecordingMetadata(String filePath) {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String recordsJson = prefs.getString(KARAOKE_RECORDS_KEY, "[]");
            JSONArray recordsArray = new JSONArray(recordsJson);

            JSONObject newRecord = new JSONObject();
            newRecord.put("video_id", UUID.randomUUID().toString());
            newRecord.put("file", filePath);
            newRecord.put("record_sec", videoPlayerView.getCurrentPosition());

            // Format time as 00:00:00
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
            newRecord.put("record_time", sdf.format(new Date(videoPlayerView.getCurrentPosition())));

            newRecord.put("startRecordTime", recordingStartTime);
            newRecord.put("category", videoName);

            recordsArray.put(newRecord);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KARAOKE_RECORDS_KEY, recordsArray.toString());
            editor.apply();

        } catch (JSONException e) {
            Log.e(TAG, "Error saving recording metadata", e);
        }
    }

    private void playRecordedAudio(String audioUrl, float startTime) {
        this.audioPath = audioUrl;
        this.recordingStartTime = (long) (startTime * 1000); // Convert to milliseconds

        // We'll setup the audio player but wait for the video to reach the start time
        try {
            audioPlayer = new MediaPlayer();
            audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            audioPlayer.setDataSource(audioPath);
            audioPlayer.prepare();

            // Play audio at the right time based on video position
            videoPlayerView.setOnTimeUpdateListener(new VideoPlayerView.OnTimeUpdateListener() {
                @Override
                public void onTimeUpdate(float currentTime) {
                    if (currentTime >= recordingStartTime && !isPlaying) {
                        playAudioAtCurrentPosition();
                    }
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "Error preparing audio playback", e);
        }
    }

    private void playAudioAtCurrentPosition() {
        if (audioPlayer != null && !isPlaying) {
            try {
                // Play from the right position
                long offsetTime = (long) (currentVideoPosition - recordingStartTime);
                if (offsetTime > 0) {
                    audioPlayer.seekTo((int) offsetTime);
                }

                audioPlayer.start();
                isPlaying = true;

                // Sync with video player controls
                videoPlayerView.setOnPauseListener(new VideoPlayerView.OnPauseListener() {
                    @Override
                    public void onPause() {
                        if (audioPlayer.isPlaying()) {
                            audioPlayer.pause();
                        }
                    }
                });

                videoPlayerView.setOnResumeListener(new VideoPlayerView.OnResumeListener() {
                    @Override
                    public void onResume() {
                        if (!audioPlayer.isPlaying()) {
                            audioPlayer.start();
                        }
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error playing audio", e);
            }
        }
    }

    private void showNoDataMessage() {
        videoPlayerView.setVisibility(View.GONE);
        noDataTextView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isRecording) {
                stopRecording();
            }
            if (audioPlayer != null) {
                audioPlayer.stop();
                audioPlayer.release();
                audioPlayer = null;
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (audioRecorder != null && isRecording) {
            audioRecorder.stopRecording();
        }
        if (audioPlayer != null) {
            audioPlayer.release();
            audioPlayer = null;
        }
        super.onDestroy();
    }
}