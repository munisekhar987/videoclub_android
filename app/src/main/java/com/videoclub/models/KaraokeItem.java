package com.videoclub.models;

import android.os.Parcel;
import android.os.Parcelable;

public class KaraokeItem implements Parcelable {
    private String id;
    private String videoName;
    private String videoUrl;

    public KaraokeItem(String id, String videoName, String videoUrl) {
        this.id = id;
        this.videoName = videoName;
        this.videoUrl = videoUrl;
    }

    protected KaraokeItem(Parcel in) {
        id = in.readString();
        videoName = in.readString();
        videoUrl = in.readString();
    }

    public static final Creator<KaraokeItem> CREATOR = new Creator<KaraokeItem>() {
        @Override
        public KaraokeItem createFromParcel(Parcel in) {
            return new KaraokeItem(in);
        }

        @Override
        public KaraokeItem[] newArray(int size) {
            return new KaraokeItem[size];
        }
    };

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

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(videoName);
        parcel.writeString(videoUrl);
    }
}
