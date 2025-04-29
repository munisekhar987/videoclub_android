package com.videoclub;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.videoclub.adapters.ChannelAdapter;
import com.videoclub.adapters.KaraokeAdapter;
import com.videoclub.adapters.TVCategoryAdapter;
import com.videoclub.models.Channel;
import com.videoclub.models.KaraokeItem;
import com.videoclub.models.TVCategory;
import com.videoclub.utils.Constants;
import com.videoclub.utils.RestClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CarasoueldetaildatascreenActivity extends FragmentActivity {

    private static final String TAG = "CarasouelDetailScreen";
    private static final String PREFS_NAME = "MembershipPrefs";
    private static final String TOKEN_KEY = "token";

    private TextView titleTextView;
    private ImageView homeIcon;
    private EditText searchEditText;
    private ImageView searchIcon;
    private RecyclerView mainChannelsRecyclerView;
    private RecyclerView categoriesRecyclerView;
    private RecyclerView bottomMenuRecyclerView;
    private ProgressBar progressBar;
    private TextView noRecordsTextView;

    private ChannelAdapter channelAdapter;
    private TVCategoryAdapter categoryAdapter;
    private ChannelAdapter bottomMenuAdapter;
    private KaraokeAdapter karaokeAdapter;

    private List<Channel> channelList = new ArrayList<>();
    private List<TVCategory> categoryList = new ArrayList<>();
    private List<Channel> bottomMenuList = new ArrayList<>();
    private List<KaraokeItem> karaokeList = new ArrayList<>();

    private String token;
    private int displayType = 0;
    private int offset = 1;
    private String searchText = "";
    private String primaryTvCategoryId = "52";
    private String primaryTvSubCategoryId = "";
    private String tvChannelTitle = "Luzon tv";

    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carasoueldetaildatascreen);

        // Initialize views
        titleTextView = findViewById(R.id.title_text);
        homeIcon = findViewById(R.id.home_icon);
        searchEditText = findViewById(R.id.search_edit_text);
        searchIcon = findViewById(R.id.search_icon);
        mainChannelsRecyclerView = findViewById(R.id.main_channels_recycler_view);
        categoriesRecyclerView = findViewById(R.id.categories_recycler_view);
        bottomMenuRecyclerView = findViewById(R.id.bottom_menu_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        noRecordsTextView = findViewById(R.id.no_records_text);

        // Setup recycler views
        setupRecyclerViews();

        // Setup click listeners
        setupClickListeners();

        // Get token from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        token = prefs.getString(TOKEN_KEY, "");

        // Get display type from intent
        if (getIntent() != null && getIntent().hasExtra("productTitle")) {
            String productTitle = getIntent().getStringExtra("productTitle").toLowerCase();

            if (productTitle.equals("premium")) {
                displayType = 11;
                titleTextView.setText("Premium Channels");
                getTargetAppData();
                makeApiRequest("action", "11");
            } else if (productTitle.equals("mi radio")) {
                displayType = 22;
                titleTextView.setText(tvChannelTitle);
                getTVChannelsCategory(primaryTvCategoryId);
            } else if (productTitle.equals("karaoke")) {
                displayType = 33;
                titleTextView.setText("Karaoke");
                getKaraokeData();
            }
        }

        // Initialize bottom menu data
        initializeBottomMenuData();
    }

    private void setupRecyclerViews() {
        // Main channels recycler view
        GridLayoutManager mainGridLayoutManager = new GridLayoutManager(this, 2);
        mainChannelsRecyclerView.setLayoutManager(mainGridLayoutManager);
        channelAdapter = new ChannelAdapter(this, channelList);
        mainChannelsRecyclerView.setAdapter(channelAdapter);

        // Categories recycler view
        GridLayoutManager categoriesGridLayoutManager = new GridLayoutManager(this, 2);
        categoriesRecyclerView.setLayoutManager(categoriesGridLayoutManager);
        categoryAdapter = new TVCategoryAdapter(this, categoryList);
        categoriesRecyclerView.setAdapter(categoryAdapter);

        // Bottom menu recycler view
        GridLayoutManager bottomMenuGridLayoutManager = new GridLayoutManager(this, 4, GridLayoutManager.HORIZONTAL, false);
        bottomMenuRecyclerView.setLayoutManager(bottomMenuGridLayoutManager);
        bottomMenuAdapter = new ChannelAdapter(this, bottomMenuList);
        bottomMenuRecyclerView.setAdapter(bottomMenuAdapter);

        // Karaoke adapter (will be set when needed)
        karaokeAdapter = new KaraokeAdapter(this, karaokeList);
    }

    private void setupClickListeners() {
        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewHomeScreenActivity.class);
            startActivity(intent);
            finish();
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchText = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchEditText.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                handleSearch();
                return true;
            }
            return false;
        });

        channelAdapter.setOnItemClickListener(channel -> {
            checkVideoUrl(channel);
        });

        categoryAdapter.setOnItemClickListener(category -> {
            if (displayType == 11) {
                getSubcategoriesPremium(category.getId(), token, category.getName());
            } else if (displayType == 22) {
                getSubcategoriesChannels(category.getId());
            }
        });

        bottomMenuAdapter.setOnItemClickListener(menuItem -> {
            getBottomData(menuItem);
        });
    }

    private void initializeBottomMenuData() {
        bottomMenuList.clear();

        if (displayType == 11) {
            // Premium bottom menu
            bottomMenuList.add(new Channel("1", "", Images.letsmeetlower));
            bottomMenuList.add(new Channel("2", "", Images.realtimelower));
            bottomMenuList.add(new Channel("3", "", Images.pagerlower));
            bottomMenuList.add(new Channel("4", "", Images.concyellow));
        } else if (displayType == 22) {
            // TV channels bottom menu
            bottomMenuList.add(new Channel("52", "Luzon tv", Images.luzontvlower));
            bottomMenuList.add(new Channel("53", "Mindanao Tv", Images.mindanaolower));
            bottomMenuList.add(new Channel("54", "Visayas Tv", Images.visayastvlower));
            bottomMenuList.add(new Channel("55", "dbuzz", Images.dbuzzlower));
        }

        bottomMenuAdapter.notifyDataSetChanged();
    }

    private void getTargetAppData() {
        showLoading();

        RestClient.get(this, "api/targetapp/list", token, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(CarasoueldetaildatascreenActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);

                        if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            List<TVCategory> tempList = new ArrayList<>();

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject item = dataArray.getJSONObject(i);
                                tempList.add(new TVCategory(
                                        item.getString("id"),
                                        item.getString("name"),
                                        item.getString("image")
                                ));
                            }

                            runOnUiThread(() -> {
                                categoryList.clear();
                                categoryList.addAll(tempList);
                                categoryAdapter.notifyDataSetChanged();
                                hideLoading();
                            });
                        } else {
                            runOnUiThread(() -> {
                                hideLoading();
                                Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                        jsonObject.optString("message", "Error loading data"),
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            hideLoading();
                            Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                    "Error parsing data",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                "Server error: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void getTVChannelsCategory(String categoryId) {
        showLoading();

        RestClient.get(this, "api/targetapp/list/" + categoryId, token, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(CarasoueldetaildatascreenActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);

                        if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            JSONObject firstCategory = dataArray.getJSONObject(0);
                            JSONArray subCategoryArray = firstCategory.getJSONArray("sub_category");

                            List<TVCategory> tempCategoryList = new ArrayList<>();

                            for (int i = 0; i < subCategoryArray.length(); i++) {
                                JSONObject item = subCategoryArray.getJSONObject(i);
                                tempCategoryList.add(new TVCategory(
                                        item.getString("id"),
                                        item.optString("name", ""),
                                        item.getString("image")
                                ));
                            }

                            String firstSubCategoryId = "";
                            if (subCategoryArray.length() > 0) {
                                firstSubCategoryId = subCategoryArray.getJSONObject(0).getString("id");
                            }

                            String finalFirstSubCategoryId = firstSubCategoryId;
                            runOnUiThread(() -> {
                                categoryList.clear();
                                categoryList.addAll(tempCategoryList);
                                categoryAdapter.notifyDataSetChanged();

                                if (!finalFirstSubCategoryId.isEmpty()) {
                                    primaryTvSubCategoryId = finalFirstSubCategoryId;
                                    getTVChannelsSubCategory(finalFirstSubCategoryId);
                                } else {
                                    hideLoading();
                                }
                            });
                        } else {
                            runOnUiThread(() -> {
                                hideLoading();
                                Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                        jsonObject.optString("message", "Error loading data"),
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            hideLoading();
                            Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                    "Error parsing data",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                "Server error: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void getTVChannelsSubCategory(String subCategoryId) {
        showLoading();

        RestClient.get(this, "api/tvChannels/list/" + subCategoryId, token, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(CarasoueldetaildatascreenActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);

                        if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            List<Channel> tempList = new ArrayList<>();

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject item = dataArray.getJSONObject(i);
                                tempList.add(new Channel(
                                        item.getString("id"),
                                        item.optString("title", ""),
                                        item.getString("image")
                                ));
                            }

                            runOnUiThread(() -> {
                                channelList.clear();
                                channelList.addAll(tempList);
                                channelAdapter.notifyDataSetChanged();
                                hideLoading();
                            });
                        } else {
                            runOnUiThread(() -> {
                                hideLoading();
                                Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                        jsonObject.optString("message", "Error loading data"),
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            hideLoading();
                            Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                    "Error parsing data",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                "Server error: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void makeApiRequest(String name, String type) {
        showLoading();

        String url;
        if (type.equals("11")) {
            url = "http://api.msgnaa.info/api/p/premium";
        } else {
            url = "http://api.msgnaa.info/api/p/live";
        }

        RestClient.getWithCustomHeader(this, url, token, name, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(CarasoueldetaildatascreenActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONObject dataObject = jsonObject.getJSONObject("data");
                        JSONArray channelArray = dataObject.getJSONArray("channel");

                        List<Channel> tempList = new ArrayList<>();

                        for (int i = 0; i < channelArray.length(); i++) {
                            JSONObject item = channelArray.getJSONObject(i);
                            JSONObject dataItem = item.getJSONObject("data");

                            Channel channel = new Channel(
                                    item.getString("id"),
                                    dataItem.getString("chennel"),
                                    dataItem.getString("logo")
                            );

                            if (dataItem.has("upload_account")) {
                                channel.setUploadAccount(dataItem.getString("upload_account"));
                            }

                            if (!searchText.isEmpty()) {
                                if (channel.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                                    tempList.add(channel);
                                }
                            } else {
                                tempList.add(channel);
                            }
                        }

                        runOnUiThread(() -> {
                            channelList.clear();
                            channelList.addAll(tempList);
                            channelAdapter.notifyDataSetChanged();
                            hideLoading();
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            hideLoading();
                            Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                    "Error parsing data",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                "Server error: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void getKaraokeData() {
        showLoading();

        RestClient.get(this, "api/p/karaoke?page_num=" + offset + "&keyword=" + searchText, token, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(CarasoueldetaildatascreenActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);

                        if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            List<KaraokeItem> tempList = new ArrayList<>();

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject item = dataArray.getJSONObject(i);
                                tempList.add(new KaraokeItem(
                                        item.getString("id"),
                                        item.getString("video_name"),
                                        item.optString("video_url", "")
                                ));
                            }

                            runOnUiThread(() -> {
                                if (offset == 1) {
                                    karaokeList.clear();
                                }
                                karaokeList.addAll(tempList);

                                // Switch to karaoke layout
                                setContentView(R.layout.activity_karaoke);
                                RecyclerView karaokeRecyclerView = findViewById(R.id.karaoke_recycler_view);
                                karaokeRecyclerView.setAdapter(karaokeAdapter);
                                karaokeAdapter.notifyDataSetChanged();

                                hideLoading();
                            });
                        } else {
                            runOnUiThread(() -> {
                                hideLoading();
                                Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                        jsonObject.optString("message", "Error loading data"),
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            hideLoading();
                            Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                    "Error parsing data",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                "Server error: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void fetchMoreKaraokeData() {
        if (isLoading) return;

        isLoading = true;
        offset++;

        RestClient.get(this, "api/p/karaoke?page_num=" + offset + "&keyword=" + searchText, token, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    isLoading = false;
                    Toast.makeText(CarasoueldetaildatascreenActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);

                        if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                            JSONArray dataArray = jsonObject.getJSONArray("data");
                            List<KaraokeItem> tempList = new ArrayList<>();

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject item = dataArray.getJSONObject(i);
                                tempList.add(new KaraokeItem(
                                        item.getString("id"),
                                        item.getString("video_name"),
                                        item.optString("video_url", "")
                                ));
                            }

                            runOnUiThread(() -> {
                                int oldSize = karaokeList.size();
                                karaokeList.addAll(tempList);
                                karaokeAdapter.notifyItemRangeInserted(oldSize, tempList.size());
                                isLoading = false;
                            });
                        } else {
                            runOnUiThread(() -> {
                                isLoading = false;
                                Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                        jsonObject.optString("message", "Error loading data"),
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            isLoading = false;
                            Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                    "Error parsing data",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        isLoading = false;
                        Toast.makeText(CarasoueldetaildatascreenActivity.this,
                                "Server error: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void getBottomData(Channel item) {
        String id = item.getId();

        if (id.equals("52") || id.equals("53") || id.equals("54")) {
            primaryTvCategoryId = id;

            if (id.equals("52")) {
                tvChannelTitle = "Luzon Tv";
            } else if (id.equals("53")) {
                tvChannelTitle = "Mindanao Tv";
            } else if (id.equals("54")) {
                tvChannelTitle = "Visayas Tv";
            }

            titleTextView.setText(tvChannelTitle);
            getTVChannelsCategory(id);
        }
    }

    private void getSubcategoriesPremium(String categoryId, String token, String name) {
        if (categoryId.equals("52") || categoryId.equals("53") || categoryId.equals("54")) {
            primaryTvCategoryId = categoryId;
            displayType = 22;

            if (categoryId.equals("52")) {
                tvChannelTitle = "Luzon Tv";
            } else if (categoryId.equals("53")) {
                tvChannelTitle = "Mindanao Tv";
            } else if (categoryId.equals("54")) {
                tvChannelTitle = "Visayas Tv";
            }

            titleTextView.setText(tvChannelTitle);
            initializeBottomMenuData();
            getTVChannelsCategory(categoryId);
        } else {
            makeApiRequest(name, String.valueOf(displayType));
        }
    }

    private void getSubcategoriesChannels(String categoryId) {
        primaryTvSubCategoryId = categoryId;
        getTVChannelsSubCategory(categoryId);
    }

    private void checkVideoUrl(Channel item) {
        if (displayType == 11) {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("detailContent", item);
            intent.putExtra("displayType", "1");
            startActivity(intent);
        } else if (displayType == 22) {
            // Check if URL ends with ?notoken
            String liveUrl = item.getVideoUrl();
            if (liveUrl != null && liveUrl.endsWith("?notoken")) {
                Toast.makeText(this, getString(R.string.app_name) + ": No Video url found", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, VideoPlayerActivity.class);
                intent.putExtra("detailContent", item);
                intent.putExtra("displayType", "2");
                startActivity(intent);
            }
        }
    }

    private void handleSearch() {
        if (displayType == 11) {
            if (searchText.isEmpty()) {
                getTargetAppData();
                makeApiRequest("action", "11");
            } else {
                if (channelList.isEmpty()) {
                    getTargetAppData();
                    makeApiRequest("action", "11");
                } else {
                    List<Channel> filteredList = new ArrayList<>();
                    for (Channel channel : channelList) {
                        if (channel.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                            filteredList.add(channel);
                        }
                    }
                    channelList.clear();
                    channelList.addAll(filteredList);
                    channelAdapter.notifyDataSetChanged();
                }
            }
        } else if (displayType == 33) {
            offset = 1;
            karaokeList.clear();
            karaokeAdapter.notifyDataSetChanged();
            getKaraokeData();
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        noRecordsTextView.setText("Loading...");
        noRecordsTextView.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        if (displayType == 11 || displayType == 22) {
            if (channelList.isEmpty()) {
                noRecordsTextView.setText("No Record Found");
                noRecordsTextView.setVisibility(View.VISIBLE);
            } else {
                noRecordsTextView.setVisibility(View.GONE);
            }
        } else if (displayType == 33) {
            if (karaokeList.isEmpty()) {
                noRecordsTextView.setText("No Record Found");
                noRecordsTextView.setVisibility(View.VISIBLE);
            } else {
                noRecordsTextView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
