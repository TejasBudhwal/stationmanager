package com.example.stationmanager;

public class Location {
    private double latitude;
    private double longitude;

    public Location() {
        // Default constructor required for Firebase
    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }


    // Getters and setters for latitude and longitude
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
