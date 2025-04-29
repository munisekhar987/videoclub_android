package com.videoclub;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.videoclub.adapters.ChannelAdapter;
import com.videoclub.models.Channel;
import com.videoclub.utils.Images;

import java.util.ArrayList;
import java.util.List;

public class ChannelSubcategoryScreenActivity extends FragmentActivity {

    private ImageView backButton;
    private TextView titleText;
    private RecyclerView channelsRecyclerView;
    private Channel channelData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_subcategory_screen);

        // Initialize views
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        channelsRecyclerView = findViewById(R.id.channels_recycler_view);

        // Get channel data from intent
        if (getIntent() != null && getIntent().getParcelableExtra("data") != null) {
            channelData = getIntent().getParcelableExtra("data");
            titleText.setText(channelData.getTitle());
        } else {
            Toast.makeText(this, "Channel data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup back button
        backButton.setOnClickListener(v -> finish());

        // Setup RecyclerView with dummy data (you'd fetch real data here)
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        // This is just a placeholder implementation
        // In a real app, you would fetch subcategory data for the channel
        List<Channel> dummyList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dummyList.add(new Channel(
                    String.valueOf(i),
                    "Subcategory " + i,
                    ""
            ));
        }

        ChannelAdapter adapter = new ChannelAdapter(this, dummyList);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        channelsRecyclerView.setLayoutManager(layoutManager);
        channelsRecyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(channel -> {
            // Handle subcategory selection
            Toast.makeText(this, "Selected: " + channel.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }
}