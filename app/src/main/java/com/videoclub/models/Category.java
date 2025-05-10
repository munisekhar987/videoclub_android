package com.videoclub.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Category implements Parcelable {
    private String id;
    private String name;
    private String status; // Add this field

    public Category() {
        // Default constructor
    }

    // Add this constructor for backward compatibility
    public Category(String id, String name) {
        this.id = id;
        this.name = name;
        this.status = ""; // Initialize with empty status
    }

    public Category(String id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    protected Category(Parcel in) {
        id = in.readString();
        name = in.readString();
        status = in.readString(); // Read status from parcel
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(status); // Write status to parcel
    }
}