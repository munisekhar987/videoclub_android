package com.videoclub.utils;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Retrofit API service interface for network requests
 */
public interface ApiService {

    /**
     * Login user
     */
    @POST("mobile/login")
    Call<ResponseBody> login(@Body Map<String, String> params);

    /**
     * Get genre categories
     */
    @GET("api/p/genreCategory")
    Call<ResponseBody> getGenreCategories(@Header("Authorization") String token);

    /**
     * Get VOD categories
     */
    @POST("api/p/vods_cat")
    Call<ResponseBody> getVodsCat(@Header("Authorization") String token, @Body Map<String, Object> params);

    /**
     * Search VODs
     */
    @POST("api/p/vods_search")
    Call<ResponseBody> searchVods(@Header("Authorization") String token, @Body Map<String, Object> params);

    /**
     * Verify coupons
     */
    @POST("api/p/verifyCoupons")
    Call<ResponseBody> verifyCoupons(@Header("Authorization") String token, @Body Map<String, String> params);

    /**
     * Save coupons
     */
    @POST("api/p/saveCoupons")
    Call<ResponseBody> saveCoupon(@Header("Authorization") String token, @Body Map<String, String> params);

    /**
     * Get app colors
     */
    @GET("/api/p/get_app_colors")
    Call<ResponseBody> getAppColors(@Header("Authorization") String token);

    /**
     * Get app types
     */
    @GET("/api/p/appTypes")
    Call<ResponseBody> getAppTypes(@Header("Authorization") String token);

    /**
     * Get user profile
     */
    @GET("api/users/profile")
    Call<ResponseBody> getUserProfile(@Header("Authorization") String token);

    /**
     * Get assets
     */
    @POST("assets")
    Call<ResponseBody> getAssets(@Body Map<String, String> params);

    /**
     * Get pages content
     */
    @GET("api/p/get_pages/{pageType}")
    Call<ResponseBody> getPages(@Header("Authorization") String token, @Path("pageType") String pageType);

    /**
     * Get buzz URL
     */
    @GET("api/p/buzz_url")
    Call<ResponseBody> getBuzzUrl(@Header("Authorization") String token);

    /**
     * Get left menu
     */
    @GET("api/p/categories/menu/left")
    Call<ResponseBody> getLeftMenu(@Header("Authorization") String token);

    /**
     * Get right menu
     */
    @GET("api/p/categories/menu/right")
    Call<ResponseBody> getRightMenu(@Header("Authorization") String token);

    /**
     * Get middle menu
     */
    @GET("api/p/categories/menu/middle")
    Call<ResponseBody> getMiddleMenu(@Header("Authorization") String token);

    /**
     * Get bottom menu
     */
    @GET("api/p/categories/menu/bottom")
    Call<ResponseBody> getBottomMenu(@Header("Authorization") String token);

    /**
     * Get top menu
     */
    @GET("api/p/categories/top_menu")
    Call<ResponseBody> getTopMenu(@Header("Authorization") String token);
}