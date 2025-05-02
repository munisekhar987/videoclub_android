package com.videoclub;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.videoclub.utilities.Colors;
import com.videoclub.utilities.Images;

/**
 * HomeDetailActivity - Shows movie details when a user clicks on a movie
 */
public class HomeDetailActivity extends FragmentActivity {
    private ImageView coverImageView;
    private TextView titleTextView;
    private TextView uploaderTextView;
    private Button playButton;
    private ImageView backButton;

    private String movieId;
    private String movieName;
    private String coverImageUrl;
    private String uploadAccount;
    private String displayType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_detail);

        // Initialize views
        coverImageView = findViewById(R.id.cover_image);
        titleTextView = findViewById(R.id.movie_title);
        uploaderTextView = findViewById(R.id.uploader_name);
        playButton = findViewById(R.id.play_button);
        backButton = findViewById(R.id.back_button);

        // Get data from intent
        if (getIntent() != null && getIntent().getExtras() != null) {
            displayType = getIntent().getStringExtra("displayType");
            movieId = getIntent().getStringExtra("movieId");
            movieName = getIntent().getStringExtra("movieName");
            coverImageUrl = getIntent().getStringExtra("coverImage");
            uploadAccount = getIntent().getStringExtra("uploadAccount");

            // Set UI elements
            titleTextView.setText(movieName);

            if (uploadAccount != null && !uploadAccount.isEmpty()) {
                uploaderTextView.setVisibility(View.VISIBLE);
                uploaderTextView.setText(uploadAccount);
            } else {
                uploaderTextView.setVisibility(View.GONE);
            }

            // Load cover image
            Glide.with(this)
                    .load(coverImageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.empty_thumbnail)
                            .error(R.drawable.empty_thumbnail))
                    .into(coverImageView);
        }

        // Set up click listeners
        setupListeners();
    }

    /**
     * Set up UI click listeners
     */
    private void setupListeners() {
        // Play button
        playButton.setOnClickListener(v -> {
            if (displayType != null && displayType.equals("2")) {
                // This is a regular movie
                Intent intent = new Intent(HomeDetailActivity.this, VideoPlayerActivity.class);
                intent.putExtra("movieId", movieId);
                intent.putExtra("movieName", movieName);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Unsupported content type", Toast.LENGTH_SHORT).show();
            }
        });

        // Back button
        backButton.setOnClickListener(v -> finish());
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