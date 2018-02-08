package com.example.admin.ford_maps;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by admin on 2/8/2018.
 */

public class FriendInfo {
    private LatLng location;
    private String profilePictureUrl;

    public FriendInfo() {
    }

    public FriendInfo(LatLng location, String profilePictureUrl) {
        this.location = location;
        this.profilePictureUrl = profilePictureUrl;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}
