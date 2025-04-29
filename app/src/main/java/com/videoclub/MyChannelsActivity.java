package com.videoclub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.videoclub.adapters.CategoryAdapter;
import com.videoclub.adapters.ChannelAdapter;
import com.videoclub.adapters.RegionAdapter;
import com.videoclub.models.Category;
import com.videoclub.models.Channel;
import com.videoclub.models.Region;
import com.videoclub.utils.Constants;
import com.videoclub.utils.Images;
import com.videoclub.utils.RestClient;
import com.videoclub.utils.TVNavigationHelper;

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

public class MyChannelsActivity extends FragmentActivity {

    private ImageView homeButton;
    private TextView titleText;
    private TextView regionWelcomeText;
    private RecyclerView channelsRecyclerView;
    private RecyclerView regionsRecyclerView;
    private RecyclerView categoriesRecyclerView;
    private TextView noDataText;
    private TextView regionsLabelText;

    private List<Channel> channelsList = new ArrayList<>();
    private List<Region> regionsList = new ArrayList<>();
    private List<Category> categoriesList = new ArrayList<>();

    private ChannelAdapter channelAdapter;
    private RegionAdapter regionAdapter;
    private CategoryAdapter categoryAdapter;

    private String currentRegionName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_channels);

        // Initialize views
        homeButton = findViewById(R.id.home_button);
        titleText = findViewById(R.id.title_text);
        regionWelcomeText = findViewById(R.id.region_welcome_text);
        channelsRecyclerView = findViewById(R.id.channels_recycler_view);
        regionsRecyclerView = findViewById(R.id.regions_recycler_view);
        categoriesRecyclerView = findViewById(R.id.categories_recycler_view);
        noDataText = findViewById(R.id.no_data_text);
        regionsLabelText = findViewById(R.id.regions_label_text);

        // Set title
        titleText.setText("My Channel");

        // Setup RecyclerViews
        setupRecyclerViews();

        // Setup click listeners
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyChannelsActivity.this, NewHomeScreenActivity.class);
            startActivity(intent);
            finish();
        });

        // Get categories list
        getCategoryList();
    }

    private void setupRecyclerViews() {
        // Channels RecyclerView
        channelAdapter = new ChannelAdapter(this, channelsList);
        GridLayoutManager channelsLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false);
        channelsRecyclerView.setLayoutManager(channelsLayoutManager);
        channelsRecyclerView.setAdapter(channelAdapter);

        // Set channel click listener
        channelAdapter.setOnItemClickListener(channel -> {
            Intent intent = new Intent(MyChannelsActivity.this, ChannelSubcategoryScreenActivity.class);
            intent.putExtra("data", channel);
            startActivity(intent);
        });

        // Regions RecyclerView
        regionAdapter = new RegionAdapter(this, regionsList);
        LinearLayoutManager regionsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        regionsRecyclerView.setLayoutManager(regionsLayoutManager);
        regionsRecyclerView.setAdapter(regionAdapter);

        // Set region click listener
        regionAdapter.setOnItemClickListener(region -> {
            getChannelsData(region.getId(), region.getName());
        });

        // Categories RecyclerView
        categoryAdapter = new CategoryAdapter(this, categoriesList);
        LinearLayoutManager categoriesLayoutManager = new LinearLayoutManager(this);
        categoriesRecyclerView.setLayoutManager(categoriesLayoutManager);
        categoriesRecyclerView.setAdapter(categoryAdapter);

        // Set category click listener
        categoryAdapter.setOnItemClickListener(category -> {
            getCategoryRegionList(category.getId());
        });

        // Set up TV navigation helpers for improved D-pad navigation

        // Navigation between main channel list and category list (horizontal movement)
        TVNavigationHelper.setupRecyclerViewNavigation(
                channelsRecyclerView,
                categoriesRecyclerView,
                KeyEvent.KEYCODE_DPAD_RIGHT
        );

        TVNavigationHelper.setupRecyclerViewNavigation(
                categoriesRecyclerView,
                channelsRecyclerView,
                KeyEvent.KEYCODE_DPAD_LEFT
        );

        // Navigation between main channel list and regions list (vertical movement)
        TVNavigationHelper.setupRecyclerViewNavigation(
                channelsRecyclerView,
                regionsRecyclerView,
                KeyEvent.KEYCODE_DPAD_DOWN
        );

        TVNavigationHelper.setupRecyclerViewNavigation(
                regionsRecyclerView,
                channelsRecyclerView,
                KeyEvent.KEYCODE_DPAD_UP
        );

        // Navigation between regions list and category list (horizontal movement)
        TVNavigationHelper.setupRecyclerViewNavigation(
                regionsRecyclerView,
                categoriesRecyclerView,
                KeyEvent.KEYCODE_DPAD_RIGHT
        );

        TVNavigationHelper.setupRecyclerViewNavigation(
                categoriesRecyclerView,
                regionsRecyclerView,
                KeyEvent.KEYCODE_DPAD_LEFT
        );
    }

    private void getCategoryList() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        String token = prefs.getString(Constants.TOKEN_KEY, "");

        RestClient.get(this, "api/p/categoryList", token, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MyChannelsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
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

                            List<Category> tempList = new ArrayList<>();

                            for (int i = 0; i < categoriesArray.length(); i++) {
                                JSONObject item = categoriesArray.getJSONObject(i);
                                Category category = new Category(
                                        item.getString("id"),
                                        item.getString("name")
                                );
                                tempList.add(category);
                            }

                            runOnUiThread(() -> {
                                categoriesList.clear();
                                categoriesList.addAll(tempList);
                                categoryAdapter.notifyDataSetChanged();

                                if (!tempList.isEmpty()) {
                                    getCategoryRegionList(tempList.get(0).getId());
                                }
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(MyChannelsActivity.this, "Error loading categories", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(MyChannelsActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MyChannelsActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void getCategoryRegionList(String categoryId) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("id", categoryId);

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            RestClient.post(this, "api/p/regionList", body, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(MyChannelsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseData);

                            if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                                JSONArray regionsArray = jsonObject.getJSONObject("data").getJSONArray("region");

                                List<Region> tempList = new ArrayList<>();

                                for (int i = 0; i < regionsArray.length(); i++) {
                                    JSONObject item = regionsArray.getJSONObject(i);
                                    Region region = new Region(
                                            item.getString("id"),
                                            item.getString("name"),
                                            item.getString("image")
                                    );
                                    tempList.add(region);
                                }

                                runOnUiThread(() -> {
                                    regionsList.clear();
                                    regionsList.addAll(tempList);
                                    regionAdapter.notifyDataSetChanged();

                                    if (!tempList.isEmpty()) {
                                        getChannelsData(tempList.get(0).getId(), tempList.get(0).getName());
                                    }
                                });
                            } else {
                                runOnUiThread(() -> {
                                    Toast.makeText(MyChannelsActivity.this, "Error loading regions", Toast.LENGTH_SHORT).show();
                                });
                            }
                        } catch (JSONException e) {
                            runOnUiThread(() -> {
                                Toast.makeText(MyChannelsActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(MyChannelsActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        } catch (JSONException e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
        }
    }

    private void getChannelsData(String regionId, String regionName) {
        try {
            currentRegionName = regionName;
            regionWelcomeText.setText("Welcome to\nREGION " + regionName);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("id", regionId);

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            RestClient.post(this, "api/p/channelData", body, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(MyChannelsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseData);

                            if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                                JSONArray channelsArray = jsonObject.getJSONObject("data").getJSONArray("categories");

                                List<Channel> tempList = new ArrayList<>();

                                for (int i = 0; i < channelsArray.length(); i++) {
                                    JSONObject item = channelsArray.getJSONObject(i);
                                    Channel channel = new Channel(
                                            item.getString("id"),
                                            item.getString("name"),
                                            ""  // No image URL in this response
                                    );
                                    tempList.add(channel);
                                }

                                runOnUiThread(() -> {
                                    channelsList.clear();
                                    channelsList.addAll(tempList);
                                    channelAdapter.notifyDataSetChanged();

                                    if (tempList.isEmpty()) {
                                        showNoData();
                                    } else {
                                        showData();
                                    }
                                });
                            } else {
                                runOnUiThread(this::showNoData);
                            }
                        } catch (JSONException e) {
                            runOnUiThread(() -> {
                                Toast.makeText(MyChannelsActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                                showNoData();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(MyChannelsActivity.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                            showNoData();
                        });
                    }
                }

                private void showNoData() {
                    channelsRecyclerView.setVisibility(View.GONE);
                    noDataText.setVisibility(View.VISIBLE);
                }
            });
        } catch (JSONException e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
        }
    }

    private void showData() {
        channelsRecyclerView.setVisibility(View.VISIBLE);
        noDataText.setVisibility(View.GONE);
    }

    private void showNoData() {
        channelsRecyclerView.setVisibility(View.GONE);
        noDataText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}