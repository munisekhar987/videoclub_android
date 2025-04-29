package com.videoclub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.videoclub.adapters.ChannelCategoryAdapter;
import com.videoclub.models.ChannelCategory;
import com.videoclub.utils.Constants;
import com.videoclub.utils.Images;
import com.videoclub.utils.RestClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChannelDetailScreenActivity extends FragmentActivity {

    private ImageView backButton;
    private TextView titleText;
    private RecyclerView channelsRecyclerView;
    private TextView noDataText;
    private String channelId;
    private List<ChannelCategory> channelsList = new ArrayList<>();
    private ChannelCategoryAdapter channelAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_detail_screen);

        // Initialize views
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        channelsRecyclerView = findViewById(R.id.channels_recycler_view);
        noDataText = findViewById(R.id.no_data_text);

        // Get channel ID from intent
        if (getIntent() != null && getIntent().hasExtra("channelId")) {
            channelId = getIntent().getStringExtra("channelId");
        } else {
            Toast.makeText(this, "Channel ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set title
        titleText.setText("My Channel");

        // Setup RecyclerView
        channelAdapter = new ChannelCategoryAdapter(this, channelsList);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        channelsRecyclerView.setLayoutManager(layoutManager);
        channelsRecyclerView.setAdapter(channelAdapter);

        // Setup click listeners
        backButton.setOnClickListener(v -> finish());

        // Set item click listener
        channelAdapter.setOnItemClickListener(channel -> {
            Intent intent = new Intent(ChannelDetailScreenActivity.this, ChannelVideoPlayerActivity.class);
            intent.putExtra("channelUrl", channel.getUrl());
            startActivity(intent);
        });

        // Get channel details
        getChannelDetailList();
    }

    private void getChannelDetailList() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        String token = prefs.getString(Constants.TOKEN_KEY, "");

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("channel_id", channelId);

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            RestClient.postData(this, "api/p/videoData", body, token, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(ChannelDetailScreenActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        showNoData();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseData);

                            if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                                JSONArray categoriesArray = jsonObject.getJSONObject("data").getJSONArray("categories");

                                List<ChannelCategory> tempList = new ArrayList<>();

                                for (int i = 0; i < categoriesArray.length(); i++) {
                                    JSONObject item = categoriesArray.getJSONObject(i);
                                    ChannelCategory category = new ChannelCategory(
                                            item.getString("id"),
                                            item.getString("name"),
                                            item.optString("url", ""),
                                            item.optString("name3", "")
                                    );
                                    tempList.add(category);
                                }

                                runOnUiThread(() -> {
                                    if (tempList.isEmpty()) {
                                        showNoData();
                                    } else {
                                        channelsList.clear();
                                        channelsList.addAll(tempList);
                                        channelAdapter.notifyDataSetChanged();
                                        showData();
                                    }
                                });
                            } else {
                                runOnUiThread(this::showNoData);
                            }
                        } catch (JSONException e) {
                            runOnUiThread(() -> {
                                Toast.makeText(ChannelDetailScreenActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                                showNoData();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(ChannelDetailScreenActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                            showNoData();
                        });
                    }
                }

                private void showNoData() {
                    channelsRecyclerView.setVisibility(View.GONE);
                    noDataText.setVisibility(View.VISIBLE);
                }

                private void showData() {
                    channelsRecyclerView.setVisibility(View.VISIBLE);
                    noDataText.setVisibility(View.GONE);
                }
            });
        } catch (JSONException e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            showNoData();
        }
    }

    private void showNoData() {
        channelsRecyclerView.setVisibility(View.GONE);
        noDataText.setVisibility(View.VISIBLE);
    }
}