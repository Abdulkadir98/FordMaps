package com.example.admin.ford_maps;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by admin on 1/26/2018.
 */

public class Utils {
    public static String getAccessTokenUserID(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString("ACCESS_TOKEN_USERID", "defaultStringIfNothingFound");
    }
    public static String getNearbyRestaurantUrl(LatLng currentLocation, double radius){
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+
                currentLocation.latitude+","+currentLocation.longitude+"&radius="+
                radius+"&type=restaurant&key=AIzaSyCTtCboaBG8aoGyz5uCyuhboQrGABRUXzs";
    }
}
