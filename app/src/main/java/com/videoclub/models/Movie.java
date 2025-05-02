package com.videoclub.models;

import java.io.Serializable;

/**
 * Model class for movies
 */
public class Movie implements Serializable {
    private String id;
    private String videoName;
    private String coverImage;
    private String uploadAccount;
    private boolean error;

    public Movie() {
    }

    public Movie(String id, String videoName, String coverImage, String uploadAccount) {
        this.id = id;
        this.videoName = videoName;
        this.coverImage = coverImage;
        this.uploadAccount = uploadAccount;
        this.error = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getUploadAccount() {
        return uploadAccount;
    }

    public void setUploadAccount(String uploadAccount) {
        this.uploadAccount = uploadAccount;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}