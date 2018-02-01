package com.example.admin.ford_maps;

import java.util.ArrayList;

/**
 * Created by admin on 2/1/2018.
 */

public class Friend {
    private String username;
    private double latitude;
    private double longitude;
    private ArrayList<String> friends;

    public Friend(){

    }

    public Friend(String username, double latitude, double longitude, ArrayList<String> friends) {
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
        this.friends = friends;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

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

    public ArrayList<String> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<String> friends) {
        this.friends = friends;
    }
}
