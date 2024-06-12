package com.qromarck.reciperu.Entity;

import androidx.annotation.NonNull;

public class Location {

    private String userId;
    private double longitude;
    private double latitude;
    private String city;

    public Location() {
    }

    public Location(String userId,  double longitude, double latitude, String city) {
        this.userId = userId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.city = city;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @NonNull
    @Override
    public String toString() {
        return "Location{" +
                "userId='" + userId + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", city='" + city + '\'' +
                '}';
    }
}
