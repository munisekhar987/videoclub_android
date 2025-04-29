package com.videoclub.models;


import android.os.Parcel;
import android.os.Parcelable;

public class Channel implements Parcelable {
    private String id;
    private String title;
    private String imageUrl;
    private String videoUrl;
    private String uploadAccount;
    private boolean hasError;

    public Channel(String id, String title, String imageUrl) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.hasError = false;
    }

    public Channel(String id, String title, int imageResourceId) {
        this.id = id;
        this.title = title;
        this.imageUrl = "res://" + imageResourceId; // Resource identifier
        this.hasError = false;
    }

    protected Channel(Parcel in) {
        id = in.readString();
        title = in.readString();
        imageUrl = in.readString();
        videoUrl = in.readString();
        uploadAccount = in.readString();
        hasError = in.readByte() != 0;
    }

    public static final Creator<Channel> CREATOR = new Creator<Channel>() {
        @Override
        public Channel createFromParcel(Parcel in) {
            return new Channel(in);
        }

        @Override
        public Channel[] newArray(int size) {
            return new Channel[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getUploadAccount() {
        return uploadAccount;
    }

    public void setUploadAccount(String uploadAccount) {
        this.uploadAccount = uploadAccount;
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(imageUrl);
        parcel.writeString(videoUrl);
        parcel.writeString(uploadAccount);
        parcel.writeByte((byte) (hasError ? 1 : 0));
    }
}