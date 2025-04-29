package com.videoclub.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ChannelCategory implements Parcelable {
    private String id;
    private String name;
    private String url;
    private String name3;

    public ChannelCategory(String id, String name, String url, String name3) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.name3 = name3;
    }

    protected ChannelCategory(Parcel in) {
        id = in.readString();
        name = in.readString();
        url = in.readString();
        name3 = in.readString();
    }

    public static final Creator<ChannelCategory> CREATOR = new Creator<ChannelCategory>() {
        @Override
        public ChannelCategory createFromParcel(Parcel in) {
            return new ChannelCategory(in);
        }

        @Override
        public ChannelCategory[] newArray(int size) {
            return new ChannelCategory[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName3() {
        return name3;
    }

    public void setName3(String name3) {
        this.name3 = name3;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(url);
        dest.writeString(name3);
    }
}