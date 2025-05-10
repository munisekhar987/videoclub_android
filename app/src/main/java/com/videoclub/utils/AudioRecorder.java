package com.videoclub.utils;

import android.media.MediaRecorder;
import android.util.Log;
import java.io.IOException;

public class AudioRecorder {
    private static final String TAG = "AudioRecorder";
    private MediaRecorder recorder;

    public void startRecording(String outputFilePath) throws IOException {
        if (recorder != null) {
            stopRecording();
        }

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setOutputFile(outputFilePath);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            Log.e(TAG, "Failed to start recording", e);
            throw e;
        }
    }

    public void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping recorder", e);
            }
            recorder.release();
            recorder = null;
        }
    }
}