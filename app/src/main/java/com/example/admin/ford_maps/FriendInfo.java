package com.example.admin.ford_maps;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by admin on 2/8/2018.
 */

public class FriendInfo {
    private LatLng location;
    private Bitmap profilePicture;


    public FriendInfo() {
    }

    public FriendInfo(LatLng location, Bitmap profilePictureUrl) {
        this.location = location;
        this.profilePicture = profilePictureUrl;
    }

    public LatLng getLocation() {
        return location;
    }

    public Bitmap getProfilePicture() {
        return profilePicture;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setProfilePicture(Bitmap profilePicture) {
        this.profilePicture = profilePicture;
    }
}
