package com.videoclub.utils;

import android.content.Context;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestClient {
    // Direct OkHttp client for manual HTTP requests
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    // Manual OkHttp methods
    public static void get(Context context, String endpoint, String token, Callback callback) {
        String baseUrl = context.getString(R.string.api_base_url);
        String url = baseUrl + endpoint;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void getWithCustomHeader(Context context, String fullUrl, String token, String appName, Callback callback) {
        Request request = new Request.Builder()
                .url(fullUrl)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("target-app-name", appName)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void postData(Context context, String endpoint, RequestBody body, String token, Callback callback) {
        String baseUrl = context.getString(R.string.api_base_url);
        String url = baseUrl + endpoint;

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void post(Context context, String endpoint, RequestBody body, Callback callback) {
        String baseUrl = context.getString(R.string.api_base_url);
        String url = baseUrl + endpoint;

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(callback);
    }

    // Retrofit implementation
    private static final String BASE_URL = "http://api.msgnaa.info/";
    private static Retrofit retrofit = null;

    /**
     * Get a configured Retrofit instance
     */
    private static Retrofit getClient() {
        if (retrofit == null) {
            // Create logging interceptor for debugging
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Configure OkHttpClient with timeouts
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Create Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().create()))
                    .build();
        }
        return retrofit;
    }

    /**
     * Get the API service
     */
    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}