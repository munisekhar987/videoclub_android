package com.videoclub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.videoclub.adapters.CategoryAdapter;
import com.videoclub.adapters.MovieAdapter;
import com.videoclub.models.Category;
import com.videoclub.models.Movie;
import com.videoclub.utilities.ApiService;
import com.videoclub.utilities.Colors;
import com.videoclub.utils.Images;
import com.videoclub.utils.RestClient;
import com.videoclub.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * CineramaActivity - Android TV implementation of the CineramaScreen React Native component
 */
public class CineramaActivity extends FragmentActivity {
    private static final String TAG = "CineramaActivity";

    // UI Components
    private VerticalGridView categoryListView;
    private HorizontalGridView moviesGridView;
    private EditText searchEditText;
    private ImageView homeIcon, searchIcon;
    private TextView titleTextView;
    private ProgressBar progressBar;

    // Adapters
    private CategoryAdapter categoryAdapter;
    private MovieAdapter movieAdapter;

    // Data
    private List<Category> categoryList = new ArrayList<>();
    private List<Movie> movieList = new ArrayList<>();
    private List<Movie> filteredMovieList = new ArrayList<>();

    // State
    private String currentStatus = "New Movies";
    private String currentCategoryId = "1";
    private int currentOffset = 1;
    private boolean isLoading = false;
    private String searchText = "";
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinerama);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Initialize API service
        apiService = RestClient.getApiService();

        // Initialize views
        initViews();

        // Configure listeners
        setupListeners();

        // Load data
        getCategoryList();
        getMovieList(currentCategoryId, currentOffset);
    }

    /**
     * Initialize all views
     */
    private void initViews() {
        categoryListView = findViewById(R.id.category_list_view);
        moviesGridView = findViewById(R.id.movies_grid_view);
        searchEditText = findViewById(R.id.search_edit_text);
        homeIcon = findViewById(R.id.home_icon);
        searchIcon = findViewById(R.id.search_icon);
        titleTextView = findViewById(R.id.title_text_view);
        progressBar = findViewById(R.id.progress_bar);

        // Set up adapters
        categoryAdapter = new CategoryAdapter(this, categoryList);
        movieAdapter = new MovieAdapter(this, movieList);

        categoryListView.setAdapter(categoryAdapter);
        moviesGridView.setAdapter(movieAdapter);

        // Set title
        titleTextView.setText(getString(R.string.blockbuster_movies));

        // Configure grid views
        categoryListView.setNumColumns(1);
        moviesGridView.setNumRows(1);

        // Set home icon image
        Glide.with(this)
                .load(Images.HOME_ICON_RED)
                .into(homeIcon);
    }

    /**
     * Set up event listeners
     */
    private void setupListeners() {
        // Home icon click listener
        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(CineramaActivity.this, NewHomeScreenActivity.class);
            startActivity(intent);
        });

        // Search functionality
        searchEditText.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });

        // Category item click listener
        categoryAdapter.setOnItemClickListener((category, position) -> {
            currentStatus = category.getStatus();
            currentCategoryId = category.getId();
            currentOffset = 1;

            // Clear search text
            searchEditText.setText("");
            searchText = "";

            // Clear movie list and load new data
            movieList.clear();
            movieAdapter.notifyDataSetChanged();

            // Load movies for selected category
            getMovieListForCategory(category);

            // Update category list selection
            categoryAdapter.setSelectedPosition(position);
        });

        // Movie item click listener
        movieAdapter.setOnItemClickListener(movie -> {
            Intent intent = new Intent(CineramaActivity.this, HomeDetailActivity.class);
            intent.putExtra("displayType", "2");
            intent.putExtra("movieId", movie.getId());
            intent.putExtra("movieName", movie.getVideoName());
            intent.putExtra("coverImage", movie.getCoverImage());
            intent.putExtra("uploadAccount", movie.getUploadAccount());
            startActivity(intent);
        });

        // Load more as the user scrolls to the end
        moviesGridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollHorizontally(1) && !isLoading) {
                    // Reached the end, load more
                    loadMoreMovies();
                }
            }
        });
    }

    /**
     * Get category list from API
     */
    private void getCategoryList() {
        String token = sessionManager.getToken();

        if (TextUtils.isEmpty(token)) {
            Log.e(TAG, "Token is empty");
            return;
        }

        Call<ResponseBody> call = apiService.getGenreCategories("Bearer " + token);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        String status = jsonObject.optString("status");

                        if (status.equalsIgnoreCase("success")) {
                            JSONArray dataArray = jsonObject.optJSONArray("data");

                            if (dataArray != null) {
                                categoryList.clear();

                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject categoryObj = dataArray.optJSONObject(i);

                                    if (categoryObj != null) {
                                        Category category = new Category();
                                        category.setId(categoryObj.optString("id"));
                                        category.setStatus(categoryObj.optString("status"));
                                        categoryList.add(category);
                                    }
                                }

                                // Update UI on main thread
                                runOnUiThread(() -> {
                                    categoryAdapter.notifyDataSetChanged();
                                    // Set first item as selected by default
                                    if (!categoryList.isEmpty()) {
                                        categoryAdapter.setSelectedPosition(0);
                                    }
                                });
                            }
                        } else {
                            Log.e(TAG, "API returned error status: " + status);
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    /**
     * Get movie list from API for a specific category and offset
     */
    private void getMovieList(String categoryId, int offset) {
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        String token = sessionManager.getToken();

        if (TextUtils.isEmpty(token)) {
            Log.e(TAG, "Token is empty");
            progressBar.setVisibility(View.GONE);
            isLoading = false;
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("cat_id", categoryId);
        params.put("page_num", offset);

        Call<ResponseBody> call = apiService.getVodsCat("Bearer " + token, params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        String status = jsonObject.optString("status");

                        if (status.equalsIgnoreCase("success")) {
                            JSONObject dataObj = jsonObject.optJSONObject("data");

                            if (dataObj != null) {
                                // Get movies for current status
                                JSONArray moviesArray = dataObj.optJSONArray(currentStatus);

                                if (moviesArray != null) {
                                    List<Movie> newMovies = new ArrayList<>();

                                    for (int i = 0; i < moviesArray.length(); i++) {
                                        JSONObject movieObj = moviesArray.optJSONObject(i);

                                        if (movieObj != null) {
                                            Movie movie = new Movie();
                                            movie.setId(movieObj.optString("id"));
                                            movie.setVideoName(movieObj.optString("video_name"));
                                            movie.setCoverImage(movieObj.optString("cover_image"));
                                            movie.setUploadAccount(movieObj.optString("upload_account", ""));
                                            movie.setError(false);
                                            newMovies.add(movie);
                                        }
                                    }

                                    // If this is a fresh load (offset 1), clear the list
                                    if (offset == 1) {
                                        movieList.clear();
                                    }

                                    // Add new movies to the list
                                    movieList.addAll(newMovies);

                                    // Update UI on main thread
                                    runOnUiThread(() -> {
                                        movieAdapter.notifyDataSetChanged();
                                    });
                                }
                            }
                        } else {
                            Log.e(TAG, "API returned error status: " + status);
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                isLoading = false;
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Get movie list for a specific category
     */
    private void getMovieListForCategory(Category category) {
        currentStatus = category.getStatus();
        currentCategoryId = category.getId();
        currentOffset = 1;

        getMovieList(currentCategoryId, currentOffset);
    }

    /**
     * Load more movies (pagination)
     */
    private void loadMoreMovies() {
        if (isLoading) return;

        currentOffset++;
        getMovieList(currentCategoryId, currentOffset);
    }

    /**
     * Search functionality
     */
    private void performSearch() {
        searchText = searchEditText.getText().toString().trim();

        if (TextUtils.isEmpty(searchText)) {
            // Reset to original list if search is cleared
            movieList.clear();
            filteredMovieList.clear();
            movieAdapter.notifyDataSetChanged();
            getMovieList(currentCategoryId, 1);

            // Hide category list when searching
            categoryListView.setVisibility(View.VISIBLE);
        } else {
            // Hide category list when searching
            categoryListView.setVisibility(View.GONE);

            // Perform search API call
            searchMovies(searchText);
        }
    }

    /**
     * API call for movie search
     */
    private void searchMovies(String query) {
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        String token = sessionManager.getToken();

        if (TextUtils.isEmpty(token)) {
            Log.e(TAG, "Token is empty");
            progressBar.setVisibility(View.GONE);
            isLoading = false;
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", query);

        Call<ResponseBody> call = apiService.searchVods("Bearer " + token, params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);

                        JSONArray dataArray = jsonObject.optJSONArray("data");

                        if (dataArray != null) {
                            filteredMovieList.clear();

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject movieObj = dataArray.optJSONObject(i);

                                if (movieObj != null) {
                                    Movie movie = new Movie();
                                    movie.setId(movieObj.optString("id"));
                                    movie.setVideoName(movieObj.optString("video_name"));
                                    movie.setCoverImage(movieObj.optString("cover_image"));
                                    movie.setUploadAccount(movieObj.optString("upload_account", ""));
                                    movie.setError(false);
                                    filteredMovieList.add(movie);
                                }
                            }

                            // Update the movie adapter with filtered results
                            movieAdapter.updateMovies(filteredMovieList);
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing response: " + e.getMessage());
                    }
                } else {
                    Log.e(TAG, "Response not successful: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                isLoading = false;
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Handle image loading error
     */
    public void handleImageError(int position) {
        if (position >= 0 && position < movieList.size()) {
            Movie movie = movieList.get(position);
            movie.setError(true);
            movieAdapter.notifyItemChanged(position);
        }
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