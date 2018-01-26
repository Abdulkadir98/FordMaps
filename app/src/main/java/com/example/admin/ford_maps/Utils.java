package com.example.admin.ford_maps;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by admin on 1/26/2018.
 */

public class Utils {
    public static String getAccessToken(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString("ACCESS_TOKEN_USERID", "defaultStringIfNothingFound");
    }
}
